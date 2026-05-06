package com.cyphershare.config;

import com.cyphershare.controller.ChunkWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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
}
