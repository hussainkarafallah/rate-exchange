package com.hussainkarafallah.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transferwise.idempotence4j.autoconfigure.Idempotence4jAutoConfiguration;
import com.transferwise.idempotence4j.core.ActionRepository;
import com.transferwise.idempotence4j.core.DefaultIdempotenceService;
import com.transferwise.idempotence4j.core.IdempotenceService;
import com.transferwise.idempotence4j.core.LockProvider;
import com.transferwise.idempotence4j.core.ResultSerializer;
import com.transferwise.idempotence4j.core.serializers.json.JsonResultSerializer;
import com.transferwise.idempotence4j.postgres.JdbcPostgresActionRepository;
import com.transferwise.idempotence4j.postgres.JdbcPostgresLockProvider;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class IdempotencyConfiguration {
    Idempotence4jAutoConfiguration idempotence4jAutoConfiguration;

    @Bean
    @ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
    public ResultSerializer jsonResultSerializer(ObjectMapper objectMapper) {
        return new JsonResultSerializer(objectMapper);
    }

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

    @Bean
    public IdempotenceService idempotenceService(
        PlatformTransactionManager platformTransactionManager,
        ActionRepository actionRepository,
        LockProvider lockProvider,
        ResultSerializer resultSerializer
    ) {
        return new DefaultIdempotenceService(platformTransactionManager, lockProvider, actionRepository, resultSerializer, m -> {});
    }
}
