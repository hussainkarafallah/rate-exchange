package com.hussainkarafallah.order.service;

import com.hussainkarafallah.order.service.commands.CancelFulfillmentCommand;
import com.transferwise.kafka.tkms.api.ITransactionalKafkaMessageSender;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelFulfillment {
    
    private final ITransactionalKafkaMessageSender transactionalKafkaMessageSender;

    @Transactional
    public void exec(CancelFulfillmentCommand command){
        /*// smaller message means higher performance
        RequestMatchingEvent event = RequestMatchingEvent.builder()
            .requestId(command.getFulfillmentId())
            .operation(MatchingOperation.UNQUEUE.name())
            .instrument(command.getInstrument().name())
            .build();
        var header = new Header().setKey(KafkaHeaders.IDEMPOTENCY_HEADER).setValue(toBytes(event.getRequestId()));
        transactionalKafkaMessageSender.sendMessage(
                new TkmsMessage()
                        .setTopic(KafkaTopics.MATCHING_REQUEST_TOPIC)
                        .setHeaders(List.of(header))
                        .setKey(event.getInstrument())
                        .setValue(toBytes(event)));*/
    }
}
