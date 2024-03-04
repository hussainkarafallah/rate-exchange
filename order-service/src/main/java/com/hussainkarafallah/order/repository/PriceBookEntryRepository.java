package com.hussainkarafallah.order.repository;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.PriceBookEntry;

public interface PriceBookEntryRepository {
    PriceBookEntry findByInstrument(Instrument instrument);
    void save(PriceBookEntry entry);
}