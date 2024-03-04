package com.hussainkarafallah;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;

import org.junit.jupiter.api.Test;

public class MatchingTests extends BaseIntTest {
    @Test
    void testSimpleMatching(){
        // given
		var cmd1 = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.BUY)
			.traderId(123939L)
			.targetQuantity(aDecimal(304))
			.price(Optional.of(aDecimal(100)))
			.build();
        var cmd2 = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.SELL)
			.traderId(823783L)
			.targetQuantity(aDecimal(500))
			.price(Optional.of(aDecimal(12)))
			.build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
			RequestMatchingEvent.class,
			ev-> ev.getInstrument().equals("IRNMDN") &&
				ev.getType().equals("BUY")
		);
        awaitMessageSent(
			RequestMatchingEvent.class,
			ev->  ev.getInstrument().equals("IRNMDN") &&
				ev.getType().equals("SELL")
		);
        var match = awaitMessageReceived(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(12) , match.getPrice());
        assertEquals(aDecimal(304), match.getQuantity());
    }

    @Test
    void buyOrderWithNoPriceMatches(){
        // given
		var cmd1 = CreateOrderCommand.builder()
        .idempotencyUuid(UUID.randomUUID())
        .instrument(Instrument.MGDTH)
        .orderType(OrderType.BUY)
        .traderId(123939L)
        .targetQuantity(aDecimal(39))
        .price(Optional.empty())
        .build();
        var cmd2 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MGDTH)
            .orderType(OrderType.SELL)
            .traderId(823783L)
            .targetQuantity(aDecimal(22))
            .price(Optional.of(aDecimal(1700)))
            .build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev-> ev.getInstrument().equals("MGDTH") &&
                ev.getType().equals("BUY")
        );
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev->  ev.getInstrument().equals("MGDTH") &&
                ev.getType().equals("SELL")
        );
        var match = awaitMessageReceived(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(1700) , match.getPrice());
        assertEquals(aDecimal(22), match.getQuantity());
    }


    @Test
    void sellOrderWithNoPriceMatches(){
        // given
		var cmd1 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MTLCA)
            .orderType(OrderType.BUY)
            .traderId(123939L)
            .targetQuantity(aDecimal(304))
            .price(Optional.of(aDecimal(100)))
            .build();
        var cmd2 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MTLCA)
            .orderType(OrderType.SELL)
            .traderId(823783L)
            .targetQuantity(aDecimal(500))
            .price(Optional.empty())
            .build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev-> ev.getInstrument().equals("MTLCA") &&
                ev.getType().equals("BUY")
        );
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev->  ev.getInstrument().equals("MTLCA") &&
                ev.getType().equals("SELL")
        );
        var match = awaitMessageReceived(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(100) , match.getPrice());
        assertEquals(aDecimal(304), match.getQuantity());
    }
}
