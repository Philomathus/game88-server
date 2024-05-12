package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密解密工具
 */
@Log4j2
public class AESCoder {
    public static final String secretKey   = "$bV;_N#if5:[`^@npoU|><+9!Sj*)Be7";
    public static final String AES         = "AES";
    public static final String charsetName = "UTF-8";

    /**
     * 生成密钥 key
     *
     * @param password 加密密码
     *
     * @throws Exception
     */
    private static SecretKeySpec generateKey( String password ) throws Exception {
        // 1.构造密钥生成器，指定为AES算法,不区分大小写
        KeyGenerator keyGenerator = KeyGenerator.getInstance( AES );
        // 2. 因为AES要求密钥的长度为128，我们需要固定的密码，因此随机源的种子需要设置为我们的密码数组
        // 生成一个128位的随机源, 根据传入的字节数组
        /**
         * 这种方式 windows 下正常, Linux 环境下会解密失败
         * keyGenerator.init(128, new SecureRandom(password.getBytes()));
         */
        // 兼容 Linux
        SecureRandom random = SecureRandom.getInstance( "SHA1PRNG" );
        random.setSeed( password.getBytes() );
        keyGenerator.init( 128, random );
        // 3.产生原始对称密钥
        SecretKey original_key = keyGenerator.generateKey();
        // 4. 根据字节数组生成AES密钥
        return new SecretKeySpec( original_key.getEncoded(), AES );
    }

    /**
     * 加密
     *
     * @param content  加密的内容
     * @param password 加密密码
     */
    private static String AESEncode( String content, String password ) {
        try {
            // 根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance( AES );
            // 基于加密模式和密钥初始化Cipher
            cipher.init( Cipher.ENCRYPT_MODE, generateKey( password ) );
            // 单部分加密结束, 重置Cipher, 获取加密内容的字节数组(这里要设置为UTF-8)防止解密为乱码
            byte[] bytes = cipher.doFinal( content.getBytes( charsetName ) );
            // 将加密后的字节数组转为字符串返回
            return Base64.getEncoder().encodeToString( bytes );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }

        // 如果有错就返回 null
        return null;
    }

    /**
     * 解密
     *
     * @param content  解密内容
     * @param password 解密密码
     */
    private static String AESDecode( String content, String password ) throws Exception {
        // 将加密并编码后的内容解码成字节数组
        byte[] bytes = Base64.getDecoder().decode( content );
        // 这里指定了算法为AES
        Cipher cipher = Cipher.getInstance( AES );
        // 基于解密模式和密钥初始化Cipher
        cipher.init( Cipher.DECRYPT_MODE, generateKey( password ) );
        // 单部分加密结束，重置Cipher
        byte[] result = cipher.doFinal( bytes );
        // 将解密后的字节数组转成 UTF-8 编码的字符串返回
        return new String( result, charsetName );
    }

    /**
     * 加密
     *
     * @param content 加密内容
     */
    public static String encrypt( String content ) {
        return StringUtils.isBlank( content ) ? null : AESEncode( content, secretKey );
    }

    /**
     * 解密
     *
     * @param content 解密内容
     */
    public static String decrypt( String content ) throws Exception {
        return StringUtils.isBlank( content ) ? null : AESDecode( content, secretKey );
    }

    public static String encryptByKey( String value, String key ) throws Exception {
        Cipher        cipher   = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
        byte[]        raw      = key.getBytes( StandardCharsets.UTF_8 );
        SecretKeySpec skeySpec = new SecretKeySpec( raw, AES );
        cipher.init( Cipher.ENCRYPT_MODE, skeySpec );
        byte[] encrypted = cipher.doFinal( value.getBytes( StandardCharsets.UTF_8 ) );
        return Base64.getEncoder().encodeToString( encrypted );// 此处使用BASE64做转码
    }

    public static String encryptByKeyUrl( String value, String key ) throws Exception {
        Cipher        cipher   = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
        byte[]        raw      = key.getBytes( StandardCharsets.UTF_8 );
        SecretKeySpec skeySpec = new SecretKeySpec( raw, AES );
        cipher.init( Cipher.ENCRYPT_MODE, skeySpec );
        byte[] encrypted = cipher.doFinal( value.getBytes( StandardCharsets.UTF_8 ) );
        String base64    = Base64.getEncoder().encodeToString( encrypted );// 此处使用BASE64做转码
        return URLEncoder.encode( base64, StandardCharsets.UTF_8 );//URL加密
    }

    public static String encryptByKeyIvNoPadding( String data, String key, String iv ) throws Exception {
        Cipher cipher          = Cipher.getInstance( "AES/CBC/NoPadding" );
        int    blockSize       = cipher.getBlockSize();
        byte[] dataBytes       = data.getBytes( StandardCharsets.UTF_8 );
        int    plainTextLength = dataBytes.length;
        if ( plainTextLength % blockSize != 0 ) {
            plainTextLength = plainTextLength + ( blockSize - plainTextLength % blockSize );
        }
        byte[] plaintext = new byte[ plainTextLength ];
        System.arraycopy( dataBytes, 0, plaintext, 0, dataBytes.length );
        SecretKeySpec   keySpec = new SecretKeySpec( key.getBytes(), AES );
        IvParameterSpec ivSpec  = new IvParameterSpec( iv.getBytes() );
        cipher.init( Cipher.ENCRYPT_MODE, keySpec, ivSpec );
        byte[] encrypted = cipher.doFinal( plaintext );
        return Base64.getUrlEncoder().encodeToString( encrypted );
    }

