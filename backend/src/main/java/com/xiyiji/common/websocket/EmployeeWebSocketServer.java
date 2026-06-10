package com.xiyiji.common.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/ws/employee")
public class EmployeeWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(EmployeeWebSocketServer.class);
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("WebSocket connected: {}, total: {}", session.getId(), sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("WebSocket disconnected: {}, total: {}", session.getId(), sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        sessions.remove(session);
        log.error("WebSocket error: {}", session.getId(), error);
    }

    /**
     * 广播新订单通知给所有员工端
     */
    public static void broadcastNewOrder(String message) {
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("WebSocket send error", e);
                }
            }
        }
    }
}