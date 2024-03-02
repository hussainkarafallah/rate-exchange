package com.example.hussainkarafallah.service.commands;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.example.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.InstrumentSymbol;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateOrderCommand {
    private final UUID idempotencyUuid;
    private final InstrumentSymbol symbol;
    private final OrderType orderType;
    private final Optional<BigDecimal> price;
    private final Long userId;
}
