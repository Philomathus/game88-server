package tv.game88.wallet.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
@EnableAspectJAutoProxy( exposeProxy = true )
@EnableRetry
public class Game88WalletAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88WalletAppApplication.class, args );
    }
}