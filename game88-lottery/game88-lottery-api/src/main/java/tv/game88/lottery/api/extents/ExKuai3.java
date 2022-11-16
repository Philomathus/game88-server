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
public class ExKuai3 {

    public static       Map<String, Integer>     weightableMap = new HashMap<>();

    static {
        weightableMap.put( "1", 100 );
        weightableMap.put( "2", 100 );
        weightableMap.put( "3", 100 );
        weightableMap.put( "4", 100 );
        weightableMap.put( "5", 100 );
        weightableMap.put( "6", 100 );

    }

    public static BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal   prize   = BigDecimal.ZERO;
        List<String> offList = Arrays.asList( officialSpl );
        String[]     betarrs = betSelect.split( "&" );
        String       des;
        LocalMethod             method  = LotteryCacheUtils.me.getLocalMethod( methodId );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 2 );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( method.getName() ) {
        case "三军":
            for ( String bt : betarrs ) {
                if ( offList.contains( bt ) ) {
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
        case "短牌":
            String result = getDuanPaiReuslt( offList );
            if ( result != null ) {
                for ( String bt : betarrs ) {
                    if ( result.equals( bt ) ) {
                        prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                        break;
                    }
                }
            }
            break;
        }
        return prize;
    }

    public static String getDuanPaiReuslt( List<String> offList ) {
        String               result   = null;
        Map<String, Integer> countMap = new HashMap<>();
        for ( String a : offList ) {
            if ( countMap.containsKey( a ) ) {
                int ta = countMap.get( a ) + 1;
                countMap.put( a, ta );
                if ( ta == 2 ) {
                    result = a;
                }
            } else {
                countMap.put( a, 1 );
            }
        }
        if ( countMap.size() != 2 ) {
            return null;
        }
        return result.concat( "," ).concat( result );
    }


    public static String getZongDanShuang( Integer total ) {
        if ( total % 2 == 0 ) {
            return "双";
        }
        return "单";
    }


    public static String getZongDaXiao( Integer total ) {
        if ( total >= 11 ) {
            return "大";
        }
        return "小";
    }

    public static String concatBetString( Map<String, BigDecimal> betMap ) {
        String sp           = "-";
        String betCountinfo = "";
        String bt           = "1";
        betCountinfo = betCountinfo.concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "2";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "3";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "4";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "5";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "6";

        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "大";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "小";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "单";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "双";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );

        bt           = "1,1";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp11" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "2,2";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp22" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "3,3";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp33" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "4,4";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp44" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "5,5";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp55" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "6,6";
        betCountinfo = betCountinfo.concat( sp ).concat( "dp66" ).concat( ":" ).concat( betMap.get( bt ).toString() );

        return betCountinfo;
    }

    public static BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        for ( String bt : list ) {
            //三军
            paijiangTotal = paijiangTotal.add( peiMap.get( bt ) );
        }
        //短牌
        String result = getDuanPaiReuslt( list );
        if ( result != null ) {
            paijiangTotal = paijiangTotal.add( peiMap.get( result ) );
        }
        //总和
        int total = list.stream().mapToInt( Integer::parseInt ).sum();
        paijiangTotal = paijiangTotal.add( peiMap.get( getZongDanShuang( total ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getZongDaXiao( total ) ) );

        return paijiangTotal;
    }

    public static List<String> randomResult(){
        return RandomUtils.randomWeight( 3, new HashMap<>( weightableMap ), false );
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
