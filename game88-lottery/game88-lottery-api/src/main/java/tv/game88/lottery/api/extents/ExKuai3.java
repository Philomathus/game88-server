package tv.game88.lottery.api.extents;

import lombok.extern.slf4j.Slf4j;
import tv.game88.lottery.api.dto.LocalMethod;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExKuai3 {

    public static       Map<String, Integer>     weightableMap = new HashMap<>();
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap    = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal>  oddsMap       = new HashMap<>();

    static {
        weightableMap.put( "1", 100 );
        weightableMap.put( "2", 100 );
        weightableMap.put( "3", 100 );
        weightableMap.put( "4", 100 );
        weightableMap.put( "5", 100 );
        weightableMap.put( "6", 100 );

    }

    public static BigDecimal handle( String methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal   prize   = BigDecimal.ZERO;
        List<String> offList = Arrays.asList( officialSpl );
        String[]     betarrs = betSelect.split( "&" );
        String       des;
        LocalMethod  method  = methodsMap.get( methodId );
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
}
