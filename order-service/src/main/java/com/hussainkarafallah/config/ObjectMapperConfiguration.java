package com.hussainkarafallah.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hussainkarafallah.utils.ExceptionUtils;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfiguration {
    private final static ObjectMapper objectMapper = createObjectMapper();

    public static byte[] toBytes(Object obj){
        return ExceptionUtils.doUnchecked(() -> objectMapper.writeValueAsBytes(obj));
    }

    public static <T> T fromBytes(byte[] bytes, TypeReference<T> ref){
        return ExceptionUtils.doUnchecked(() -> objectMapper.readValue(bytes, ref));
    }

    private static ObjectMapper createObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }


}
