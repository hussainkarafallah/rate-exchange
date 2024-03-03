package com.hussainkarafallah.order.repository.converters;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hussainkarafallah.config.ObjectMapperConfiguration;
import com.hussainkarafallah.order.domain.Order.ComponentMatchingRequest;

import org.springframework.core.convert.converter.Converter;

public class BytesToComponentMatchingRequestsConverter implements Converter<byte[], List<ComponentMatchingRequest>> {

    @Override
    public List<ComponentMatchingRequest> convert(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return ObjectMapperConfiguration.fromBytes(bytes , new TypeReference<List<ComponentMatchingRequest>>() {
        });
    }
}