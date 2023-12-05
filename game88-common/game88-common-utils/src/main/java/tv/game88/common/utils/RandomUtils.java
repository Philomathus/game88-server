package tv.game88.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 随机数算法
 */
public class RandomUtils {

    public static BigDecimal getBigDecimal( Object value ) {
        BigDecimal ret = null;
        if ( value != null ) {
            ret = switch ( value ) {
                case BigDecimal bigDecimal -> bigDecimal;
                case String s -> new BigDecimal( s );
                case BigInteger bigInteger -> new BigDecimal( bigInteger );
                case Number number -> BigDecimal.valueOf( number.doubleValue() );
                case null, default -> throw new ClassCastException(
                        "Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal." );
            };
        }
        return ret;
    }

    /**
     * 随机权重值
     *
     * @param weightableMap
     * @param totalWeight
     * @param <T>
     */
    public static <T> T randomWeight( Map<T, Integer> weightableMap, int totalWeight ) {
        int num = randomIntWithMax( 1, totalWeight );
        int sum = 0;
        for ( T weightable : weightableMap.keySet() ) {
            sum += weightableMap.get( weightable );
            if ( num <= sum ) {
                return weightable;
            }
        }
        return null;
    }

    /**
     * 随机权重值
     *
     * @param weightableMap
     * @param <T>
     */
    public static <T> List<T> randomWeight( int returnSize, Map<T, Integer> weightableMap, boolean isOnly ) {
        int totalWeight = 0;
        for ( int weight : weightableMap.values() ) {
            totalWeight += weight;
        }

        List<T> list = new ArrayList<>();
        for ( int i = 0; i < returnSize; i++ ) {
            if ( weightableMap.isEmpty() ) {
                break;
            }
            T t = randomWeight( weightableMap, totalWeight );
            list.add( t );
            if ( isOnly ) {
                totalWeight -= weightableMap.get( t );
                weightableMap.remove( t );
            }
        }
        return list;
    }

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

    public static BigDecimal randomDecimalWithMax( BigDecimal min, BigDecimal max ) {
        if ( min.compareTo( max ) >= 0 ) {
            return min;
        }
        BigDecimal randomBigDecimal = min.add( BigDecimal.valueOf( Math.random() ).multiply( max.subtract( min ) ) );
        return randomBigDecimal.setScale( 3, RoundingMode.HALF_UP );
    }

    public static void main( String[] args ) {
        for ( int i = 0; i < 50; i++ ) {
            System.out.println( randomDecimalWithMax( BigDecimal.valueOf( 0.01 ), BigDecimal.valueOf( 0.015 ) ) );
        }
    }

    public static BigDecimal randomBigDecimalWithMax( BigDecimal min, BigDecimal max ) {
        if ( min.compareTo( max ) > 0 ) {
            return min;
        }
        return min.add( BigDecimal.valueOf( Math.random() ).multiply( max.subtract( min ) ) );
    }
}
