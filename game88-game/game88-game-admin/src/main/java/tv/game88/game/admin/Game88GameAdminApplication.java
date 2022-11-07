package tv.game88.game.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
public class Game88GameAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GameAdminApplication.class, args );
    }
}