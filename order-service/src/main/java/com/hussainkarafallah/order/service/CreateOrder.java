package com.hussainkarafallah.order.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hussainkarafallah.order.IdempotentActions;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;
import com.hussainkarafallah.utils.UuidUtils;
import com.transferwise.idempotence4j.core.ActionId;
import com.transferwise.idempotence4j.core.IdempotenceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;;


@Service
@RequiredArgsConstructor
public class CreateOrder {


    private final IdempotenceService idempotenceService;

    private final OrderRepository orderRepository;

    private final BroadcastOrder broadcastOrder;

    @Transactional
    public void exec(CreateOrderCommand command){
        // since prefixCombUUid will be unique rest of parameters are not critical but just respecting library contract
        ActionId actionId = new ActionId(
            command.getIdempotencyUuid(),
            IdempotentActions.CREATE_ORDER.name(),
            command.getTraderId().toString()
        );
        idempotenceService.execute(actionId, () -> createOrder(command), new TypeReference<Order>() {});
    }

    private Order createOrder(CreateOrderCommand command){
        Order order = Order.newOrderBuilder()
            .id(UuidUtils.generatePrefixCombUuid())
            .instrument(command.getInstrument())
            .orderType(command.getOrderType())
            .targetQuantity(command.getTargetQuantity())
            .price(command.getPrice().orElse(null))
            .traderId(command.getTraderId())
            .build();
        orderRepository.save(order);
        broadcastOrder.onOrderCreated(order);
        return order;
    }

}
