package tv.game88.platform.api.constant;

import tv.game88.common.utils.LoadResourceUtil;

import java.io.IOException;

public class KeyConstants {
    public static String MEMBER_LOGIN_PRIVATE_KEY;

    static {
        try {
            MEMBER_LOGIN_PRIVATE_KEY = LoadResourceUtil.getSecurityKeyStr( "secretkey/memberLoginPrivateKey" );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
