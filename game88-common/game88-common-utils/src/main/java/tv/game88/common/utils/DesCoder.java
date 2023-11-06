package tv.game88.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DesCoder {
    //数字签名，密钥算法
    public static final String KEY_ALGORITHM = "DES";

    /**
     * 数字签名 签名/验证算法
     */
    public static final String SIGNATURE_ALGORITHM_DES = "DES/ECB/PKCS5Padding";

    public static String encrypt( String value, String key ) throws Exception {
        Cipher cipher = Cipher.getInstance( SIGNATURE_ALGORITHM_DES );
        cipher.init( Cipher.ENCRYPT_MODE, new SecretKeySpec( key.getBytes( StandardCharsets.UTF_8 ), KEY_ALGORITHM ) );
        byte[] bytes = cipher.doFinal( value.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.getEncoder().encodeToString( bytes );
    }

    public static String decrypt( String value, String key ) throws Exception {
        Cipher cipher = Cipher.getInstance( SIGNATURE_ALGORITHM_DES );
        cipher.init( Cipher.DECRYPT_MODE, new SecretKeySpec( key.getBytes( StandardCharsets.UTF_8 ), KEY_ALGORITHM ) );
        byte[] bytes = cipher.doFinal( value.getBytes( StandardCharsets.UTF_8 ) );
        return new String( bytes, StandardCharsets.UTF_8 );
    }
}
