package com.hussainkarafallah.order.service;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.util.List;
import java.util.UUID;

import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.messaging.KafkaHeaders;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
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
public class PublishOrderUpdate {

    private final ITransactionalKafkaMessageSender transactionalKafkaMessageSender;

    @Transactional(propagation = Propagation.MANDATORY)
    public void onOrderCreated(@NonNull Order order) {
        onOrderUpdated(null, OrderMapper.toOrderSnapshot(order));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void onOrderUpdated(OrderSnapshot oldOrder, @NonNull OrderSnapshot newOrder) {
        UUID eventId = UuidUtils.generatePrefixCombUuid();
        OrderUpdateEvent event = OrderUpdateEvent.builder()
                .eventId(eventId)
                .oldState(oldOrder == null ? null : oldOrder.getState())
                .newState(newOrder.getState())
                .snapshot(newOrder)
                .build();
        var header = new Header().setKey(KafkaHeaders.IDEMPOTENCY_HEADER).setValue(toBytes(eventId));
        transactionalKafkaMessageSender.sendMessage(
                new TkmsMessage()
                        .setTopic(KafkaTopics.ORDER_UPDATE_TOPIC)
                        .setHeaders(List.of(header))
                        .setKey(newOrder.getId().toString())
                        .setValue(toBytes(event)));
    }

    

    
}
