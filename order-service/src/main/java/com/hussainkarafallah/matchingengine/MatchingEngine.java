package com.hussainkarafallah.matchingengine;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.toBytes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;

import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.matchingengine.domain.MatchingOrder;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.utils.UuidUtils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingEngine implements Runnable {
    private final PriorityQueue<MatchingOrder> sellOrdersPool;
    private final PriorityQueue<MatchingOrder> pendingBuyOrders;
    private final PriorityQueue<MatchingOrder> sellOrdersWithNoPrice;
    private final KafkaProducer<String, byte[]> producer;
    private final BlockingQueue<MatchingOrder> queue;

    public MatchingEngine(KafkaProducer<String, byte[]> producer, BlockingQueue<MatchingOrder> queue){
        Comparator<MatchingOrder> orderByOrderId = Comparator.comparing(MatchingOrder::getOrderId);
        sellOrdersPool = new PriorityQueue<>(Comparator.comparing(MatchingOrder::getPrice).thenComparing(MatchingOrder::getOrderId));
        pendingBuyOrders = new PriorityQueue<>(orderByOrderId);
        sellOrdersWithNoPrice = new PriorityQueue<>(orderByOrderId);
        this.producer = producer;
        this.queue = queue;
    }

    @Override
    public void run() {
        while(true){
            List<MatchingOrder> orders = new ArrayList<>();
            queue.drainTo(orders);
            orders.forEach(order -> {
                log.info("Engine:: Accepted order into the engine {} , {} , {}", order.getId(), order.getType(), order.getInstrument());
                if (MatchingType.BUY.equals(order.getType())) {
                    acceptBuyOrder(order);
                } else if (MatchingType.SELL.equals(order.getType())) {
                    acceptSellOrder(order);
                }
            });
        }
    }


    public void acceptBuyOrder(MatchingOrder buyOrder) {
        // first we try to match with the best price possible
        if (!sellOrdersPool.isEmpty() && canMatch(buyOrder, sellOrdersPool.peek())) {
            match(buyOrder , sellOrdersPool.poll());
        }
        // we try to match with a sell order with no price 
        else if(!sellOrdersWithNoPrice.isEmpty()) {
            match(buyOrder , sellOrdersWithNoPrice.poll());
        }
        // we could not match and we add to our waiting list
        else {
            log.info("Engine:: added buy order {} to pending queue" , buyOrder.getId());
            pendingBuyOrders.add(buyOrder);
        }
    }

    public void acceptSellOrder(MatchingOrder sellOrder) {
        Iterator<MatchingOrder> iterator = pendingBuyOrders.iterator();
        // a sell order must always go to the pool unless we have pending buy orders
        while (iterator.hasNext()) {
            MatchingOrder buyOrder = iterator.next();
            if (canMatch(buyOrder, sellOrder)) {
                match(buyOrder, sellOrder);
                iterator.remove();
                break;
            }
        }
        if(sellOrder.getPrice() == null){
            log.info("Engine:: added sell order {} to no price queue" , sellOrder.getId());
            sellOrdersWithNoPrice.add(sellOrder);
        }
        else{
            log.info("Engine:: added sell order {} to pool" , sellOrder.getId());
            sellOrdersPool.add(sellOrder);
        }
    }

    private boolean canMatch(MatchingOrder buyOrder, MatchingOrder sellOrder){
        log.info("Engine:: testing matching {} , {}" , buyOrder.getId() , sellOrder.getId());
        if(buyOrder.getPrice() == null && sellOrder.getPrice() == null){
            return false;
        }
        if(buyOrder.getPrice() == null || sellOrder.getPrice() == null){
            return true;
        }
        return buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0;
    }

    private void match(MatchingOrder buyOrder, MatchingOrder sellOrder){
        log.info("Engine:: Matched {} , {}" , buyOrder.getId() , sellOrder.getId());
        BigDecimal price = sellOrder.getPrice() == null ? buyOrder.getPrice() : sellOrder.getPrice();
        FulfillmentMatchedEvent event = FulfillmentMatchedEvent.builder()
            .matchId(UuidUtils.generatePrefixCombUuid())
            .buyFulfillmentId(buyOrder.getId())
            .buyOrderId(buyOrder.getOrderId())
            .sellFulfillmentId(sellOrder.getId())
            .sellOrderId(sellOrder.getOrderId())
            .quantity(buyOrder.getQuantity().min(sellOrder.getQuantity()))
            .price(price)
            .build();
        ProducerRecord<String,byte[]> record = new ProducerRecord<>(
            KafkaTopics.FULFILLMENT_MATCHED,
            buyOrder.getInstrument().toString(),
            toBytes(event)
        );
        producer.send(record);
    }


    
}
