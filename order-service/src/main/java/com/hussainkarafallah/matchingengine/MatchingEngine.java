package com.hussainkarafallah.matchingengine;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Properties;

import com.hussainkarafallah.config.KafkaConfiguration;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.matchingengine.domain.MatchingOrder;
import com.hussainkarafallah.messaging.KafkaTopics;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.util.Assert;

public class MatchingEngine {
    private final PriorityQueue<MatchingOrder> sellOrdersPool;
    private final LinkedList<MatchingOrder> pendingBuyOrders = new LinkedList<>();
    private final LinkedList<MatchingOrder> sellOrdersWithNoPrice = new LinkedList<>();
    private final KafkaProducer<String, byte[]> producer;

    public MatchingEngine(KafkaConfiguration kafkaConfiguration){
        sellOrdersPool = new PriorityQueue<>(Comparator.comparing(MatchingOrder::getPrice).thenComparing(MatchingOrder::getOrderId));
        producer = createProducer(kafkaConfiguration);
    }

    public void acceptBuyOrder(MatchingOrder buyOrder) {
        // first we try to match with the best price possible
        if (!sellOrdersPool.isEmpty() && sellOrdersPool.peek().getPrice().compareTo(buyOrder.getPrice()) <= 0) {
            match(buyOrder , sellOrdersPool.poll());
        }
        // we try to match with a sell order with no price 
        else if(!sellOrdersWithNoPrice.isEmpty()) {
            match(buyOrder , sellOrdersWithNoPrice.pollFirst());
        }
        // we could not match and we add to our waiting list
        else {
            pendingBuyOrders.add(buyOrder);
        }
    }

    public void acceptSellOrder(MatchingOrder sellOrder) {
        Iterator<MatchingOrder> iterator = pendingBuyOrders.iterator();
        // a sell order must always go to the pool unless we have pending buy orders
        while (iterator.hasNext()) {
            MatchingOrder buyOrder = iterator.next();
            if (sellOrder.getPrice() == null || buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0) {
                match(buyOrder, sellOrder);
                iterator.remove();
                break;
            }
        }
        if(sellOrder.getPrice() == null){
            sellOrdersWithNoPrice.add(sellOrder);
        }
        else{
            sellOrdersPool.add(sellOrder);
        }
    }

    private void match(MatchingOrder buyOrder, MatchingOrder sellOrder){
        Assert.isTrue(buyOrder.getType().equals(MatchingType.BUY) , () -> "first parameter is buy order");
        Assert.isTrue(buyOrder.getType().equals(MatchingType.SELL) , () -> "second parameter is sell order");
        BigDecimal price = sellOrder.getPrice() == null ? buyOrder.getPrice() : sellOrder.getPrice();
        FulfillmentMatchedEvent event = FulfillmentMatchedEvent.builder()
            .buyFulfillmentId(buyOrder.getId())
            .buyOrderId(buyOrder.getOrderId())
            .sellFulfillmentId(sellOrder.getId())
            .sellOrderId(sellOrder.getOrderId())
            .quantity(buyOrder.getQuantity().min(sellOrder.getQuantity()))
            .price(price)
            .build();
        ProducerRecord<String,byte[]> record = new ProducerRecord<>(
            KafkaTopics.FULFILLMENT_MATCHED,
            buyOrder.getId().toString(),
            toBytes(event)
        );
        producer.send(record);
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
