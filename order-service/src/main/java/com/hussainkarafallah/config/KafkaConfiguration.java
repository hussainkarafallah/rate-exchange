package com.hussainkarafallah.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class KafkaConfiguration {

    @Value("${tw-tkms.kafka.bootstrap.servers}")
    private String BOOTSTRAP_SERVERS;

    @Value("${spring.application.name}")
    private String GROUP_ID;

}
