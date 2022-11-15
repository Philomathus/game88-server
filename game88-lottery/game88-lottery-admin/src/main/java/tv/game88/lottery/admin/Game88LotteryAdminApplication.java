package tv.game88.lottery.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication( scanBasePackages = { "tv.game88" } )
@EnableScheduling
public class Game88LotteryAdminApplication {
    public static void main( String[] args ) {
        SpringApplication.run( Game88LotteryAdminApplication.class, args );
    }
}