package tv.game88.game.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@EnableAspectJAutoProxy( exposeProxy = true )
@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableRetry
public class Game88GameAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88GameAppApplication.class, args );
    }
}