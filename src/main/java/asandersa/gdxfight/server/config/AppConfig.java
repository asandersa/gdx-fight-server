package asandersa.gdxfight.server.config;


import asandersa.gdxfight.server.GameLoop;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
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
}
