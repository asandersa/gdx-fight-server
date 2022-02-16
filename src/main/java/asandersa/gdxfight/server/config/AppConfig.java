package asandersa.gdxfight.server.config;


import asandersa.gdxfight.server.GameLoop;
import asandersa.gdxfight.server.actors.Ship;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    /**
     *
     Приложение, которое имплементирует наш сервер.
     Не создается представление того, что будем рассчитывать в визуальном виде.
     */
    public HeadlessApplication getApplication(GameLoop gameLoop) {
        return new HeadlessApplication(gameLoop);

    }

    @Bean
    public Json getJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.addClassTag("ship", Ship.class);
        return json;


    }

}
