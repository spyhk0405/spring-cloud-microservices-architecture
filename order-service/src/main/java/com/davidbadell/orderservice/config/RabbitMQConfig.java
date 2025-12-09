package com.davidbadell.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    @Value("${rabbitmq.exchange.order:order-exchange}")
    private String orderExchange;
    
    @Value("${rabbitmq.queue.notification:notification-queue}")
    private String notificationQueue;
    
    @Value("${rabbitmq.routing-key.order-created:order.created}")
    private String orderCreatedRoutingKey;
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueue)
                .withArgument("x-dead-letter-exchange", orderExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "order.dead")
                .build();
    }
    
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(orderExchange)
                .with("order.*");
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
