package com.ens.hhparser5.service;

import com.ens.hhparser5.utility.ClockHolder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitConnector rabbitConnector;
    @Autowired
    private Queue myQueue;
    @Autowired
    private TopicExchange exchange;


    public void mockRabbitSend(){
        rabbitTemplate.convertAndSend(myQueue.getName(), "Hello, world! Тест кириллицы. "+ Instant.now(ClockHolder.getClock()));
    }
    public void mockRabbitReceive() throws IOException, TimeoutException {
        try {
            rabbitConnector.rabbitReceive();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // отправляет сообщение в очередь по-умолчанию
    public void rabbitSend(String message) {
        rabbitTemplate.convertAndSend(myQueue.getName(), message);
    }

    // отправляет сообщение в exchange
    public void rabbitSendToExchange(String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange.getName(), routingKey, message);
    }

}
