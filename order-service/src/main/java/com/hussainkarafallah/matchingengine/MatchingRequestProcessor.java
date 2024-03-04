package com.hussainkarafallah.matchingengine;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hussainkarafallah.config.KafkaConfiguration;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.matchingengine.domain.MatchingOrder;
import com.hussainkarafallah.matchingengine.domain.MatchingOrderDeserializer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchingRequestProcessor {

    public static final String MATCHING_REQUEST_TOPIC = "matching.request";

    private final KafkaConfiguration kafkaConfiguration;
    private final KafkaConsumer<String, MatchingOrder> consumer;
    private final MatchingEngine engine;
    private final Cache<UUID, Boolean> processedOrdersCache;

    public MatchingRequestProcessor(KafkaConfiguration configuration) {
        this.kafkaConfiguration = configuration;
        this.engine =new MatchingEngine(configuration);
        consumer = createConsumer(kafkaConfiguration);
        this.processedOrdersCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();
    }

    public void start() {
        log.info("Starting consumer");
        try {
            consumer.subscribe(Collections.singletonList(MATCHING_REQUEST_TOPIC));
            while (true) {
                // we want very low polling latency
                var records = consumer.poll(Duration.ofMillis(1));
                for (ConsumerRecord<String, MatchingOrder> record : records) {
                    MatchingOrder order = record.value();
                    if (order == null) {
                        // see poison pill handling in deserializer
                        continue;
                    }
                    processOrder(order);
                }
                // Commit the offsets after processing each batch of messages
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }

    private void processOrder(MatchingOrder order) {
        /*
            this does not guarantee idempotency but meant for optimization and removing duplicates
            because kafka is at least once delivery. Our order logic must handle matches idempotently
            Also in case of incidents the cache might be cleared
        */
        if (processedOrdersCache.getIfPresent(order.getId()) == null) {
            processedOrdersCache.put(order.getId(), true);
            if (MatchingType.BUY.equals(order.getType())) {
                engine.acceptBuyOrder(order);
            } else if (MatchingType.SELL.equals(order.getType())) {
                engine.acceptSellOrder(order);
            }
        }
    }

    private KafkaConsumer<String, MatchingOrder> createConsumer(KafkaConfiguration kafkaConfiguration) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBOOTSTRAP_SERVERS());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfiguration.getGROUP_ID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MatchingOrderDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(props);
    }
}
