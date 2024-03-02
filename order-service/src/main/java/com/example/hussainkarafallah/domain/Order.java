package com.example.hussainkarafallah.domain;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.interfaces.InstrumentSymbol;

public interface Order {

    UUID getId();

    InstrumentSymbol getSymbol();

    OrderState getOrderState();

    BigDecimal getTargetQuantity();

    BigDecimal getFulfilledQuantity();

    Optional<BigDecimal> getPrice();

    Long getTraderId();

    default void validate() {
        if (getTargetQuantity().compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Target quantity cannot be negative");
        }
        if (getTargetQuantity().compareTo(getFulfilledQuantity()) == -1) {
            throw new DomainValidationException("Fulfilled quantity cannot be more than target quantity");
        }
        if (getFulfilledQuantity().compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Fulfilled quantity cannot be negative");
        }
        if (getPrice().isPresent() && getPrice().get().compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Price cannot be negative");
        }
    }

}