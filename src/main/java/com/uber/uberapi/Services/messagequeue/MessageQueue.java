package com.uber.uberapi.Services.messagequeue;

public interface MessageQueue {
    void sendMessage(String topic,MQMessage message);
    MQMessage consumeMessage(String topic);
}
