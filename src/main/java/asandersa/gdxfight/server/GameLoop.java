package asandersa.gdxfight.server;

import asandersa.gdxfight.server.actors.Ship;
import asandersa.gdxfight.server.ws.WebSocketHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@Component

public class GameLoop extends ApplicationAdapter {
    private static final float frameRate = 1 / 2f;
    private final WebSocketHandler socketHandler;
    private final Json json;
    private float lastRender = 0;
    private final ObjectMap<String, Ship> ships = new ObjectMap<>();
    private final ForkJoinPool pool = ForkJoinPool.commonPool();


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
                session.getNativeSession().getBasicRemote().sendText(session.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        socketHandler.setDisconnectListener(session -> {
            ships.remove(session.getId());
        });
        socketHandler.setMessageListener(((session, message) -> {
            pool.execute(() -> {
                String type = message.getString("type");
                switch (type) {
                    case "state":
                        Ship ship = ships.get(session.getId());
                        ship.setLeftPressed(message.getBoolean("leftPressed"));
                        ship.setRightPressed(message.getBoolean("rightPressed"));
                        ship.setUpPressed(message.getBoolean("upPressed"));
                        ship.setDownPressed(message.getBoolean("downPressed"));
                        ship.setAngle(message.getFloat("angle"));
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
            for (ObjectMap.Entry<String, Ship> shipEntry : ships) {
                Ship ship = shipEntry.value;
                ship.act(lastRender);
            }
            ;

            lastRender = 0;

            pool.execute(() -> {
                String stateJson = json.toJson(ships);
                for (StandardWebSocketSession session : socketHandler.getSessions()) {
                    try {
                        session.getNativeSession().getBasicRemote().sendText(stateJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
