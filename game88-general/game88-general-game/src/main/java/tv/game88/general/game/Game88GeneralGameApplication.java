package tv.game88.general.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88GeneralGameApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GeneralGameApplication.class, args );
    }
}