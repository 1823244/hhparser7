package com.ens.hhparser5.service;

import com.ens.hhparser5.configuration.RabbitConfig;
import com.ens.hhparser5.exceptions.HhparserException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Component
public class RabbitConnector {

    // лимит итераций получения сообщений (если вдруг что-то зависнет)
    final private int SAFE_COUNTER_LIMIT = 100000;
    private final Logger logger = LoggerFactory.getLogger(RabbitConnector.class);

    @Autowired
    private RabbitConfig rabbitConfig;

    @Autowired
    CachingConnectionFactory cachingConnectionFactory;



    public List<String> rabbitReceive() throws HhparserException {

        List<String> returnValue = new ArrayList<>();
        boolean autoAck = true;
        logger.info("started to poll messages");
        int safeCounter = 0;
        int messagesCount = 0;

        // do not use try-with-resources
        // https://www.rabbitmq.com/tutorials/tutorial-one-java.html
        // quote: "Why don't we use a try-with-resource statement to automatically
        // close the channel and the connection? By doing so we would simply make
        // the program move on, close everything, and exit! This would be awkward
        // because we want the process to stay alive while the consumer is listening
        // asynchronously for messages to arrive."
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitConfig.getRABBIT_HOST());
            factory.setPort(rabbitConfig.getRABBIT_PORT());
            factory.setUsername(rabbitConfig.getRABBIT_USERNAME());
            factory.setPassword(rabbitConfig.getRABBIT_PASSWORD());
            factory.setVirtualHost(rabbitConfig.getRABBIT_VIRTUALHOST());
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclarePassive(rabbitConfig.getQUEUE_NAME());

            GetResponse response = channel.basicGet(rabbitConfig.getQUEUE_NAME(),autoAck);

            while(response != null) {
                if (safeCounter > SAFE_COUNTER_LIMIT) break;
                String messageText = new String(response.getBody(), "UTF-8");
                returnValue.add(messageText);
                //logger.info("RabbitMQ received message: {}", messageText);
                response = channel.basicGet(rabbitConfig.getQUEUE_NAME(),autoAck);
                safeCounter+=1;
                messagesCount+=1;
            }
            response = null;
            channel.close();
            connection.close();
            cachingConnectionFactory.destroy();
            channel = null;
            connection = null;
            factory = null;

            logger.info("finished to poll messages. total: {}", messagesCount);
        } catch (UnsupportedEncodingException e) {
            throw new HhparserException(e);
        } catch (Exception e) {
            throw new HhparserException(e);
        }

        return returnValue;
    }

/*    @RabbitListener(queues = {"${queue.name}"})
    private void receive(@Payload String fileBody) {
        logger.info("Message " + fileBody);
    }*/

}
