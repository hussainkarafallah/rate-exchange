package com.hussainkarafallah.order.service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteFulfillments {
    
    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    @Transactional
    void tryExecute(Order order){
        if(!order.canExecute()){
            return;
        }

        for(Fulfillment fulfillment : order.getFulfillments()){
            
            if(!fulfillment.getState().equals(FulfillmentState.FULFILLED)){
                continue;
            }

            executeFulfillment(order, fulfillment);
            
            Order matchingOrder = orderRepository.findById(fulfillment.getFulfillerId()).orElseThrow();
            Fulfillment matchingFulfillment = matchingOrder.getFulfillments().stream()
                .filter(f -> order.getId().equals(f.getFulfillerId()) && fulfillment.getInstrument().equals(f.getInstrument()))
                .findAny()
                .orElseThrow();
            executeFulfillment(matchingOrder, matchingFulfillment);

        }

    }

    private void executeFulfillment(Order order , Fulfillment fulfillment){
        OrderSnapshot beforeExecutiOrderSnapshot = OrderMapper.toOrderSnapshot(order);

        fulfillment.setState(FulfillmentState.EXECUTED);
        if(order.canBeClosed()){
            order.setState(OrderState.CLOSED);
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeExecutiOrderSnapshot, OrderMapper.toOrderSnapshot(order));
    }



}
