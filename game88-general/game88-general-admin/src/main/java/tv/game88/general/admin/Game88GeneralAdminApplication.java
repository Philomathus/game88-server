package tv.game88.general.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
@EnableAsync
public class Game88GeneralAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GeneralAdminApplication.class, args );
    }
}