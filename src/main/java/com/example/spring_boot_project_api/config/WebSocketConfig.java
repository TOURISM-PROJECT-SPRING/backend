package com.example.spring_boot_project_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Destination prefix for broadcasting to subscribers (e.g. /topic/admin/bookings, /topic/owner/{id}/bookings)
        config.enableSimpleBroker("/topic");
        // Application destination prefix for messages sent from client to server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS fallback endpoint
        registry.addEndpoint("/ws-tourism")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Native WebSocket endpoint
        registry.addEndpoint("/ws-tourism")
                .setAllowedOriginPatterns("*");
    }
}
