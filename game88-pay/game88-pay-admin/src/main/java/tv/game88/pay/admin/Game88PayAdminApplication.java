package tv.game88.pay.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy( exposeProxy = true )
@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88PayAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88PayAdminApplication.class, args );
    }
}