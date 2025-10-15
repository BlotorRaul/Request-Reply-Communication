package com.example.user_service.events;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserEventPublisher {

	private final AmqpTemplate amqpTemplate;

	@Value("${app.rabbitmq.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routingKey}")
	private String routingKey;

	public UserEventPublisher(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public void publishUserCreatedEvent(String userId, String email) {
		Map<String, Object> message = new HashMap<>();
		message.put("event", "USER_CREATED");
		message.put("userId", userId);
		message.put("email", email);
		//trimite mesajul message catre exchange-ul user.exchange folosind eticheta user.created
		amqpTemplate.convertAndSend(exchange, routingKey, message);
		System.out.println("Sent USER_CREATED event for userId = " + userId);
	}
}
