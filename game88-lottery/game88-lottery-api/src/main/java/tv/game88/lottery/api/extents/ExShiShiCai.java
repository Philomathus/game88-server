package tv.game88.lottery.api.extents;


import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.LocalMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ExShiShiCai {

    public static Map<String, Integer> weightableMap = new HashMap<>();

    static {
        weightableMap.put( "0", 100 );
        weightableMap.put( "1", 100 );
        weightableMap.put( "2", 100 );
        weightableMap.put( "3", 100 );
        weightableMap.put( "4", 100 );
        weightableMap.put( "5", 100 );
        weightableMap.put( "6", 100 );
        weightableMap.put( "7", 100 );
        weightableMap.put( "8", 100 );
        weightableMap.put( "9", 100 );

    }

    public static BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal              prize   = BigDecimal.ZERO;
        String[]                betarrs = betSelect.split( "&" );
        String                  des     = "";
        LocalMethod             method  = LotteryCacheUtils.me.getLocalMethod( methodId );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 0 );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( method.getName() ) {
        case "万位VS个位":
            String tarCode = officialSpl[ 0 ];
            String tail = officialSpl[ officialSpl.length - 1 ];
            des = longHuHe( tarCode, tail );
            for ( String bt : betarrs ) {
                if ( !des.equals( bt ) ) {
                    continue;
                }
                prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                break;
            }
            break;
        case "万位":
            for ( String bt : betarrs ) {
                //单双
                des = getFirstDanShuang( officialSpl[ 0 ] );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
                //大小
                des = getFirstDaXiao( officialSpl[ 0 ] );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
                //质和
                des = getFirstZhiHe( officialSpl[ 0 ] );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
            }
            break;
        case "佰位":
            for ( String bt : betarrs ) {
                if ( officialSpl[ 2 ].equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    break;
                }
            }
            break;
        }
        return prize;
    }

    public static String longHuHe( String head, String end ) {
        int com = head.compareTo( end );
        if ( com > 0 ) {
            return "龙";
        } else if ( com < 0 ) {
            return "虎";
        } else {
            return "和";
        }
    }

    public static String getFirstDanShuang( String first ) {
        if ( Integer.parseInt( first ) % 2 == 0 ) {
            return "双";
        }
        return "单";
    }

    public static String getFirstDaXiao( String first ) {
        return Integer.parseInt( first ) >= 5 ? "大" : "小";

    }

    public static String getFirstZhiHe( String first ) {
        boolean contains = Arrays.asList( "1", "2", "3", "5", "7" ).contains( first );
        return contains ? "质" : "合";
    }

    public static String getZongDanShuang( List<String> list ) {
        int total = list.stream().mapToInt( Integer::parseInt ).sum();
        if ( total % 2 == 0 ) {
            return "和双";
        }
        return "和单";
    }

    public static String getZongDanShuang( Integer total ) {
        if ( total % 2 == 0 ) {
            return "和双";
        }
        return "和单";
    }

    public static String getZongDaXiao( List<String> list ) {
        int total = list.stream().mapToInt( Integer::parseInt ).sum();
        if ( total >= 12 ) {
            return "和大";
        }
        return "和小";
    }

    public static String getZongDaXiao( Integer total ) {
        if ( total >= 12 ) {
            return "和大";
        }
        return "和小";
    }

    public static String concatBetString( Map<String, BigDecimal> betMap ) {
        String[] keys   = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "单", "双", "大", "小", "质", "合", "龙", "虎", "和" };
        String[] values = new String[ keys.length ];
        for ( int i = 0; i < keys.length; i++ ) {
            values[ i ] = betMap.getOrDefault( keys[ i ], BigDecimal.ZERO ).setScale( 2, RoundingMode.HALF_UP ).toString();
        }
        return String.format( "0:%s-1:%s-2:%s-3:%s-4:%s-5:%s-6:%s-7:%s-8:%s-9:%s-单:%s-双:%s-大:%s-小:%s-质:%s-合:%s-龙:%s-虎:%s-和:%s",
                values );
    }

    public static BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        //第一球两面
        paijiangTotal = paijiangTotal.add( peiMap.get( getFirstDanShuang( list.get( 0 ) ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getFirstDaXiao( list.get( 0 ) ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getFirstZhiHe( list.get( 0 ) ) ) );
        //佰位
        paijiangTotal = paijiangTotal.add( peiMap.get( list.get( 2 ) ) );

        //第一球VS第五球
        paijiangTotal = paijiangTotal.add( peiMap.get( longHuHe( list.get( 0 ), list.get( list.size() - 1 ) ) ) );

        return paijiangTotal;
    }

    public static List<String> randomResult(){
        return RandomUtils.randomWeight( 5, new HashMap<>( weightableMap ), false );
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
