package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.DomainValidationException;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Fulfillment {
    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private FulfillmentState state;

    @NonNull
    private final BigDecimal targetQuantity;

    @NonNull
    private final BigDecimal targetPrice;

    private BigDecimal fulfilledQuantity;
    private BigDecimal fulfilledPrice;

    @NonNull
    private Instant dateUpdated;

    public Fulfillment(
        UUID id,
        Instrument instrument,
        FulfillmentState state,
        BigDecimal targetQuantity,
        BigDecimal targetPrice,
        BigDecimal fulfilledQuantity,
        BigDecimal fulfilledPrice,
        Instant dateUpdate
    ) {
        this.id = id;
        this.instrument = instrument;
        this.state = state;
        this.targetQuantity = targetQuantity;
        this.targetPrice = targetPrice;
        this.fulfilledQuantity = fulfilledQuantity;
        this.fulfilledPrice = fulfilledPrice;
        this.dateUpdated = dateUpdate;
        if (this.instrument.isComposite()) {
            throw new DomainValidationException("fulfillments can only be for simple instruments");
        }
        if (state.equals(FulfillmentState.NOT_COMPLETED)) {
            if (fulfilledQuantity != BigDecimal.ZERO || fulfilledPrice != null) {
                throw new DomainValidationException("Idle fulfillments should have null matching price and quantity");
            }
        }
    };

    public static Fulfillment newFulfillment(UUID id, Instrument instrument, BigDecimal targetQuantity, BigDecimal targetPrice) {
        return new Fulfillment(id, instrument, FulfillmentState.NOT_COMPLETED, targetQuantity, targetPrice, BigDecimal.ZERO, null, Instant.now());
    }

    public void validate() {
        // todo
    }

    public void setState(FulfillmentState state){
        this.state = state;
        validate();
    }

}
