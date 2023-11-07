package tv.game88.wallet.api.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ZXingUtil {

    /**
     * 画二维码图片
     *
     * @param imgPath 预生成图片路径
     * @param content 二维码内容
     * @param width   二维码宽
     * @param height  二维码高
     * @param logo    二维码居中logo图片地址
     *
     * @throws Exception 异常
     */
    public static void encodeImg( String imgPath, String content, int width, int height, String logo ) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        //排错率  L<M<Q<H
        hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H );
        //编码
        hints.put( EncodeHintType.CHARACTER_SET, "utf-8" );
        //外边距：margin
        hints.put( EncodeHintType.MARGIN, 1 );
        /*
         * content : 需要加密的 文字
         * BarcodeFormat.QR_CODE:要解析的类型（二维码）
         * hints：加密涉及的一些参数：编码、排错率
         */
        BitMatrix bitMatrix = new MultiFormatWriter().encode( content, BarcodeFormat.QR_CODE, width, height, hints );
        //内存中的一张图片：此时需要的图片 是二维码-> 需要一个boolean[][] ->BitMatrix
        BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );

        for ( int x = 0; x < width; x++ ) {
            for ( int y = 0; y < height; y++ ) {
                image.setRGB( x, y, ( bitMatrix.get( x, y ) ? Color.BLACK.getRGB() : Color.WHITE.getRGB() ) );
            }
        }
        //画logo
        logoMatrix( image, logo );
        //string->file
        File file = new File( imgPath );
        //生成图片
        ImageIO.write( image, "png", file );
    }

    /**
     * 解密：二维码->文字
     *
     * @param file 图片文件
     *
     * @throws Exception 异常
     */
    public static String decodeImg( File file ) throws Exception {
        if ( !file.exists() ) {
            return null;
        }
        //读取指定的二维码文件
        BufferedImage   bufferedImage = ImageIO.read( file );
        LuminanceSource source        = new BufferedImageLuminanceSource( bufferedImage );
        BinaryBitmap    binaryBitmap  = new BinaryBitmap( new HybridBinarizer( source ) );
        //定义二维码参数
        Map<DecodeHintType, String> hints = new HashMap<>();
        hints.put( DecodeHintType.CHARACTER_SET, "utf-8" );
        Result result = new MultiFormatReader().decode( binaryBitmap, hints );
        bufferedImage.flush();
        return result.toString();
    }

    //传入logo、二维码 ->带logo的二维码
    private static void logoMatrix( BufferedImage matrixImage, String logo ) throws IOException {
        //在二维码上画logo:产生一个  二维码画板
        Graphics2D g2 = matrixImage.createGraphics();

        //画logo： String->BufferedImage(内存)
        BufferedImage logoImg = ImageIO.read( new File( logo ) );
        int           height  = matrixImage.getHeight();
        int           width   = matrixImage.getWidth();
        //纯logo图片
        g2.drawImage( logoImg, width * 2 / 5, height * 2 / 5, width / 5, height / 5, null );

        //产生一个 画 白色圆角正方形的 画笔
        BasicStroke stroke = new BasicStroke( 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        //将画板-画笔 关联
        g2.setStroke( stroke );
        //创建一个正方形
        RoundRectangle2D.Float round = new RoundRectangle2D.Float(
                width * 2 / 5, height * 2 / 5, width / 5, height / 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        g2.setColor( Color.WHITE );
        g2.draw( round );

        //灰色边框
        BasicStroke stroke2 = new BasicStroke( 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        g2.setStroke( stroke2 );
        //创建一个正方形
        RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(
                width * 2 / 5 + 2,
                height * 2 / 5 + 2, width / 5 - 4, height / 5 - 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        //        Color color = new Color(128,128,128) ;
        g2.setColor( Color.GRAY );
        g2.draw( round2 );

        g2.dispose();
        matrixImage.flush();
    }

    public static void main( String[] args ) throws Exception {
        //String imgPath = "/Users/meng.jun/Downloads/5a7fdffcdcebc.png";
        String imgPath = "/Users/meng.jun/Downloads/1.jpg";
        System.out.println( decodeImg( new File( imgPath ) ) );
        String imgPath2 = "/Users/meng.jun/Downloads/2.jpg";
        System.out.println( decodeImg( new File( imgPath2 ) ) );
    }
}