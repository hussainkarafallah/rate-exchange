package com.hussainkarafallah.matchingengine.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.MatchingType;

import io.micrometer.common.lang.NonNull;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class MatchingOrder {
    @NonNull
    private UUID id;
    @NonNull
    private UUID orderId;
    @NonNull
    private Instrument instrument;
    @NonNull
    private BigDecimal price;
    @NonNull
    private BigDecimal quantity;
    @NonNull
    private MatchingType type;
}