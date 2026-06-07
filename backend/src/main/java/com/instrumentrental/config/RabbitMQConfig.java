package com.instrumentrental.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue reminderQueue() {
        return new Queue("notification.reminder", true);
    }

    @Bean
    public Queue overdueQueue() {
        return new Queue("notification.overdue", true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Binding bindReminder(Queue reminderQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(reminderQueue).to(notificationExchange).with("notification.reminder");
    }

    @Bean
    public Binding bindOverdue(Queue overdueQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(overdueQueue).to(notificationExchange).with("notification.overdue");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}