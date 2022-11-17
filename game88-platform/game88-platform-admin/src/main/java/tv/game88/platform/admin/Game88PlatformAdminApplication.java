package tv.game88.platform.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88PlatformAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PlatformAdminApplication.class, args );
    }
}