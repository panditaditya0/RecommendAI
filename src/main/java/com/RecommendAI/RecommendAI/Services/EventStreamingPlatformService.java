package com.RecommendAI.RecommendAI.Services;

public interface EventStreamingPlatformService {
    void sendMessage(String topic, String message);
}