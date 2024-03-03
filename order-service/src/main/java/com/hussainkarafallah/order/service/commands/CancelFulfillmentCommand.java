package com.hussainkarafallah.order.service.commands;

import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;

import lombok.Value;

@Value
public class CancelFulfillmentCommand {
    UUID orderId;
    UUID fulfillmentId;
    Instrument instrument;
    OrderType type;
}
