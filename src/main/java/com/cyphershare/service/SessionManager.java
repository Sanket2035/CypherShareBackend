package com.cyphershare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;


import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class SessionManager {

    private final Map<String, SessionContext> activeSessions = new ConcurrentHashMap<>();

    public void createSession(String code, String tier) {
        long timeoutMs = "BUSINESS".equalsIgnoreCase(tier) ? 30 * 60 * 1000 : 5 * 60 * 1000;
        long maxBytes = "BUSINESS".equalsIgnoreCase(tier) ? Long.MAX_VALUE : 2L * 1024 * 1024 * 1024; // 2GB
        activeSessions.put(code, new SessionContext(System.currentTimeMillis() + timeoutMs, maxBytes));
        System.out.println("Session created for code: " + code + ", tier: " + tier);
    }

    public boolean incrementAndCheckBytes(String code, long bytes) {
        SessionContext context = activeSessions.get(code);
        if (context == null) return false;
        context.bytesTransferred += bytes;
        return context.bytesTransferred <= context.maxBytes;
    }

    public boolean validateSession(String code) {
        SessionContext context = activeSessions.get(code);
        if (context == null) {
            return false;
        }
        if (System.currentTimeMillis() > context.expiresAt) {
            activeSessions.remove(code);
            System.out.println("Session expired for code: " + code);
            return false;
        }
        return true;
    }

    public void registerSender(String code, WebSocketSession session) {
        SessionContext context = activeSessions.get(code);
        if (context != null) {
            context.senderSession = session;
        }
    }

    public void registerReceiver(String code, WebSocketSession session) {
        SessionContext context = activeSessions.get(code);
        if (context != null) {
            context.receiverSession = session;
        }
    }

    public WebSocketSession getReceiver(String code) {
        SessionContext context = activeSessions.get(code);
        return context != null ? context.receiverSession : null;
    }

    public WebSocketSession getSender(String code) {
        SessionContext context = activeSessions.get(code);
        return context != null ? context.senderSession : null;
    }

    public void removeSession(String code) {
        activeSessions.remove(code);
    }

    private static class SessionContext {
        long expiresAt;
        long maxBytes;
        long bytesTransferred = 0;
        WebSocketSession senderSession;
        WebSocketSession receiverSession;

        public SessionContext(long expiresAt, long maxBytes) {
            this.expiresAt = expiresAt;
            this.maxBytes = maxBytes;
        }
    }
}
