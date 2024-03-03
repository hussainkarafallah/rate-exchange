package com.hussainkarafallah;

import java.math.BigDecimal;

import com.hussainkarafallah.order.service.CreateOrder;
import com.transferwise.idempotence4j.autoconfigure.Idempotence4jAutoConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {TestApplication.class, TestConfig.class, Idempotence4jAutoConfiguration.class})
@ActiveProfiles("test")
public abstract class BaseIntTest {

    @Autowired
	protected CreateOrder createOrder;

    public static BigDecimal aDecimal(double v){
        return BigDecimal.valueOf(v);
    }
}
