package com.hussainkarafallah.order.service;

import java.math.BigDecimal;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.PriceBookEntry;
import com.hussainkarafallah.order.repository.PriceBookEntryRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteTrade {
    private final PriceBookEntryRepository priceBookEntryRepository;

    private final ITransactionalKafkaMes

    public void executeTrade(Instrument instrument, BigDecimal price, Long firstTraderId , Long secondTraderId){
        priceBookEntryRepository.save(new PriceBookEntry(instrument, price));
    }
}
