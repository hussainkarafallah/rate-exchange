package com.hussainkarafallah.order.repository;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.fromBytes;
import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void save(Order order) {
        // todo add optimistic locking
        Map<String, Object> params = Map.of(
                "id", order.getId(),
                "instrument", order.getInstrument().name(),
                "orderState", order.getState().name(),
                "orderType", order.getOrderType().name(),
                "traderId", order.getTraderId(),
                "fulfillments", toBytes(order.getFulfillments())
        );

        String sql = """
            INSERT INTO orders (id, instrument, order_state, order_type, trader_id, fulfillments)
            VALUES (:id, :instrument, :orderState, :orderType, :traderId, :fulfillments)
            ON CONFLICT (id) DO UPDATE SET
            instrument = EXCLUDED.instrument,
            order_state = EXCLUDED.order_state,
            order_type = EXCLUDED.order_type,
            trader_id = EXCLUDED.trader_id,
            fulfillments = EXCLUDED.fulfillments;
            """;

        jdbcTemplate.update(sql, params);
    }

    public Optional<Order> findById(UUID id) {
        Map<String, Object> params = Map.of("id", id);
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                "SELECT * FROM orders WHERE id = :id",
                params,
                new OrderRowMapper()));
    }


    private class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            Instrument instrument = Instrument.valueOf(rs.getString("instrument"));
            OrderState orderState = OrderState.valueOf(rs.getString("order_state"));
            OrderType orderType = OrderType.valueOf(rs.getString("order_type"));
            Long traderId = rs.getLong("trader_id");
            byte[] fulfillmentsBlob = rs.getBytes("fulfillments");
            List<Fulfillment> fulfillments = fromBytes(fulfillmentsBlob, new TypeReference<List<Fulfillment>>(){}); 
            return new Order(id, instrument, orderState, orderType, traderId, fulfillments);
        }
    }
}
