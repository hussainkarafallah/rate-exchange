package com.hussainkarafallah.order.service;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.interfaces.TradeEvent;
import com.hussainkarafallah.messaging.KafkaHeaders;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.order.domain.PriceBookEntry;
import com.hussainkarafallah.order.repository.PriceBookEntryRepository;
import com.hussainkarafallah.utils.UuidUtils;
import com.transferwise.kafka.tkms.api.ITransactionalKafkaMessageSender;
import com.transferwise.kafka.tkms.api.TkmsMessage;
import com.transferwise.kafka.tkms.api.TkmsMessage.Header;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteTrade {
    private final PriceBookEntryRepository priceBookEntryRepository;

    private final ITransactionalKafkaMessageSender transactionalKafkaMessageSender;

    public void executeTrade(Instrument instrument, BigDecimal price, Long firstTraderId , Long secondTraderId){
        priceBookEntryRepository.save(new PriceBookEntry(instrument, price));
        TradeEvent event = TradeEvent.builder()
            .id(UuidUtils.generatePrefixCombUuid())
            .instrument(instrument)
            .price(price)
            .firstTraderId(firstTraderId)
            .secondTraderId(secondTraderId)
            .date(Instant.now())
            .build();
        var header = new Header().setKey(KafkaHeaders.IDEMPOTENCY_HEADER).setValue(toBytes(event.getId()));
        transactionalKafkaMessageSender.sendMessage(
                new TkmsMessage()
                        .setTopic(KafkaTopics.ORDER_UPDATE_TOPIC)
                        .setHeaders(List.of(header))
                        .setKey(instrument.toString())
                        .setValue(toBytes(event)));
    }
}
