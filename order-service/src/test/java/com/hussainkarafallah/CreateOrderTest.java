package com.hussainkarafallah;

import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;

import org.junit.jupiter.api.Test;

class CreateOrderTest extends BaseIntTest{

	@Test
	void createOrderTest() {
		// given
		var createOrderCommand = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.BUY)
			.traderId(123939L)
			.targetQuantity(aDecimal(304))
			.price(Optional.of(aDecimal(0)))
			.build();
		// when
		createOrder.exec(createOrderCommand);
		// then
		awaitMessageSent(OrderUpdateEvent.class, ev -> {
			return ev.getNewState().equals("OPEN")
			&& ev.getSnapshot().getTraderId() == 123939L;
		});
	}

}
