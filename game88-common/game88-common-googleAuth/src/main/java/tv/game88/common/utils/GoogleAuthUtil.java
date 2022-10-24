package tv.game88.common.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

public class GoogleAuthUtil {

    private static final GoogleAuthenticator GOOGLE_AUTHENTICATOR = new GoogleAuthenticator();

    /**
     * 随机生成一个密钥
     */
    public static String createSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[]       bytes  = new byte[ 20 ];
        random.nextBytes( bytes );
        Base32 base32    = new Base32();
        String secretKey = base32.encodeToString( bytes );
        return secretKey.toLowerCase();
    }

    /**
     * 验证验证码
     */
    public static boolean verifyCode( String secretKey, int verificationCode ) {
        return GOOGLE_AUTHENTICATOR.authorize( secretKey, verificationCode );
    }

    public static String getQRBarcodeURL( String user, String host, String secret ) {
        String format = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
        return String.format( format, user, host, secret );
    }

    public static String tranUrlToBase64String( String url ) {
        try {
            URL               urlImg            = new URL( url );
            HttpURLConnection httpURLConnection = ( HttpURLConnection ) urlImg.openConnection();
            httpURLConnection.addRequestProperty( "User-Agent", "Mozilla / 4.76" );
            InputStream is = httpURLConnection.getInputStream();
            //定义字节数组大小；
            byte[]                buffer                = new byte[ 1024 ];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int                   rc                    = 0;
            while ( ( rc = is.read( buffer, 0, 100 ) ) > 0 ) {
                byteArrayOutputStream.write( buffer, 0, rc );
            }
            buffer = byteArrayOutputStream.toByteArray();
            Base64 base64 = new Base64();
            return base64.encodeToString( buffer );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
