package com.hussainkarafallah.order.service;

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

import com.hussainkarafallah.config.KafkaConfiguration;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.order.util.FulfillmentMatchedEventDeserializer;
import com.hussainkarafallah.order.util.QueueDrainer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingEventProcessor {
    
    private static int NUM_THREADS = 10;
    private static int max_capacity = 1000000;

    private final KafkaConsumer<String, FulfillmentMatchedEvent> consumer;
    private final ExecutorService executorService;
    private final List<BlockingQueue<FulfillmentMatchedEvent>> queues;

    public MatchingEventProcessor(KafkaConfiguration kafkaConfiguration, MatchOrders matchOrders) {
        consumer = createConsumer(kafkaConfiguration);
        consumer.subscribe(Collections.singletonList(KafkaTopics.FULFILLMENT_MATCHED));
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        queues = new ArrayList<>();
        for(int i = 0 ; i < NUM_THREADS ; i++){
            queues.add(new ArrayBlockingQueue<>(max_capacity));
            executorService.submit(QueueDrainer.queueDrainer(queues.get(i) , matchOrders::match));
        }
        new Thread(this::consumeMessages).start();
    }

    
    private void consumeMessages() {
        log.info("Starting matching requests consumer");
        try {
            while (true) {
                // we want very low polling latency
                var records = consumer.poll(Duration.ofMillis(1));
                for (ConsumerRecord<String, FulfillmentMatchedEvent> record : records) {
                    FulfillmentMatchedEvent event = record.value();
                    if (event == null) {
                        // see poison pill handling in deserializer
                        continue;
                    }
                    queues.get(record.key().hashCode() % NUM_THREADS).add(event);
                    
                }
                // Commit the offsets after processing each batch of messages
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }


    private KafkaConsumer<String, FulfillmentMatchedEvent> createConsumer(KafkaConfiguration kafkaConfiguration) {
        Properties props = new Properties();
        log.info("suggested group id is {}" , kafkaConfiguration.getGROUP_ID());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBOOTSTRAP_SERVERS());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfiguration.getGROUP_ID());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfiguration.getGROUP_ID() + UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, FulfillmentMatchedEventDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(props);
    }

}
