package tv.game88.game.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
@EnableAspectJAutoProxy( exposeProxy = true )
public class Game88GameAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GameAdminApplication.class, args );
    }
}