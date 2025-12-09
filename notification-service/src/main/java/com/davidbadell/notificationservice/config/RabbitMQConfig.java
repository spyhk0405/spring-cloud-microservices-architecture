package com.davidbadell.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(10);
        return factory;
    }
}
