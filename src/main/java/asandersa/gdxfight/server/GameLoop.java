package asandersa.gdxfight.server;

import asandersa.gdxfight.server.actors.Ship;
import asandersa.gdxfight.server.ws.WebSocketHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@Component

public class GameLoop extends ApplicationAdapter {
    private static final float frameRate = 1 / 60f;
    private final WebSocketHandler socketHandler;
    private final Json json;
    private float lastRender = 0;
    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    private final ObjectMap<String, Ship> ships = new ObjectMap<>();
    private final Array<Ship> stateToSend = new Array<>();


    public GameLoop(WebSocketHandler socketHandler, Json json) {
        this.socketHandler = socketHandler;
        this.json = json;
    }

    @Override
    public void create() {
        socketHandler.setConnectListener(session -> {
            Ship ship = new Ship();
            ship.setId(session.getId());
            ships.put(session.getId(), ship);
            try {
                session
                        .getNativeSession()
                        .getBasicRemote()
                        .sendText(
                                String.format("{\"class\":\"sessionKey\",\"id\":\"%s\"}", session.getId())
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        socketHandler.setDisconnectListener(session -> {
            sendToEverybody(
                    String.format("{\"class\":\"evict\",\"id\":\"%s\"}", session.getId())
            );

            ships.remove(session.getId());
        });
        socketHandler.setMessageListener(((session, message) -> {
            pool.execute(() -> {
                String type = message.get("type").asText();
                switch (type) {
                    case "state":
                        Ship ship = ships.get(session.getId());
                        ship.setLeftPressed(message.get("leftPressed").asBoolean());
                        ship.setRightPressed(message.get("rightPressed").asBoolean());
                        ship.setUpPressed(message.get("upPressed").asBoolean());
                        ship.setDownPressed(message.get("downPressed").asBoolean());
                        ship.setAngle((float) message.get("angle").asDouble());
                    break;
                    default:
                        throw new RuntimeException("Unknown WS object type: " + type);
                }
            });
        }));
    }

    @Override
    public void render() {
        lastRender += Gdx.graphics.getDeltaTime();
        if (lastRender >= frameRate) {
            stateToSend.clear();
            for (ObjectMap.Entry<String, Ship> shipEntry : ships) {
                Ship ship = shipEntry.value;
                ship.act(lastRender);
                stateToSend.add(ship);
            }
            ;

            lastRender = 0;
            String stateJson = json.toJson(stateToSend);

            sendToEverybody(stateJson);
        }
    }
    private void sendToEverybody(String json) {
        pool.execute(() -> {
            for (StandardWebSocketSession session : socketHandler.getSessions()) {
                try {
                    if (session.isOpen()) {
                        session.getNativeSession().getBasicRemote().sendText(json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
