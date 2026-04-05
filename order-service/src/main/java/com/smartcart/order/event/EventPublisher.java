package com.smartcart.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EventPublisher {

    public static final String ORDER_TOPIC = "smartcart.orders";
    public static final String WISHLIST_TOPIC = "smartcart.wishlist";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final boolean kafkaEnabled;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = isKafkaAvailable();
    }

    public void publishOrderCreated(OrderEvents.OrderCreatedEvent event) {
        if (!kafkaEnabled) {
            log.warn("Kafka disabled — skipping ORDER_CREATED event for order {}", event.getOrderId());
            return;
        }
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ORDER_TOPIC, String.valueOf(event.getUserId()), event);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish ORDER_CREATED event for order {}: {}", event.getOrderId(), ex.getMessage());
                } else {
                    log.info("Published ORDER_CREATED event: orderId={}, partition={}, offset={}",
                            event.getOrderId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing ORDER_CREATED event: {}", e.getMessage());
        }
    }

    public void publishWishlistEvent(OrderEvents.WishlistEvent event) {
        if (!kafkaEnabled) {
            log.warn("Kafka disabled — skipping WISHLIST_{} event for user {}", event.getAction(), event.getUserId());
            return;
        }
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(WISHLIST_TOPIC, String.valueOf(event.getUserId()), event);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish WISHLIST event for user {}: {}", event.getUserId(), ex.getMessage());
                } else {
                    log.info("Published WISHLIST_{} event: userId={}, productId={}", event.getAction(), event.getUserId(), event.getProductId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing WISHLIST event: {}", e.getMessage());
        }
    }

    private boolean isKafkaAvailable() {
        try {
            kafkaTemplate.getProducerFactory().createProducer().close();
            log.info("Kafka connection established — events ENABLED");
            return true;
        } catch (Exception e) {
            log.warn("Kafka not available — events DISABLED (app will run without Kafka)");
            return false;
        }
    }
}
