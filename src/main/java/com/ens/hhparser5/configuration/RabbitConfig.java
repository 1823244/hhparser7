package com.ens.hhparser5.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@EnableRabbit
public class RabbitConfig {
    @Value("${queue.name}")
    private String QUEUE_NAME;
    @Value("${spring.rabbitmq.host}")
    private String RABBIT_HOST;
    @Value("${spring.rabbitmq.port}")
    private int RABBIT_PORT;
    @Value("${spring.rabbitmq.username}")
    private String RABBIT_USERNAME;
    @Value("${spring.rabbitmq.virtualHost}")
    private String RABBIT_VIRTUALHOST;
    @Value("${spring.rabbitmq.password}")
    private String RABBIT_PASSWORD;

    @Autowired
    private AppConfig appConfig;

    @Bean
    public Queue myQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public Queue queue1S(){return new Queue("hhparser5-1S", true);}
    @Bean
    public Queue queueJava(){return new Queue("hhparser5-java", true);}

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("hhparser5");
    }

    public String getQUEUE_NAME() {
        return QUEUE_NAME;
    }

    public String getRABBIT_HOST() {
        return RABBIT_HOST;
    }

    public int getRABBIT_PORT() {
        return RABBIT_PORT;
    }

    public String getRABBIT_USERNAME() {
        return RABBIT_USERNAME;
    }

    public String getRABBIT_VIRTUALHOST() {
        return RABBIT_VIRTUALHOST;
    }

    public String getRABBIT_PASSWORD() {
        return RABBIT_PASSWORD;
    }
//
//    @Bean
//    public MessageListenerContainer messageListenerContainer(CachingConnectionFactory connectionFactory){
//
//        SimpleMessageListenerContainer s = new SimpleMessageListenerContainer();
//        s.setConnectionFactory(connectionFactory);
//        if (!appConfig.isEnableRabbitListener()) {
//            return s;
//        }
//
//        s.setQueues(queue1S(), queueJava());
//        s.setMessageListener(new RabbitListener());
//        return s;
//    }
//
//    @Bean
//    public CachingConnectionFactory connectionFactory (){
//        if (!appConfig.isEnableRabbitListener()) {
//            return new CachingConnectionFactory();
//        }
//        CachingConnectionFactory factory = new CachingConnectionFactory(getRABBIT_HOST());
//        factory.setPort(getRABBIT_PORT());
//        factory.setUsername(getRABBIT_USERNAME());
//        factory.setPassword(getRABBIT_PASSWORD());
//        factory.setVirtualHost(getRABBIT_VIRTUALHOST());
//        return factory;
//    }

}
