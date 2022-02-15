package asandersa.gdxfight.server;

import asandersa.gdxfight.server.ws.WebSocketHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.Array;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component

public class GameLoop extends ApplicationAdapter {
    private final WebSocketHandler socketHandler;
    private final Array<String> events = new Array<>();

    public GameLoop(WebSocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void create() {
        socketHandler.setConnectListener(session -> {
            events.add(session.getId() + " just joined");

        });
        socketHandler.setDisconnectListener(session -> {
            events.add(session.getId() + " just disconnected");
        });
        socketHandler.setMessageListener(((session, message) -> {
            events.add(session.getId() + " said" + message);
        }));
    }

    @Override
    public void render() {
        for (WebSocketSession session : socketHandler.getSessions()) { //получаем все актуальные сессии
            try {
                for (String event : events) {
                    session.sendMessage(new TextMessage(event));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            events.clear();
        }
    }
}
