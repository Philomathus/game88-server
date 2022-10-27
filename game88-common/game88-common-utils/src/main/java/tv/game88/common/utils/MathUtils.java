package tv.game88.common.utils;

public class MathUtils {

    /**
     * 返回min-max之间的随机数
     *
     * @param min(包含)
     * @param max(包含)
     */
    public static int randomIntWithMax( int min, int max ) {
        if ( min >= max ) {
            return min;
        }
        return ( int ) ( Math.random() * ( max - min + 1 ) + min );
    }
}
