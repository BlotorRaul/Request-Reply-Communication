package com.example.device_service.events;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserEventConsumer {

	@RabbitListener(queues = "${app.rabbitmq.queue}")
	public void handleUserEvent(Map<String, Object> message) {
		System.out.println("Received event from user-service: " + message);

		if ("USER_CREATED".equals(message.get("event"))) {
			System.out.println(" New user registered: " + message.get("email"));
		}
	}
}
