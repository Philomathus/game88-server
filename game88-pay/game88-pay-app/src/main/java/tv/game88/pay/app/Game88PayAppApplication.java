package tv.game88.pay.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy( exposeProxy = true )
@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
@EnableRetry
public class Game88PayAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PayAppApplication.class, args );
    }
}