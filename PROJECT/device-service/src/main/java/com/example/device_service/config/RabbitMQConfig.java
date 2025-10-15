package com.example.device_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Value("${app.rabbitmq.exchange}")
	private String exchangeName;

	@Value("${app.rabbitmq.queue}")
	private String queueName;

	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(exchangeName);
	}

	@Bean
	public Queue queue() {
		return new Queue(queueName, true);
	}
//daca in exchange-ul user.exchange vine un mesaj cu routingKey = user.created, trimite-l in coada device.user.queue
	//Sau
	//Leaga coada device.user.queue cu exchange-ul user.exchange si primeste doar mesajele cu routingKey = user.created
	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("user.created");
	}
}
