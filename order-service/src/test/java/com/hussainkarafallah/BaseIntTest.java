package com.hussainkarafallah;

import static com.hussainkarafallah.config.ObjectMapperConfiguration.fromBytes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.messaging.KafkaTopics;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.repository.PriceBookEntryJdbcRepository;
import com.hussainkarafallah.order.service.CreateOrder;
import com.transferwise.idempotence4j.autoconfigure.Idempotence4jAutoConfiguration;
import com.transferwise.kafka.tkms.test.ITkmsSentMessagesCollector;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;

@SpringBootTest
@ContextConfiguration(classes = { TestApplication.class, TestConfig.class, Idempotence4jAutoConfiguration.class })
@ActiveProfiles("test")
public abstract class BaseIntTest {

    @Autowired
    protected CreateOrder createOrder;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected ITkmsSentMessagesCollector tkmsSentMessagesCollector;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Autowired
    protected PriceBookEntryJdbcRepository priceBookRepository;
    
    @BeforeEach
    void init(){
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"orders");
        testKafkaConsumer.reset();
    }

    public static BigDecimal aDecimal(double v) {
        return BigDecimal.valueOf(v);
    }

    protected <T> T awaitMessageSent(Class<T> clazz, Predicate<T> predicate) {
        String topic = CLASS_TO_TOPIC.get(clazz);
        return Awaitility.await().until(
                () -> tkmsSentMessagesCollector.getSentMessages(topic)
                        .stream()
                        .map(msg -> msg.getProducerRecord().value())
                        .map(payload -> fromBytes(payload, clazz))
                        .filter(predicate::test)
                        .findAny(),
                Optional::isPresent
                ).orElseThrow();
    }

    protected <T> T awaitMessageReceived(Class<T> clazz, Predicate<T> predicate){
        String topic = CLASS_TO_TOPIC.get(clazz);
        return Awaitility.await().until(
                () -> testKafkaConsumer.getReceivedMessages(topic)
                        .stream()
                        .map(payload -> fromBytes(payload, clazz))
                        .filter(predicate::test)
                        .findAny(),
                Optional::isPresent
                ).orElseThrow();

    }

    private static final Map<String, Class<?>> TOPIC_TO_CLASS = Map.of(
        KafkaTopics.ORDER_UPDATE_TOPIC, OrderUpdateEvent.class,
        KafkaTopics.MATCHING_REQUEST_TOPIC, RequestMatchingEvent.class,
        KafkaTopics.FULFILLMENT_MATCHED, FulfillmentMatchedEvent.class
    );

    private static final Map<Class<?>, String> CLASS_TO_TOPIC = TOPIC_TO_CLASS.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
}