    public static String decryptByKey( String content, String key ) throws Exception {
        byte[]        encrypted1 = Base64.getDecoder().decode( content );
        byte[]        raw        = key.getBytes( StandardCharsets.UTF_8 );
        SecretKeySpec skeySpec   = new SecretKeySpec( raw, AES );
        Cipher        cipher     = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
        cipher.init( Cipher.DECRYPT_MODE, skeySpec );
        byte[] original = cipher.doFinal( encrypted1 );
        return new String( original, StandardCharsets.UTF_8 );
    }

    public static String encryptByKeyIv( String content, String AESKey, String AESIV ) throws Exception {
        Cipher          cipher   = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
        SecretKeySpec   skeySpec = new SecretKeySpec( AESKey.getBytes( StandardCharsets.US_ASCII ), AES );
        IvParameterSpec iv       = new IvParameterSpec( AESIV.getBytes() );//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init( Cipher.ENCRYPT_MODE, skeySpec, iv );
        byte[] encrypted = cipher.doFinal( content.getBytes( StandardCharsets.UTF_8 ) );
        return bytesToHex( encrypted );
    }

    public static String bytesToHex( byte[] bytes ) {
        StringBuilder buf = new StringBuilder( bytes.length * 2 );
        for ( byte b : bytes ) { // 使用String的format方法进行转换
            buf.append( String.format( "%02x", b & 0xff ) );
        }
        return buf.toString();
    }

    public static String decryptByKeyIv( String content, String AESKey, String AESIV ) throws Exception {
        Cipher          cipher   = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
        SecretKeySpec   skeySpec = new SecretKeySpec( AESKey.getBytes( StandardCharsets.US_ASCII ), AES );
        IvParameterSpec iv       = new IvParameterSpec( AESIV.getBytes() );//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init( Cipher.DECRYPT_MODE, skeySpec, iv );
        byte[] buffer    = toBytes( content );
        byte[] encrypted = cipher.doFinal( buffer );
        return new String( encrypted, StandardCharsets.UTF_8 );//此处使用BASE64做转码。
    }

    public static byte[] toBytes( String str ) {
        if ( str == null || str.trim().equals( "" ) ) {
            return new byte[ 0 ];
        }
        byte[] bytes = new byte[ str.length() / 2 ];
        for ( int i = 0; i < str.length() / 2; i++ ) {
            String subStr = str.substring( i * 2, i * 2 + 2 );
            bytes[ i ] = ( byte ) Integer.parseInt( subStr, 16 );
        }
        return bytes;
    }

    public static String encryptDES3( String str, String saltTxt ) throws Exception {
        byte[]    md5Key  = getMd5( saltTxt ); //16bytes
        SecretKey key     = new SecretKeySpec( md5Key, "DESede" );
        Cipher    ecipher = Cipher.getInstance( "DESede/ECB/PKCS5Padding" );
        ecipher.init( Cipher.ENCRYPT_MODE, key );
        byte[] data           = str.getBytes( StandardCharsets.UTF_8 );
        byte[] encryptedArray = ecipher.doFinal( data );
        return Base64.getEncoder().encodeToString( encryptedArray );
    }

    private static byte[] getMd5( String keyString ) throws Exception {
        byte[]        rawKey        = new byte[ 24 ];
        MessageDigest messageDigest = MessageDigest.getInstance( "MD5" );
        messageDigest.update( keyString.getBytes( StandardCharsets.UTF_8 ), 0, keyString.length() );
        byte[] md5 = messageDigest.digest();
        System.arraycopy( md5, 0, rawKey, 0, 16 );
        System.arraycopy( md5, 0, rawKey, 16, 8 );
        return rawKey;
    }

    public static String encryptByKeyIv7Padding( String content, String AESKey, String AESIV ) throws Exception {
        Cipher          cipher   = Cipher.getInstance( "AES/CBC/PKCS7Padding", "BC" );
        SecretKeySpec   skeySpec = new SecretKeySpec( Base64.getDecoder().decode( AESKey ), AES );
        IvParameterSpec iv       = new IvParameterSpec( Base64.getDecoder().decode( AESIV ) );//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init( Cipher.ENCRYPT_MODE, skeySpec, iv );
        byte[] encrypted = cipher.doFinal( content.getBytes( StandardCharsets.UTF_8 ) );
        return new String( Base64.getEncoder().encode( encrypted ) );
    }


    public static String decryptByKeyIv7Padding( String content, String AESKey, String AESIV ) throws Exception {
        Cipher          cipher   = Cipher.getInstance( "AES/CBC/PKCS7Padding", "BC" );
        SecretKeySpec   skeySpec = new SecretKeySpec( Base64.getDecoder().decode( AESKey ), AES );
        IvParameterSpec iv       = new IvParameterSpec( Base64.getDecoder().decode( AESIV ) );//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init( Cipher.DECRYPT_MODE, skeySpec, iv );
        byte[] encrypted = cipher.doFinal( Base64.getDecoder().decode( content ) );
        return new String( encrypted, StandardCharsets.UTF_8 );
    }
}
