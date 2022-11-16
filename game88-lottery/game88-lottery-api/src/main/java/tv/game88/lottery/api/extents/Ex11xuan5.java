package tv.game88.lottery.api.extents;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class Ex11xuan5 {

    public static Map<String, Integer> weightableMap = new HashMap<>();

    static {
        weightableMap.put( "01", 100 );
        weightableMap.put( "02", 100 );
        weightableMap.put( "03", 100 );
        weightableMap.put( "04", 100 );
        weightableMap.put( "05", 100 );
        weightableMap.put( "06", 100 );
        weightableMap.put( "07", 100 );
        weightableMap.put( "08", 100 );
        weightableMap.put( "09", 100 );
        weightableMap.put( "10", 100 );
        weightableMap.put( "11", 100 );
    }

    public static BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal              prize   = BigDecimal.ZERO;
        String[]                betarrs = betSelect.split( "&" );
        LocalMethod             method  = LotteryCacheUtils.me.getLocalMethod( methodId );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 1 );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( method.getName() ) {
        case "第一球":
            String tarCode = officialSpl[ 0 ];
            for ( String bt : betarrs ) {
                if ( !tarCode.equals( bt ) ) {
                    continue;
                }
                prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                break;
            }
            break;
        case "第一球两面":
            String des = "";
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
                }
            }
            break;
        case "总和":
            int total = Arrays.stream( officialSpl ).mapToInt( Integer::parseInt ).sum();
            for ( String bt : betarrs ) {
                //单双
                des = getZongDanShuang( total );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                    continue;
                }
                //大小
                des = getZongDaXiao( total );
                if ( des.equals( bt ) ) {
                    prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                }
            }
            break;
        }
        return prize;
    }

    public static String getFirstDanShuang( String first ) {
        if ( Integer.parseInt( first ) % 2 == 0 ) {
            return "双";
        }
        return "单";
    }

    public static String getFirstDaXiao( String first ) {
        if ( Integer.parseInt( first ) >= 6 ) {
            return "大";
        }
        return "小";
    }

    public static String getZongDanShuang( List<String> list ) {
        int total = list.stream().mapToInt( Integer::parseInt ).sum();
        if ( total % 2 == 0 ) {
            return "总双";
        }
        return "总单";
    }

    public static String getZongDanShuang( Integer total ) {
        if ( total % 2 == 0 ) {
            return "总双";
        }
        return "总单";
    }

    public static String getZongDaXiao( List<String> list ) {
        int total = list.stream().mapToInt( Integer::parseInt ).sum();
        if ( total >= 31 ) {
            return "总大";
        }
        return "总小";
    }

    public static String getZongDaXiao( Integer total ) {
        if ( total >= 31 ) {
            return "总大";
        }
        return "总小";
    }

    public static String concatBetString( Map<String, BigDecimal> betMap ) {
        String sp           = "-";
        String betCountinfo = "";
        String bt           = "01";
        betCountinfo = betCountinfo.concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "02";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "03";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "04";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "05";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "06";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "07";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "08";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "09";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "10";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "11";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "单";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "双";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "大";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "小";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "总单";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "总双";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "总大";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "总小";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );

        return betCountinfo;
    }

    public static BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        paijiangTotal = paijiangTotal.add( peiMap.get( list.get( 0 ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( Ex11xuan5.getFirstDanShuang( list.get( 0 ) ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( Ex11xuan5.getFirstDaXiao( list.get( 0 ) ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( Ex11xuan5.getZongDanShuang( list ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( Ex11xuan5.getZongDaXiao( list ) ) );
        return paijiangTotal;
    }

    public static List<String> randomResult(){
        return RandomUtils.randomWeight( 5, new HashMap<>( Ex11xuan5.weightableMap ), true );
    }

    public static Map<String, Object> killResult( Map<String, BigDecimal> prizeMap, Map<String, BigDecimal> betMap,
                                                  BigDecimal totalBet ) {
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
