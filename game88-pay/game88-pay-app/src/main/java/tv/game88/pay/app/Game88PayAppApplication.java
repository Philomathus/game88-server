package tv.game88.pay.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
public class Game88PayAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PayAppApplication.class, args );
    }
}