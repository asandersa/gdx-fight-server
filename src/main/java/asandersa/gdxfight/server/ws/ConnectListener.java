package asandersa.gdxfight.server.ws;

import org.springframework.web.socket.WebSocketSession;

public interface ConnectListener {
    void handle(WebSocketSession session);
}
