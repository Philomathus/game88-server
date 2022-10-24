package tv.game88.core.admin.constant;

import tv.game88.common.utils.LoadResourceUtil;

import java.io.IOException;

public class KeyConstants {
    public static String LOGIN_PRIVATE_KEY;
    public static String GOOGLE_AUTH_PRIVATE_KEY;
    public static String GOOGLE_AUTH_PUBLIC_KEY;

    static {
        try {
            LOGIN_PRIVATE_KEY       = LoadResourceUtil.getSecurityKeyStr( "secretkey/loginPrivateKey" );
            GOOGLE_AUTH_PRIVATE_KEY = LoadResourceUtil.getSecurityKeyStr( "secretkey/googleAuthPrivateKey" );
            GOOGLE_AUTH_PUBLIC_KEY  = LoadResourceUtil.getSecurityKeyStr( "secretkey/googleAuthPublicKey" );
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }
}
