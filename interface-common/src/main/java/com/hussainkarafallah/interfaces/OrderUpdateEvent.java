package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderUpdateEvent {
    UUID eventId;
    UUID orderId;
    String oldState;
    String newState;
    BigDecimal oldFulfilllment;
    BigDecimal newFulfillment;
    OrderSnapshot snapshot;
}
