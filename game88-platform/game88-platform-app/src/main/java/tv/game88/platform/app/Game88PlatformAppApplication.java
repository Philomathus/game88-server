package tv.game88.platform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication( scanBasePackages = { "tv.game88" } )
public class Game88PlatformAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PlatformAppApplication.class, args );
    }
}