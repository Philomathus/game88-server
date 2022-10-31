package tv.game88.platform.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
public class Game88PlatformAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PlatformAdminApplication.class, args );
    }
}