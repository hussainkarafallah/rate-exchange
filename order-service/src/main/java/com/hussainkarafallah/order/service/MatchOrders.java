package com.hussainkarafallah.order.service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.FulfillOrderCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchOrders {

    private final OrderRepository orderRepository;

    private final FulfillOrder fulfillOrder;

    @Transactional
    public void match(FulfillmentMatchedEvent event){
        Order buyOrder = orderRepository.findById(event.getBuyOrderId()).orElseThrow();
        Order sellOrder = orderRepository.findById(event.getSellOrderId()).orElseThrow();
        var buyOrderFulfillment = buyOrder.getFulfillments().stream()
            .filter(x -> x.getId().equals(event.getBuyFulfillmentId())).findAny().orElseThrow();
        var sellOrderFulfillment = sellOrder.getFulfillments().stream()
            .filter(x -> x.getId().equals(event.getSellFulfillmentId())).findAny().orElseThrow();
        
        if(buyOrderFulfillment.getState().equals(FulfillmentState.NOT_COMPLETED) && sellOrderFulfillment.getState().equals(FulfillmentState.NOT_COMPLETED)){

            fulfillOrder.exec(FulfillOrderCommand.builder()
                .order(buyOrder)
                .fulfillment(buyOrderFulfillment)
                .fulfillerId(sellOrder.getId())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .build()
            );

            fulfillOrder.exec(FulfillOrderCommand.builder()
                .order(sellOrder)
                .fulfillment(sellOrderFulfillment)
                .fulfillerId(buyOrder.getId())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .build()
            );

            
        }


    }


}
