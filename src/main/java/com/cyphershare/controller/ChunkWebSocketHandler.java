package com.cyphershare.controller;

import com.cyphershare.service.SessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;


import java.net.URI;

@Component
@SuppressWarnings("null")
public class ChunkWebSocketHandler extends AbstractWebSocketHandler {

    private final SessionManager sessionManager;

    public ChunkWebSocketHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            session.close(new CloseStatus(1007, "Invalid URI"));
            return;
        }

        String query = uri.getQuery();
        String code = extractParam(query, "code");
        String role = extractParam(query, "role");

        if (code == null || role == null || !sessionManager.validateSession(code)) {
            session.close(new CloseStatus(1007, "Invalid or expired session"));
            return;
        }

        if ("sender".equalsIgnoreCase(role)) {
            sessionManager.registerSender(code, session);
            System.out.println("Sender connected for code: " + code);
        } else if ("receiver".equalsIgnoreCase(role)) {
            sessionManager.registerReceiver(code, session);
            System.out.println("Receiver connected for code: " + code);
            
            WebSocketSession sender = sessionManager.getSender(code);
            if (sender != null && sender.isOpen()) {
                sender.sendMessage(new TextMessage("RECEIVER_READY"));
            }
        } else {
            session.close(new CloseStatus(1007, "Invalid role"));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        URI uri = session.getUri();
        if (uri == null) return;

        String code = extractParam(uri.getQuery(), "code");
        String role = extractParam(uri.getQuery(), "role");

        if ("sender".equalsIgnoreCase(role)) {
            if (!sessionManager.incrementAndCheckBytes(code, message.getPayloadLength())) {
                session.sendMessage(new TextMessage("ERROR: Data limit exceeded for Basic Tier (2GB max). Upgrade to Business."));
                session.close(new CloseStatus(1009, "Message Too Big"));
                return;
            }

            WebSocketSession receiver = sessionManager.getReceiver(code);
            if (receiver != null && receiver.isOpen()) {
                receiver.sendMessage(message);
            } else {
                session.sendMessage(new TextMessage("ERROR: Receiver not connected"));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        URI uri = session.getUri();
        if (uri == null) return;
        
        String code = extractParam(uri.getQuery(), "code");
        String role = extractParam(uri.getQuery(), "role");

        if ("sender".equalsIgnoreCase(role)) {
            WebSocketSession receiver = sessionManager.getReceiver(code);
            if (receiver != null && receiver.isOpen()) {
                receiver.sendMessage(message);
            }
        } else if ("receiver".equalsIgnoreCase(role)) {
            WebSocketSession sender = sessionManager.getSender(code);
            if (sender != null && sender.isOpen()) {
                sender.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String code = extractParam(uri.getQuery(), "code");
            if (code != null) {
                System.out.println("Connection closed for code: " + code);
            }
        }
    }

    private String extractParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }
}
