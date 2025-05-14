package com.tfg.tfg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // ✅ Este es el endpoint WebSocket
                .setAllowedOrigins("*")  // ✅ Permitir conexiones desde cualquier origen
                .withSockJS();  // ✅ Habilitar soporte para navegadores antiguos
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");  // ✅ Mensajes serán enviados a "/topic"
        registry.setApplicationDestinationPrefixes("/app");  // ✅ Prefijo para mensajes enviados desde frontend
    }
}
