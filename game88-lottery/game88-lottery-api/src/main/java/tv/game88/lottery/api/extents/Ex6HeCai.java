package tv.game88.lottery.api.extents;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.LocalMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Ex6HeCai {
    //methodID:methods
    public static Map<String, Integer> weightableMap = new HashMap<>();

    static {
        for ( int i = 1; i <= 49; i++ ) {
            weightableMap.put( String.format( "%02d", i ), 100 );
        }
    }

    public static BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal              prize   = BigDecimal.ZERO;
        String                  tarCode = officialSpl[ officialSpl.length - 1 ];
        String[]                betarrs = betSelect.split( "&" );
        String                  des     = "";
        LocalMethod             method  = LotteryCacheUtils.me.getLocalMethod( methodId );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 4 );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( method.getName() ) {
        case "特码两面":
            for ( String bt : betarrs ) {
                //单双
                des = getDanShuang( tarCode );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
                //大小
                des = getDaXiao( tarCode );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
            }
            break;
        case "特码生肖":
            for ( String bt : betarrs ) {
                //单双
                des = getShengXiao( tarCode );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                }
            }
            break;
        case "特码色波":
            for ( String bt : betarrs ) {

                des = getColor( tarCode );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                }
            }
            break;
        }
        return prize;
    }

    public static String getDanShuang( String first ) {
        if ( Integer.parseInt( first ) % 2 == 0 ) {
            return "双";
        }
        return "单";
    }

    public static String getDaXiao( String first ) {
        if ( Integer.parseInt( first ) >= 25 ) {
            return "大";
        }
        return "小";
    }

    public static String getColor( String code ) {
        return switch ( code ) {
            case "01", "02", "07", "08", "12", "13", "18", "19", "23", "24", "29", "30", "34", "35", "40", "45", "46" -> "红";
            case "03", "04", "09", "10", "14", "15", "20", "25", "26", "31", "36", "37", "41", "42", "47", "48" -> "蓝";
            default -> "绿";
        };
    }

    public static void main( String[] args ) {
        System.out.println( getShengXiao( "32" ) );
    }

    public static String getShengXiao( String code ) {
        return switch ( Integer.parseInt( code ) ) {
            case 1, 13, 25, 37, 49 -> "虎";
            case 2, 14, 26, 38 -> "牛";
            case 3, 15, 27, 39 -> "鼠";
            case 4, 16, 28, 40 -> "猪";
            case 5, 17, 29, 41 -> "狗";
            case 6, 18, 30, 42 -> "鸡";
            case 7, 19, 31, 43 -> "猴";
            case 8, 20, 32, 44 -> "羊";
            case 9, 21, 33, 45 -> "马";
            case 10, 22, 34, 46 -> "蛇";
            case 11, 23, 35, 47 -> "龙";
            case 12, 24, 36, 48 -> "兔";
            default -> "";
        };

    }

    public static String concatBetString( Map<String, BigDecimal> betMap ) {
        String sp           = "-";
        String betCountinfo = "";
        String bt           = "鼠";
        betCountinfo = betCountinfo.concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "牛";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "虎";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "兔";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "龙";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "蛇";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "马";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "羊";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "猴";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "鸡";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "狗";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "猪";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "大";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "小";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "单";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "双";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "红";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "绿";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "蓝";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );

        return betCountinfo;
    }

    public static BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        String     tarCode       = list.get( list.size() - 1 );
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        paijiangTotal = paijiangTotal.add( peiMap.get( getDanShuang( tarCode ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getDaXiao( tarCode ) ) );

        paijiangTotal = paijiangTotal.add( peiMap.get( getShengXiao( tarCode ) ) );

        paijiangTotal = paijiangTotal.add( peiMap.get( getColor( tarCode ) ) );

        return paijiangTotal;
    }

    public static List<String> randomResult(){
        return RandomUtils.randomWeight( 7, new HashMap<>( weightableMap ), true );
    }

    public static Map<String, Object> killResult( Map<String, BigDecimal> prizeMap, Map<String, BigDecimal> betMap, BigDecimal totalBet ) {
        Map<String, Object> resultMap = new HashMap<>();
        if ( CollectionUtils.isEmpty( prizeMap ) || totalBet.compareTo( BigDecimal.ZERO ) == 0 ) {
            resultMap.put( "resultsList", ExBaccarat.randomResult() );
            resultMap.put( "killRate", BigDecimal.ZERO );
            resultMap.put( "totalPrize", BigDecimal.ZERO );
            return resultMap;
        }
        // 先生成10组随机牌
        List<String> randomResult1  = randomResult();
        List<String> randomResult2  = randomResult();
        List<String> randomResult3  = randomResult();
        List<String> randomResult4  = randomResult();
        List<String> randomResult5  = randomResult();
        List<String> randomResult6  = randomResult();
        List<String> randomResult7  = randomResult();
        List<String> randomResult8  = randomResult();
        List<String> randomResult9  = randomResult();
        List<String> randomResult10 = randomResult();

        // 判断10组随机牌谁的派奖最少
        BigDecimal randomResultPrize1  = coutPrize( randomResult1, prizeMap );
        BigDecimal randomResultPrize2  = coutPrize( randomResult2, prizeMap );
        BigDecimal randomResultPrize3  = coutPrize( randomResult3, prizeMap );
        BigDecimal randomResultPrize4  = coutPrize( randomResult4, prizeMap );
        BigDecimal randomResultPrize5  = coutPrize( randomResult5, prizeMap );
        BigDecimal randomResultPrize6  = coutPrize( randomResult6, prizeMap );
        BigDecimal randomResultPrize7  = coutPrize( randomResult7, prizeMap );
        BigDecimal randomResultPrize8  = coutPrize( randomResult8, prizeMap );
        BigDecimal randomResultPrize9  = coutPrize( randomResult9, prizeMap );
        BigDecimal randomResultPrize10 = coutPrize( randomResult10, prizeMap );

        BigDecimal min = randomResultPrize1
                .min( randomResultPrize2 )
                .min( randomResultPrize3 )
                .min( randomResultPrize4 )
                .min( randomResultPrize5 )
                .min( randomResultPrize6 )
                .min( randomResultPrize7 )
                .min( randomResultPrize8 )
                .min( randomResultPrize9 )
                .min( randomResultPrize10 );

        if ( min.compareTo( randomResultPrize1 ) == 0 ) {
            resultMap.put( "resultsList", randomResult1 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize1 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize1 );
        } else if ( min.compareTo( randomResultPrize2 ) == 0 ) {
            resultMap.put( "resultsList", randomResult2 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize2 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize2 );
        } else if ( min.compareTo( randomResultPrize3 ) == 0 ) {
            resultMap.put( "resultsList", randomResult3 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize3 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize3 );
        } else if ( min.compareTo( randomResultPrize4 ) == 0 ) {
            resultMap.put( "resultsList", randomResult4 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize4 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize4 );
        } else if ( min.compareTo( randomResultPrize5 ) == 0 ) {
            resultMap.put( "resultsList", randomResult5 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize5 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize5 );
        } else if ( min.compareTo( randomResultPrize6 ) == 0 ) {
            resultMap.put( "resultsList", randomResult6 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize6 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize6 );
        } else if ( min.compareTo( randomResultPrize7 ) == 0 ) {
            resultMap.put( "resultsList", randomResult7 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize7 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize7 );
        } else if ( min.compareTo( randomResultPrize8 ) == 0 ) {
            resultMap.put( "resultsList", randomResult8 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize8 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize8 );
        } else if ( min.compareTo( randomResultPrize9 ) == 0 ) {
            resultMap.put( "resultsList", randomResult9 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize9 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize9 );
        } else if ( min.compareTo( randomResultPrize10 ) == 0 ) {
            resultMap.put( "resultsList", randomResult10 );
            resultMap.put( "killRate", totalBet.subtract( randomResultPrize10 ).divide( totalBet, 2, RoundingMode.HALF_UP ) );
            resultMap.put( "totalPrize", randomResultPrize10 );
        }
        return resultMap;
    }
}
