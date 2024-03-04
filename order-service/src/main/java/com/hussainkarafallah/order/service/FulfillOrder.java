package com.hussainkarafallah.order.service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.FulfillOrderCommand;
import com.hussainkarafallah.utils.UuidUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FulfillOrder {

    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    

    @Transactional
    public void exec(FulfillOrderCommand command){
        Order order = command.getOrder();
        Fulfillment fulfillment = command.getFulfillment();

        OrderSnapshot beforeFulfullSnapshot = OrderMapper.toOrderSnapshot(order);

        fulfillment.setState(FulfillmentState.FULFILLED);
        fulfillment.setFulfilledPrice(command.getPrice());
        fulfillment.setFulfilledQuantity(command.getQuantity());
        fulfillment.setFulfullerId(command.getFulfillerId());

        if(fulfillment.getTargetQuantity().compareTo(command.getQuantity()) == 1){
            order.addFulfillment(Fulfillment.newFulfillment(
                UuidUtils.generatePrefixCombUuid(),
                fulfillment.getInstrument(),
                fulfillment.getTargetQuantity().subtract(command.getQuantity()),
                command.getPrice()
            ));
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeFulfullSnapshot, OrderMapper.toOrderSnapshot(order));
    }
}
