package com.smartcart.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
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
        if (!kafkaEnabled) { log.warn("Kafka disabled - skipping ORDER event"); return; }
        try {
            kafkaTemplate.send(ORDER_TOPIC, String.valueOf(event.getUserId()), event)
                    .whenComplete((r, ex) -> {
                        if (ex != null) log.error("Failed ORDER event: {}", ex.getMessage());
                        else log.info("Published ORDER event: orderId={}", event.getOrderId());
                    });
        } catch (Exception e) { log.error("Error publishing ORDER event: {}", e.getMessage()); }
    }

    public void publishWishlistEvent(OrderEvents.WishlistEvent event) {
        if (!kafkaEnabled) { log.warn("Kafka disabled - skipping WISHLIST event"); return; }
        try {
            kafkaTemplate.send(WISHLIST_TOPIC, String.valueOf(event.getUserId()), event)
                    .whenComplete((r, ex) -> {
                        if (ex != null) log.error("Failed WISHLIST event: {}", ex.getMessage());
                        else log.info("Published WISHLIST_{} event", event.getAction());
                    });
        } catch (Exception e) { log.error("Error publishing WISHLIST event: {}", e.getMessage()); }
    }

    private boolean isKafkaAvailable() {
        try { kafkaTemplate.getProducerFactory().createProducer().close(); log.info("Kafka ENABLED"); return true; }
        catch (Exception e) { log.warn("Kafka DISABLED - app will run without it"); return false; }
    }
}
