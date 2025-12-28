//package com.ecommerce.catalogservice;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class MessageConsumer {
//    @RabbitListener(queues = "myQueue")
//    public void receiveMessage(String message) {
//        System.out.println("from order service: " + message);
//        log.info("from service1: {}",message);
//    }
//}
