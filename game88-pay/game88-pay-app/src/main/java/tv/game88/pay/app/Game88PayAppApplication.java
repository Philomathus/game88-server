package tv.game88.pay.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88PayAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PayAppApplication.class, args );
    }
}