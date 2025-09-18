package com.ens.hhparser5.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.stereotype.Component;

@Component
@EnableRabbit
public class RabbitListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        System.out.println(message.getBody());
    }
}
