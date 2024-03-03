package com.hussainkarafallah.order.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.CancelFulfillmentCommand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelOrder {
    
    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    private final CancelFulfillment cancelFulfillment;
    
    @Transactional
    void cancelOrder(UUID orderId){
        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if(maybeOrder.isEmpty()){
            return;
        }
        Order order = maybeOrder.get();
        OrderSnapshot beforeCancellationSnapshot = OrderMapper.toOrderSnapshot(order);
        //
        List<Fulfillment> cancelledFulfuFulfillments = cancelledFulfillments(order);
        order.setFullfilments(cancelledFulfuFulfillments);
        order.setState(OrderState.CLOSED);
        //
        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeCancellationSnapshot, OrderMapper.toOrderSnapshot(order));
    }


    private List<Fulfillment> cancelledFulfillments(Order order){
        return order.getFulfillments().stream().map(fulfillment -> {
            if(!fulfillment.getState().equals(FulfillmentState.EXECUTED)){
                cancelFulfillment.exec(new CancelFulfillmentCommand(
                    order.getId(),
                    fulfillment.getId(),
                    fulfillment.getInstrument(),
                    order.getOrderType()
                ));
                fulfillment.setState(FulfillmentState.NOT_COMPLETED);
            }
            return fulfillment;
        
        }).toList();
    }
}
