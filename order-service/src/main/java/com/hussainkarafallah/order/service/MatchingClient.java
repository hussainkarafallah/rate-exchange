package com.hussainkarafallah.order.service;

import java.util.UUID;

public interface MatchingClient {

    void cancelRequest(UUID requestId);

}
