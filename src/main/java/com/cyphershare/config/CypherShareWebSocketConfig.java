package com.cyphershare.config;

import com.cyphershare.controller.ChunkWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@SuppressWarnings("all")
public class CypherShareWebSocketConfig implements WebSocketConfigurer {

    private final ChunkWebSocketHandler chunkWebSocketHandler;

    public CypherShareWebSocketConfig(ChunkWebSocketHandler chunkWebSocketHandler) {
        this.chunkWebSocketHandler = chunkWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chunkWebSocketHandler, "/ws/relay")
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Set max binary message size to 10MB (frontend sends chunks of 5MB)
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);
        return container;
    }
}
