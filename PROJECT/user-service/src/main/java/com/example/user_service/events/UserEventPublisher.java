package com.example.user_service.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import static com.example.user_service.config.RabbitMQConfig.*;

@Component
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserEvent(String event, String userId, String fullName, String email, boolean active) {
        UserEventMessage message = new UserEventMessage(event, userId, fullName, email, active);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        System.out.println("Sent user event to RabbitMQ: " + message);
    }
}
