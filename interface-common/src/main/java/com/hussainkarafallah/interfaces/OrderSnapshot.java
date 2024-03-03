package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class OrderSnapshot {

    @NonNull
    private final UUID id;

    @NonNull
    private final String symbol;

    @NonNull
    private final String orderState;

    @NonNull
    private final String orderType;

    @NonNull
    private final Optional<BigDecimal> price;

    @NonNull
    private final BigDecimal targetQuantity;

    @NonNull
    private final BigDecimal fulfilledQuantity;

    @NonNull
    private final Long traderId;
}