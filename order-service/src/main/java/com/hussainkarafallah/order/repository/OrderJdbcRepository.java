package com.hussainkarafallah.order.repository;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.fromBytes;
import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void save(Order order) {
        Map<String, Object> params = Map.of(
                "id", order.getId(),
                "instrument", order.getInstrument().name(),
                "orderState", order.getState().name(),
                "orderType", order.getOrderType().name(),
                "traderId", order.getTraderId(),
                "fulfillments", toBytes(order.getFulfillments()),
                "dateCreated", Timestamp.from(order.getDateCreated()),
                "dateUpdated", Timestamp.from(order.getDateUpdated()),
                "version", order.getVersion() + 1);

        String sql = """
                INSERT INTO orders (id, instrument, order_state, order_type, trader_id, fulfillments, date_created, date_updated, version)
                VALUES (:id, :instrument, :orderState, :orderType, :traderId, :fulfillments, :dateCreated, :dateUpdated, :version)
                ON CONFLICT (id) DO UPDATE SET
                instrument = EXCLUDED.instrument,
                order_state = EXCLUDED.order_state,
                order_type = EXCLUDED.order_type,
                trader_id = EXCLUDED.trader_id,
                fulfillments = EXCLUDED.fulfillments,
                date_updated = EXCLUDED.date_updated,
                version = EXCLUDED.version
                WHERE orders.version = :version - 1;
                """;

        int updated = jdbcTemplate.update(sql, params);
        if (updated == 0) {
            throw new OptimisticLockingFailureException("Optimistic locking failure: Order with ID " + order.getId()
                    + " has been updated or deleted by another transaction.");
        }
    }

    public Optional<Order> findById(UUID id) {

        return jdbcTemplate.query(
                "SELECT * FROM orders WHERE id = :id",
                Map.of("id", id),
                new OrderRowMapper()).stream().findFirst();

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
            List<Fulfillment> fulfillments = fromBytes(fulfillmentsBlob, new TypeReference<List<Fulfillment>>() {
            });
            Timestamp dateCreated = rs.getTimestamp("date_created");
            Timestamp dateUpdated = rs.getTimestamp("date_updated");
            int version = rs.getInt("version");

            Order order = new Order(id, instrument, orderState, orderType, traderId, fulfillments,
                    dateCreated.toInstant(), dateUpdated.toInstant(), version);
            order.setVersion(version);
            return order;
        }
    }
}