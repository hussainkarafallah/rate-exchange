package com.hussainkarafallah.order.service;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.KafkaTopics;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.utils.UuidUtils;
import com.transferwise.kafka.tkms.api.ITransactionalKafkaMessageSender;
import com.transferwise.kafka.tkms.api.TkmsMessage;
import com.transferwise.kafka.tkms.api.TkmsMessage.Header;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BroadcastOrder {
    
    private final ITransactionalKafkaMessageSender transactionalKafkaMessageSender;

    @Transactional(propagation = Propagation.MANDATORY)
    public void onOrderCreated(@NonNull Order order){
        onOrderUpdated(null, order);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void onOrderUpdated(Order oldOrder , @NonNull Order newOrder){
        UUID eventId = UuidUtils.generatePrefixCombUuid();
        OrderUpdateEvent event = OrderUpdateEvent.builder()
            .eventId(eventId)
            .oldState(oldOrder == null ? null : oldOrder.getOrderState().name())
            .newState(newOrder.getOrderState().name())
            .oldFulfilllment(oldOrder == null ? null : oldOrder.getFulfilledQuantity())
            .newFulfillment(newOrder.getFulfilledQuantity())
            .snapshot(toOrderSnapshot(newOrder))
            .build();
        var header = new Header().setKey("idempotency-uuid").setValue(toBytes(eventId));
        transactionalKafkaMessageSender.sendMessage(
            new TkmsMessage()
                .setTopic(KafkaTopics.OrderUpdate)
                .setHeaders(List.of(header))
                .setKey(newOrder.getInstrument().name())
                .setValue(toBytes(event))
        );
    }

    private static OrderSnapshot toOrderSnapshot(Order order) {
        return OrderSnapshot.builder()
                .id(order.getId())
                .symbol(order.getInstrument().name())
                .orderState(order.getOrderState().name())
                .orderType(order.getOrderType().name())
                .price(Optional.ofNullable(order.getPrice()))
                .targetQuantity(order.getTargetQuantity())
                .fulfilledQuantity(order.getFulfilledQuantity())
                .traderId(order.getTraderId())
                .build();
    }
}
