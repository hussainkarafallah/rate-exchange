package com.hussainkarafallah.order.repository;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.domain.Order.ComponentMatchingRequest;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderJdbcRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<Order> findById(UUID id) {
        String sql = "SELECT * FROM \"order\" WHERE id = :id";
        Map<String, Object> params = Map.of("id", id.toString());
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, new OrderRowMapper()));

    }

    public void save(Order order) {
        String sql = "INSERT INTO \"order\" (id, instrument, order_state, order_type, price, target_quantity, fulfilled_quantity, trader_id, matching_requests) "
                +
                "VALUES (:id, :instrument, :orderState, :orderType, :price, :targetQuantity, :fulfilledQuantity, :traderId, :matchingRequests) "
                +
                "ON CONFLICT (id) DO UPDATE SET " +
                "instrument = :instrument, order_state = :orderState, order_type = :orderType, price = :price, target_quantity = :targetQuantity, "
                +
                "fulfilled_quantity = :fulfilledQuantity, trader_id = :traderId, matching_requests = :matchingRequests";

        Map<String, Object> params = new HashMap<>();
        params.put("id", order.getId());
        params.put("instrument", order.getInstrument().toString());
        params.put("orderState", order.getOrderState().toString());
        params.put("orderType", order.getOrderType().toString());
        params.put("price", order.getPrice());
        params.put("targetQuantity", order.getTargetQuantity());
        params.put("fulfilledQuantity", order.getFulfilledQuantity());
        params.put("traderId", order.getTraderId());
        params.put("matchingRequests", serializeMatchingRequests(order.getMatchingRequests()));

        jdbcTemplate.update(sql, params);
    }

    private byte[] serializeMatchingRequests(List<Order.ComponentMatchingRequest> matchingRequests) {
        return toBytes(matchingRequests);
    }

    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = UUID.fromString(rs.getString("id"));
            Instrument instrument = Instrument.valueOf(rs.getString("instrument"));
            OrderState orderState = OrderState.valueOf(rs.getString("order_state"));
            OrderType orderType = OrderType.valueOf(rs.getString("order_type"));
            BigDecimal price = rs.getBigDecimal("price");
            BigDecimal targetQuantity = rs.getBigDecimal("target_quantity");
            BigDecimal fulfilledQuantity = rs.getBigDecimal("fulfilled_quantity");
            Long traderId = rs.getLong("trader_id");
            List<ComponentMatchingRequest> matchingRequests = fromBytes(
                    rs.getBytes("matching_requests"),
                    new TypeReference<List<ComponentMatchingRequest>>() {
                    }
            );

            return new Order(
                id,
                instrument,
                orderState,
                orderType,
                price,
                targetQuantity,
                fulfilledQuantity,
                traderId,
                matchingRequests
            );
        }
    }
}
