package com.example.hussainkarafallah.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Table;

import com.hussainkarafallah.interfaces.InstrumentSymbol;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@Table("simple_order")
public class SimpleOrder implements Order {

    @NonNull
    private final UUID id;

    @NonNull
    private final InstrumentSymbol symbol;

    @NonNull
    private final OrderState orderState;

    @NonNull
    private final OrderType orderType;

    @NonNull
    Optional<BigDecimal>  price;

    @NonNull
    private final BigDecimal targetQuantity;

    @NonNull
    private final BigDecimal fulfilledQuantity;

    @Nonnull
    private final Long traderId;
    
    
    private SimpleOrder(
        UUID id,
        InstrumentSymbol symbol,
        OrderState orderState,
        OrderType orderType,
        Optional<BigDecimal> price,
        BigDecimal targetQuantity,
        BigDecimal fulfilledQuantity,
        Long traderId
    ) {
        this.id = id;
        this.symbol = symbol;
        this.orderState = orderState;
        this.orderType = orderType;
        this.targetQuantity = targetQuantity;
        this.fulfilledQuantity = fulfilledQuantity;
        this.price = price;
        this.traderId = traderId;
        validate();
    }
    
    @Override
    public void validate(){
        Order.super.validate();
    }

}
