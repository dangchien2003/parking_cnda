package com.parking.notification_service.configuration;

import com.parking.notification_service.service.ws.DataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getDataHandler(), "/websocket/data").setAllowedOrigins("*");
        registry.addHandler(getDataHandler(), "/websocket/data1").setAllowedOrigins("*");
    }

    @Bean
    DataHandler getDataHandler() {
        return new DataHandler();
    }
}
