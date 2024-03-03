package com.hussainkarafallah;

import com.transferwise.idempotence4j.core.ActionRepository;
import com.transferwise.idempotence4j.core.LockProvider;
import com.transferwise.idempotence4j.postgres.JdbcPostgresActionRepository;
import com.transferwise.idempotence4j.postgres.JdbcPostgresLockProvider;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ActionRepository postgresActionRepository(DataSource dataSource) {
        return new JdbcPostgresActionRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    @Primary
    public LockProvider postgresLockProvider(DataSource dataSource) {
        return new JdbcPostgresLockProvider(new JdbcTemplate(dataSource));
    }
}
