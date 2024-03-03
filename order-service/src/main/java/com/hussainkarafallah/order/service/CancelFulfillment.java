package com.hussainkarafallah.order.service;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.util.List;

import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.CancelMatchingEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.messaging.KafkaHeaders;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.service.commands.CancelFulfillmentCommand;
import com.hussainkarafallah.order.service.commands.RequestFulfillmentCommand;
import com.transferwise.kafka.tkms.api.ITransactionalKafkaMessageSender;
import com.transferwise.kafka.tkms.api.TkmsMessage;
import com.transferwise.kafka.tkms.api.TkmsMessage.Header;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelFulfillment {
    
    private final ITransactionalKafkaMessageSender transactionalKafkaMessageSender;

    @Transactional
    public void exec(CancelFulfillmentCommand command){
        CancelMatchingEvent event = CancelMatchingEvent.builder()
            .requestId(command.getRequestId())
            .orderId(command.getOrderId())
            .instrument(command.getInstrument().name())
            .type(getMatchingType(command.getType()).name())
            .build();
        var header = new Header().setKey(KafkaHeaders.IDEMPOTENCY_HEADER).setValue(toBytes(event.getRequestId()));
        transactionalKafkaMessageSender.sendMessage(
                new TkmsMessage()
                        .setTopic(KafkaTopics.MAT)
                        .setHeaders(List.of(header))
                        .setKey(event.getInstrument())
                        .setValue(toBytes(event)));
    }

    MatchingType getMatchingType(OrderType orderType){
        return switch (orderType) {
            case BUY -> MatchingType.BUY;
            case SELL -> MatchingType.SELL;
            default -> throw new IllegalStateException("can only send requests to matching engine for BUY/SELL orders");
        };
    }
}
