package com.hussainkarafallah.matchingengine.domain;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.fromBytes;

import java.util.Map;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;

import org.apache.kafka.common.serialization.Deserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingOrderDeserializer implements Deserializer<MatchingOrder> {


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public MatchingOrder deserialize(String topic, byte[] data) {
        UUID parsedId = null;
        try{
            var event = fromBytes(data, RequestMatchingEvent.class);
            parsedId = event.getRequestId();
            return toMatchingOrder(event);
        } catch(Exception ex){
            log.error("Caught a poison pill consuming event {} in topic {}", parsedId, topic);
            return null;
        }
    }

    private MatchingOrder toMatchingOrder(RequestMatchingEvent event){
        try{
            return MatchingOrder.builder()
                .id(event.getRequestId())
                .orderId(event.getOrderId())
                .instrument(Instrument.valueOf(event.getInstrument()))
                .price(event.getPrice())
                .quantity(event.getQuantity())
                .type(MatchingType.valueOf(event.getType()))
                .build();
        } catch(Exception ex){
            log.error("Unrecognized event payload in matching engine event with id {}", event.getRequestId(), ex);
            return null;
        }
    }

    @Override
    public void close() {
    }
}