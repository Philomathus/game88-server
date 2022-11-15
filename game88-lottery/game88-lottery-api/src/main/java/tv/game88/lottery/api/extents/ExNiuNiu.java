package tv.game88.lottery.api.extents;


import com.lottery.common.dto.LocalMethod;
import com.lottery.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class ExNiuNiu {
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal>  oddsMap    = new HashMap<>();

    private static final List<String> zeroList     = Arrays.asList( "10", "J", "Q", "K" );
    private static final List<String> colorNiuList = Arrays.asList( "J", "Q", "K" );
    // S黑桃 > H 红心 >  C梅花 > D方块
    private static final String[]     suits        = { "S", "H", "C", "D" };
    private static final String[]     cardValues   = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };

    public static BigDecimal handle( String methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal  prize   = BigDecimal.ZERO;
        String[]    betarrs = betSelect.split( "&" );
        String      des     = "";
        LocalMethod method  = methodsMap.get( methodId );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( methodsMap.get( methodId ).getName() ) {
        case "胜负":
            break;
        case "龙虎 牌1VS牌5":
            break;
        case "蓝方牛":
            break;
        case "红方牛":
            break;
        default:
            break;
        }
        return prize;

    }

    public static String concatBetString( Map<String, BigDecimal> betMap ) {

        String[] keys = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "单", "双", "大", "小", "质", "合", "龙", "虎", "和" };

        String[] values = new String[ keys.length ];

        for ( int i = 0; i < keys.length; i++ ) {
            values[ i ] = betMap.getOrDefault( keys[ i ], BigDecimal.ZERO ).setScale( 2, BigDecimal.ROUND_HALF_UP ).toString();
        }

        return String.format( "0:%s-1:%s-2:%s-3:%s-4:%s-5:%s-6:%s-7:%s-8:%s-9:%s-单:%s-双:%s-大:%s-小:%s-质:%s-合:%s-龙:%s-虎:%s-和:%s",
                values );
    }

    public static BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        return paijiangTotal;
    }


    public static List<String> randomResult() {
        List<String> deck = new ArrayList<>( 52 );
        for ( String suit : suits ) {
            for ( String cardValue : cardValues ) {
                deck.add( cardValue + suit );
            }
        }
        List<String> blueShuffledDeck = new ArrayList<>();
        Random       chooser          = new Random();
        //生成蓝方手牌
        for ( int i = 0; i < 5; i++ ) {
            int blueSelection = chooser.nextInt( deck.size() );
            blueShuffledDeck.add( deck.remove( blueSelection ) );
        }
        //生成红方手牌
        List<String> redShuffledDeck = new ArrayList<>();
        for ( int i = 0; i < 5; i++ ) {
            int redSelection = chooser.nextInt( deck.size() );
            redShuffledDeck.add( deck.remove( redSelection ) );
        }
        // 控制开花色牛几率
        if ( ( isColorNiu( blueShuffledDeck ) || isColorNiu( redShuffledDeck ) ) && chooser.nextInt( 100 ) < 98 ) {
            return randomResult();
        }
        return Arrays.asList( String.join( ",", blueShuffledDeck ), String.join( ",", redShuffledDeck ) );
    }

    /**
     * 判断是否是花色牛
     *
     * @param shuffledDeck
     */
    public static boolean isColorNiu( List<String> shuffledDeck ) {
        for ( String s : shuffledDeck ) {
            String numStr;
            if ( s.length() == 2 ) {
                numStr = s.substring( 0, 1 );
            } else {
                numStr = s.substring( 0, 2 );
            }
            if ( !colorNiuList.contains( numStr ) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算总和
     */
    public static int countTotal( Collection<String> shuffledDeck ) {
        int totel = 0;
        for ( String s : shuffledDeck ) {
            String numStr = s.substring( 0, s.length() == 2 ? 1 : 2 );
            if ( !zeroList.contains( numStr ) ) {
                totel += Integer.parseInt( numStr );
            }
        }
        return totel % 10;
    }

    /**
     * 判断是否是无牛
     *
     * @param shuffledDeck
     */
    public static boolean isNoNiu( List<String> shuffledDeck ) {
        Set<Set<String>> result = new HashSet<>();
        for ( int x = 0; x < 5; x++ ) {
            for ( int y = 0; y < 5; y++ ) {
                for ( int z = 0; z < 5; z++ ) {
                    if ( x != y && y != z && x != z ) {
                        Set<String> node = new HashSet<>();
                        node.add( shuffledDeck.get( x ) );
                        node.add( shuffledDeck.get( y ) );
                        node.add( shuffledDeck.get( z ) );
                        result.add( node );
                    }
                }
            }
        }
        for ( Set<String> set : result ) {
            if ( countTotal( set ) == 0 ) {
                return false;
            }
        }
        return true;
    }

    public static String numToNiu( int count ) {
        String niu;
        switch ( count ) {
        case 0:
            niu = "牛";
            break;
        case 1:
            niu = "一";
            break;
        case 2:
            niu = "二";
            break;
        case 3:
            niu = "三";
            break;
        case 4:
            niu = "四";
            break;
        case 5:
            niu = "五";
            break;
        case 6:
            niu = "六";
            break;
        case 7:
            niu = "七";
            break;
        case 8:
            niu = "八";
            break;
        case 9:
            niu = "九";
            break;
        default:
            niu = "";
            break;
        }
        return niu;
    }

    /**
     * 获取牌型说明
     *
     * @param shuffledDeck
     */
    public static String getNiuStr( List<String> shuffledDeck ) {
        if ( isNoNiu( shuffledDeck ) ) {
            return "无牛";
        } else if ( isColorNiu( shuffledDeck ) ) {
            return "花色牛";
        } else {
            return "牛" + numToNiu( countTotal( shuffledDeck ) );
        }
    }

    public static int setNiuSize( String niuStr ) {
        int res;
        switch ( niuStr ) {
        case "无牛":
            res = 0;
            break;
        case "牛一":
            res = 1;
            break;
        case "牛二":
            res = 2;
            break;
        case "牛三":
            res = 3;
            break;
        case "牛四":
            res = 4;
            break;
        case "牛五":
            res = 5;
            break;
        case "牛六":
            res = 6;
            break;
        case "牛七":
            res = 7;
            break;
        case "牛八":
            res = 8;
            break;
        case "牛九":
            res = 9;
            break;
        case "牛牛":
            res = 10;
            break;
        case "花色牛":
            res = 11;
            break;
        default:
            res = -1;
            break;
        }
        return res;
    }

    public static List<String> compareCard( List<String> blueShuffledDeck, List<String> redShuffledDeck ) {
        String blueNiuStr = getNiuStr( blueShuffledDeck );
        String redNiuStr  = getNiuStr( redShuffledDeck );
        // 如果相等,则比较两副牌内最大的牌
        if ( blueNiuStr.equals( redNiuStr ) ) {
            String blueBigCard = getBigCard( blueShuffledDeck );
            String redBigCard  = getBigCard( redShuffledDeck );

            String blueStartNum = blueBigCard.substring( 0, blueBigCard.length() == 2 ? 1 : 2 );
            String redStartNum  = redBigCard.substring( 0, redBigCard.length() == 2 ? 1 : 2 );
            // 如果牌型号码一样,则比较花色
            if ( blueStartNum.equals( redStartNum ) ) {
                String blueColorNum = blueBigCard.substring( blueBigCard.length() - 1 );
                String redColorNum  = redBigCard.substring( redBigCard.length() - 1 );

                for ( String suit : suits ) {
                    if ( suit.equals( blueColorNum ) ) {
                        return blueShuffledDeck;
                    }
                    if ( suit.equals( redColorNum ) ) {
                        return redShuffledDeck;
                    }
                }
            } else { // 否则比较牌型号码
                for ( int i = cardValues.length - 1; i >= 0; i-- ) {
                    if ( cardValues[ i ].equals( blueStartNum ) ) {
                        return blueShuffledDeck;
                    }
                    if ( cardValues[ i ].equals( redStartNum ) ) {
                        return redShuffledDeck;
                    }
                }
            }
        } else if ( setNiuSize( blueNiuStr ) > setNiuSize( redNiuStr ) ) {
            return blueShuffledDeck;
        } else {
            return redShuffledDeck;
        }
        throw new IllegalArgumentException( "逻辑错误" );
    }

    /**
     * 获取一副牌中最大的那张牌
     *
     * @param shuffledDeck
     */
    public static String getBigCard( List<String> shuffledDeck ) {
        String cardValue = "";
        a:
        for ( int i = cardValues.length - 1; i >= 0; i-- ) {
            for ( String s : shuffledDeck ) {
                if ( s.startsWith( cardValues[ i ] ) ) {
                    cardValue = cardValues[ i ];
                    break a;
                }
            }
        }
        for ( String suit : suits ) {
            for ( String card : shuffledDeck ) {
                if ( ( cardValue + suit ).equals( card ) ) {
                    return card;
                }
            }
        }
        return null;
    }

    public static void main( String[] args ) {
        Random chooser = new Random();
        for ( int i = 0; i < 100; i++ ) {
            List<String> deck = new ArrayList<>( 52 );
            for ( String suit : suits ) {
                for ( String cardValue : cardValues ) {
                    deck.add( cardValue + suit );
                }
            }
            List<String> blueShuffledDeck = new ArrayList<>();
            //生成蓝方手牌
            for ( int j = 0; j < 5; j++ ) {
                int blueSelection = chooser.nextInt( deck.size() );
                blueShuffledDeck.add( deck.remove( blueSelection ) );
            }

            //生成红方手牌
            List<String> redShuffledDeck = new ArrayList<>();
            for ( int k = 0; k < 5; k++ ) {
                int redSelection = chooser.nextInt( deck.size() );
                redShuffledDeck.add( deck.remove( redSelection ) );
            }
            String blueNiuStr = getNiuStr( blueShuffledDeck );
            String redNiuStr  = getNiuStr( redShuffledDeck );
            if ( !blueNiuStr.equals( redNiuStr ) ) {
                continue;
            }
            System.out.print( "blue:" + JsonUtil.object2Json( blueShuffledDeck ) + " - " + blueNiuStr );
            System.out.print( " ====== " );
            System.out.print( "red:" + JsonUtil.object2Json( redShuffledDeck ) + " - " + redNiuStr );
            System.out.print( " ====== " );
            System.out.println( "赢牌:" + compareCard( blueShuffledDeck, redShuffledDeck ) );

        }
    }
}
