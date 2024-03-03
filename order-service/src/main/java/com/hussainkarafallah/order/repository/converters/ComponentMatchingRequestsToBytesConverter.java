package com.hussainkarafallah.order.repository.converters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.hussainkarafallah.order.domain.Order.ComponentMatchingRequest;

import org.springframework.core.convert.converter.Converter;

public class ComponentMatchingRequestsToBytesConverter implements Converter<List<ComponentMatchingRequest>, byte[]> {

    @Override
    public byte[] convert(List<ComponentMatchingRequest> matchingRequests) {
        if (matchingRequests == null || matchingRequests.isEmpty()) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(matchingRequests);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting component matching requests to byte array", e);
        }
    }
}
