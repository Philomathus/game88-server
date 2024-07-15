package tv.game88.common.utils;

import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class DesCoder {
    //数字签名，密钥算法
    public static final String KEY_ALGORITHM = "DES";

    /**
     * 数字签名 签名/验证算法
     */
    public static final String SIGNATURE_ALGORITHM_DES = "DES/ECB/PKCS5Padding";

    public static final String secretKey = "$bV;_N#i";

    public static String encrypt( String value, String key ) throws Exception {
        Cipher cipher = Cipher.getInstance( SIGNATURE_ALGORITHM_DES );
        cipher.init( Cipher.ENCRYPT_MODE, new SecretKeySpec( key.getBytes( StandardCharsets.UTF_8 ), KEY_ALGORITHM ) );
        byte[] bytes = cipher.doFinal( value.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.toBase64String( bytes );
    }

    public static String decrypt( String value, String key ) throws Exception {
        Cipher cipher = Cipher.getInstance( SIGNATURE_ALGORITHM_DES );
        cipher.init( Cipher.DECRYPT_MODE, new SecretKeySpec( key.getBytes(), KEY_ALGORITHM ) );
        byte[] original = cipher.doFinal( Base64.decode( value ) );
        return new String( original );
    }
}
