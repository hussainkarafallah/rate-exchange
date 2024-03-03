package com.hussainkarafallah.order.external;

import java.util.UUID;

import com.hussainkarafallah.order.service.MatchingClient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingClientImpl implements MatchingClient{
    
    /*
     * Since we are operating on the same db this works flawlessly
     * In case we are calling another service this should be a gRPC / Rest client
     * Cancellation is idempotent and we must always make sure we can cancel all fulfillment requests
     * before we cancel the order. A message based command would make things very complicated.
     */
    @Override
    @Transactional
    public void cancelRequest(UUID requestId) {
        
    }

}
