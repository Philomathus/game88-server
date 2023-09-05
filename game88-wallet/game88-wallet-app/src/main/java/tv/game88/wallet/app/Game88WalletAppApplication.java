package tv.game88.wallet.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88WalletAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88WalletAppApplication.class, args );
    }
}