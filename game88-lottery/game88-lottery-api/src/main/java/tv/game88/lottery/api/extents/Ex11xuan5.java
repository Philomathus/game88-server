package tv.game88.lottery.api.extents;

import lombok.extern.slf4j.Slf4j;
import tv.game88.lottery.api.dto.LocalMethod;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Ex11xuan5 {

    public static       Map<String, Integer>     weightableMap = new HashMap<>();
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap    = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal>  oddsMap       = new HashMap<>();

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

    public static BigDecimal handle( String methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal  prize   = BigDecimal.ZERO;
        String[]    betarrs = betSelect.split( "&" );
        LocalMethod method  = methodsMap.get( methodId );
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
}
