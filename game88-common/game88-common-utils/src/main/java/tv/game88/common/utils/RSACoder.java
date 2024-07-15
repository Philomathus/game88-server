package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 经典的数字签名算法RSA 数字签名
 *
 * @author MengJun
 */
@Log4j2
public class RSACoder {
    //数字签名，密钥算法
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 数字签名 签名/验证算法
     */
    public static final String SIGNATURE_ALGORITHM_MD5    = "MD5withRSA";
    public static final String SIGNATURE_ALGORITHM_SHA1   = "Sha1WithRSA";
    public static final String SIGNATURE_ALGORITHM_SHA256 = "SHA256withRSA";

    private static final int EN_SEGMENT_SIZE = 117;//加密长度

    private static final int DE_SEGMENT_SIZE = 128;//解密长度

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key  密钥
     *
     * @return 加密后hex16进制字符串
     * @throws Exception
     */
    public static String encryptByPublicKeyHex( String data, String key ) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec( Base64.decode( key ) );
        KeyFactory         keyFactory  = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          pubKey      = keyFactory.generatePublic( x509KeySpec );
        Cipher             cipher      = Cipher.getInstance( keyFactory.getAlgorithm() );
        cipher.init( Cipher.ENCRYPT_MODE, pubKey );
        return new BigInteger( 1, cipher.doFinal( data.getBytes( StandardCharsets.UTF_8 ) ) ).toString( 16 );
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key  密钥
     */
    public static String encryptByPublicKey( String data, String key ) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec( Base64.decode( key ) );
        KeyFactory         keyFactory  = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          pubKey      = keyFactory.generatePublic( x509KeySpec );
        Cipher             cipher      = Cipher.getInstance( keyFactory.getAlgorithm() );
        cipher.init( Cipher.ENCRYPT_MODE, pubKey );
        byte[] resultBytes = cipherDoFinal( cipher, data.getBytes( StandardCharsets.UTF_8 ), EN_SEGMENT_SIZE );
        return Base64.toBase64String( resultBytes );
    }

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key  密钥
     */
    public static String encryptByPrivateKey( String data, String key ) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec( Base64.decode( key ) );
        KeyFactory          keyFactory   = KeyFactory.getInstance( KEY_ALGORITHM );
        PrivateKey          privateKey   = keyFactory.generatePrivate( pkcs8KeySpec );
        Cipher              cipher       = Cipher.getInstance( keyFactory.getAlgorithm() );
        cipher.init( Cipher.ENCRYPT_MODE, privateKey );
        byte[] resultBytes = cipherDoFinal( cipher, data.getBytes( StandardCharsets.UTF_8 ), EN_SEGMENT_SIZE );
        return Base64.toBase64String( resultBytes );
    }

    /**
     * 公钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     */
    public static String decryptByPublicKey( String data, String key ) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec( Base64.decode( key ) );
        KeyFactory         keyFactory         = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          publicKey          = keyFactory.generatePublic( x509EncodedKeySpec );
        Cipher             cipher             = Cipher.getInstance( keyFactory.getAlgorithm() );
        cipher.init( Cipher.DECRYPT_MODE, publicKey );
        byte[] decBytes = cipherDoFinal( cipher, Base64.decode( data ), DE_SEGMENT_SIZE );
        return new String( decBytes, StandardCharsets.UTF_8 );
    }

    /**
     * 私钥解密
     **/
    public static String decryptByPrivateKey( String data, String key ) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec( Base64.decode( key ) );
        KeyFactory          keyFactory   = KeyFactory.getInstance( KEY_ALGORITHM );
        PrivateKey          privateKey   = keyFactory.generatePrivate( pkcs8KeySpec );
        Cipher              cipher       = Cipher.getInstance( keyFactory.getAlgorithm() );
        cipher.init( Cipher.DECRYPT_MODE, privateKey );
        byte[] decodeData = Base64.decode( data );
        int    blockSize  = cipher.getOutputSize( decodeData.length );
        return new String( cipherDoFinal( cipher, decodeData, blockSize ), StandardCharsets.UTF_8 );
    }

    /**
     * 分段加解密
     */
    private static byte[] cipherDoFinal( Cipher cipher, byte[] decryptData, int segmentSize ) throws IllegalBlockSizeException,
            BadPaddingException, IOException {
        if ( segmentSize <= 0 ) {
            throw new RuntimeException( "分段大小必须大于0" );
        }
        ByteArrayOutputStream out      = new ByteArrayOutputStream();
        int                   inputLen = decryptData.length;
        int                   offSet   = 0;
        byte[]                cache;
        int                   i        = 0;
        // 对数据分段解密
        while ( inputLen - offSet > 0 ) {
            if ( inputLen - offSet > segmentSize ) {
                cache = cipher.doFinal( decryptData, offSet, segmentSize );
            } else {
                cache = cipher.doFinal( decryptData, offSet, inputLen - offSet );
            }
            out.write( cache, 0, cache.length );
            i++;
            offSet = i * segmentSize;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    public static String signMd5Rsa( String data, String privateKey ) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec( Base64.decode( privateKey ) );
        KeyFactory          keyFactory          = KeyFactory.getInstance( KEY_ALGORITHM );
        PrivateKey          priKey              = keyFactory.generatePrivate( pkcs8EncodedKeySpec );
        Signature           signature           = Signature.getInstance( SIGNATURE_ALGORITHM_MD5 );
        signature.initSign( priKey );
        signature.update( data.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.toBase64String( signature.sign() );
    }

    public static boolean verifyMd5Rsa( String data, String publicKey, String sign ) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec( Base64.decode( publicKey ) );
        KeyFactory         keyFactory         = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          pubKey             = keyFactory.generatePublic( x509EncodedKeySpec );
        Signature          signature          = Signature.getInstance( SIGNATURE_ALGORITHM_MD5 );
        signature.initVerify( pubKey );
        signature.update( data.getBytes( StandardCharsets.UTF_8 ) );
        return signature.verify( Base64.decode( sign ) );
    }

    public static boolean verifySha256Rsa( String data, String publicKey, String sign ) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec( Base64.decode( publicKey ) );
        KeyFactory         keyFactory  = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          pubKey      = keyFactory.generatePublic( x509KeySpec );
        Signature          signVer     = Signature.getInstance( SIGNATURE_ALGORITHM_SHA256 );
        signVer.initVerify( pubKey );
        signVer.update( data.getBytes( StandardCharsets.UTF_8 ) );
        return signVer.verify( Base64.decode( sign ) );
    }

    public static String signSha256Rsa( String data, String privateKey ) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec( Base64.decode( privateKey ) );
        KeyFactory          keyFactory          = KeyFactory.getInstance( KEY_ALGORITHM );
        PrivateKey          priKey              = keyFactory.generatePrivate( pkcs8EncodedKeySpec );
        Signature           signature           = Signature.getInstance( SIGNATURE_ALGORITHM_SHA256 );
        signature.initSign( priKey );
        signature.update( data.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.toBase64String( signature.sign() );
    }

    public static String signSha1Rsa( String source, String privateKey ) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec( Base64.decode( privateKey ) );
        KeyFactory          keyFactory          = KeyFactory.getInstance( KEY_ALGORITHM );
        PrivateKey          priKey              = keyFactory.generatePrivate( pkcs8EncodedKeySpec );
        Signature           sign                = Signature.getInstance( SIGNATURE_ALGORITHM_SHA1 );
        sign.initSign( priKey );
        sign.update( source.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.toBase64String( sign.sign() );
    }

    public static boolean verifySha1Rsa( String data, String publicKey, String sign ) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec( Base64.decode( publicKey ) );
        KeyFactory         keyFactory  = KeyFactory.getInstance( KEY_ALGORITHM );
        PublicKey          pubKey      = keyFactory.generatePublic( x509KeySpec );
        Signature          signVer     = Signature.getInstance( SIGNATURE_ALGORITHM_SHA1 );
        signVer.initVerify( pubKey );
        signVer.update( data.getBytes( StandardCharsets.UTF_8 ) );
        return signVer.verify( Base64.decode( sign ) );
    }
}