package com.hussainkarafallah.order.repository;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.PriceBookEntry;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PriceBookEntryJdbcRepository implements PriceBookEntryRepository {

    private final JdbcTemplate jdbcTemplate;

    public PriceBookEntryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<PriceBookEntry> rowMapper = (rs, rowNum) -> new PriceBookEntry(
            Instrument.valueOf(rs.getString("instrument")),
            rs.getBigDecimal("price")
    );

    @Override
    public PriceBookEntry findByInstrument(Instrument instrument) {
        String sql = "SELECT * FROM price_book WHERE instrument = '%s'".formatted(instrument.toString());
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }

    @Override
    public void save(PriceBookEntry entry) {
        String sql = "INSERT INTO price_book (instrument, price) VALUES (?, ?) ON CONFLICT (instrument) DO UPDATE SET price = EXCLUDED.price";
        jdbcTemplate.update(sql, entry.getInstrument().name(), entry.getPrice());
    }
}