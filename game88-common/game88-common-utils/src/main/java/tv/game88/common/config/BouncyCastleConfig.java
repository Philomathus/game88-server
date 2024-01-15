package tv.game88.common.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class BouncyCastleConfig {

    static {
        // 在静态块中加载 Bouncy Castle 提供者
        Security.addProvider( new BouncyCastleProvider() );
    }
}