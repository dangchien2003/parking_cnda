package com.parking.notification_service.service.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DataHandler extends TextWebSocketHandler {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        executorService.submit(() -> {
            try {
                log.info("message: {}", message.getPayload());
                // Giả lập công việc kéo dài
                Thread.sleep(3000);
                session.sendMessage(new TextMessage("hello"));
            } catch (IOException | InterruptedException e) {
                log.error("Error handling message: {}", e.getMessage());
            }
        });
    }
}
