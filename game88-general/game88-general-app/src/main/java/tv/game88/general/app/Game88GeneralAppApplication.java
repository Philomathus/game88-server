package tv.game88.general.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
public class Game88GeneralAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GeneralAppApplication.class, args );
    }
}