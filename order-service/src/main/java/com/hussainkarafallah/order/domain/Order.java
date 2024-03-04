package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.DomainValidationException;
import com.hussainkarafallah.utils.UuidUtils;

import org.apache.commons.lang3.NotImplementedException;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Order {

    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private OrderState state;

    @NonNull
    private final OrderType orderType;

    @Nonnull
    private final Long traderId;

    @Nonnull
    List<Fulfillment> fulfillments;

    @Nonnull
    private final Instant dateCreated;

    @Nonnull
    private Instant dateUpdated;

    @Setter
    private Integer Version = 0;

    @Builder(builderMethodName = "newOrderBuilder")
    private Order(
        UUID id,
        Instrument instrument,
        OrderType orderType,
        BigDecimal price,
        BigDecimal targetQuantity,
        Long traderId
    ) {

        if (targetQuantity.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Target quantity cannot be negative");
        }
        if (price != null  && price.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Price cannot be negative");
        }

        this.id = id;
        this.instrument = instrument;
        this.state = OrderState.OPEN;
        this.orderType = orderType;
        this.traderId = traderId;
        this.dateCreated = Instant.now();
        this.dateUpdated = this.dateCreated;
        
        if (instrument.isComposite()) {
            throw new NotImplementedException("did not implement this yet for composite");
        } else {
            this.fulfillments = List.of(Fulfillment.newFulfillment(UuidUtils.generatePrefixCombUuid(), instrument, targetQuantity, price));
        }

        validate();
    }

    void validate() {
        this.fulfillments.forEach(Fulfillment::validate);
    }

    
    public void setState(OrderState newState){
        this.state = newState;
        validate();
    }

    public void setFullfilments(List<Fulfillment>fulfillments){
        this.fulfillments = fulfillments;
        validate();
    }


}