package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.DomainValidationException;
import com.hussainkarafallah.utils.UuidUtils;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Getter
@Table("order")
@AllArgsConstructor
public class Order {

    @Id
    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private final OrderState orderState;

    @NonNull
    private final OrderType orderType;

    @Nullable
    @Column("price")
    private final BigDecimal price;

    @NonNull
    private final BigDecimal targetQuantity;

    @NonNull
    private final BigDecimal fulfilledQuantity;

    @Nonnull
    private final Long traderId;

    @Nonnull
    @Column("matching_requests")
    List<ComponentMatchingRequest> matchingRequests;

    @Builder(builderMethodName = "newOrderBuilder")
    private Order(
        UUID id,
        Instrument instrument,
        OrderType orderType,
        BigDecimal price,
        BigDecimal targetQuantity,
        Long traderId
    ) {
        this.id = id;
        this.instrument = instrument;
        this.orderState = OrderState.OPEN;
        this.orderType = orderType;
        this.targetQuantity = targetQuantity;
        this.fulfilledQuantity = BigDecimal.ZERO;
        this.price = price;
        this.traderId = traderId;
        this.matchingRequests = createMatchingRequests();
        validate();
    }

    private List<ComponentMatchingRequest> createMatchingRequests() {
        if (instrument.isComposite()) {
            return instrument.getComponents()
                    .stream()
                    .map(instrument -> new ComponentMatchingRequest(UuidUtils.generatePrefixCombUuid(), instrument))
                    .toList();
        } else {
            return List.of(new ComponentMatchingRequest(UuidUtils.generatePrefixCombUuid(), instrument));
        }
    }

    void validate() {
        if (targetQuantity.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Target quantity cannot be negative");
        }
        if (targetQuantity.compareTo(getFulfilledQuantity()) == -1) {
            throw new DomainValidationException("Fulfilled quantity cannot be more than target quantity");
        }
        if (fulfilledQuantity.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Fulfilled quantity cannot be negative");
        }
        if (price != null  && price.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Price cannot be negative");
        }
    }

    @Value
    static public class ComponentMatchingRequest {
        UUID id;
        Instrument component;
    }

}