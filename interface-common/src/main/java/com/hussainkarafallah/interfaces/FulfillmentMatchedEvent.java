package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FulfillmentMatchedEvent {
    private UUID buyFulfillmentId;
    private UUID buyOrderId;
    private UUID sellFulfillmentId;
    private UUID sellOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
}
