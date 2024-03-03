package com.hussainkarafallah.interfaces;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CancelMatchingEvent {
    UUID requestId;
    UUID orderId;
    String instrument;
    String type;
}
