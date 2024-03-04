package com.hussainkarafallah.order.util;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.fromBytes;

import java.util.Map;
import java.util.UUID;

import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;

import org.apache.kafka.common.serialization.Deserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FulfillmentMatchedEventDeserializer implements Deserializer<FulfillmentMatchedEvent> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public FulfillmentMatchedEvent deserialize(String topic, byte[] data) {
        UUID parsedId = null;
        try{
            var event = fromBytes(data, FulfillmentMatchedEvent.class);
            return event;
        } catch(Exception ex){
            log.error("Caught a poison pill consuming event {} in topic {}", parsedId, topic);
            return null;
        }
    }

    @Override
    public void close() {
    }
}
