package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FulfillmentSnapshot {

    private UUID id;

    private String instrument;

    private String state;

    private BigDecimal targetQuantity;

    private BigDecimal fulfilledQuantity;
    private BigDecimal targetPrice;
    private BigDecimal fulfilledPrice;
}
