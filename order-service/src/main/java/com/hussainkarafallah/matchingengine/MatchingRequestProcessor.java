package com.hussainkarafallah.matchingengine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hussainkarafallah.config.KafkaConfiguration;
import com.hussainkarafallah.matchingengine.domain.MatchingOrder;
import com.hussainkarafallah.matchingengine.domain.MatchingOrderDeserializer;
import com.hussainkarafallah.messaging.KafkaTopics;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchingRequestProcessor {

    private final KafkaConsumer<String, MatchingOrder> consumer;
    private final Cache<UUID, Boolean> processedOrdersCache;

    private static int NUM_THREADS = 10;
    private static int max_capacity = 1000000;
    private final ExecutorService executorService;
    private final List<BlockingQueue<MatchingOrder>> queues;
    private final KafkaProducer<String , byte[]> kafkaProducer;

    public MatchingRequestProcessor(KafkaConfiguration kafkaConfiguration) {
        consumer = createConsumer(kafkaConfiguration);
        consumer.subscribe(Collections.singletonList(KafkaTopics.MATCHING_REQUEST_TOPIC));
        this.processedOrdersCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();
        this.kafkaProducer = createProducer(kafkaConfiguration);
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        queues = new ArrayList<>();
        for(int i = 0 ; i < NUM_THREADS ; i++){
            queues.add(new ArrayBlockingQueue<>(max_capacity));
            executorService.submit(new MatchingEngine(kafkaProducer, queues.get(i)));
        }
        new Thread(this::consumeMessages).start();
    }

    private void consumeMessages() {
        log.info("Starting matching requests consumer");
        try {
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
            queues.get(order.getInstrument().hashCode() % NUM_THREADS).add(order);
        }
    }

    private KafkaConsumer<String, MatchingOrder> createConsumer(KafkaConfiguration kafkaConfiguration) {
        Properties props = new Properties();
        log.info("suggested group id is {}" , kafkaConfiguration.getGROUP_ID());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBOOTSTRAP_SERVERS());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfiguration.getGROUP_ID());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfiguration.getGROUP_ID() + UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MatchingOrderDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(props);
    }

    private KafkaProducer<String , byte[]> createProducer(KafkaConfiguration configuration){
        Properties props = new Properties();
        props.put("bootstrap.servers", configuration.getBOOTSTRAP_SERVERS());
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", ByteArraySerializer.class.getName());
        // Acknowledge the leader broker after writing to its local log
        props.put("acks", "1"); 
        // Set additional configurations for low latency
        props.put("linger.ms", "1"); // Wait at most 1 ms before sending a batch
        props.put("compression.type", "none"); // Disable compression for lower latency
        props.put("max.in.flight.requests.per.connection", "1"); // Send only one request at a time per connection
        return new KafkaProducer<>(props);
    }
}
