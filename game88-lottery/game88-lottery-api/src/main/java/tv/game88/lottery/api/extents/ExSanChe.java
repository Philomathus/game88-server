package tv.game88.lottery.api.extents;


import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.common.utils.RandomUtils;
import tv.game88.lottery.api.base.AbstractExLottery;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.LocalMethod;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = "Ex3Processor" )
public class ExSanChe extends AbstractExLottery {

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
    }

    public BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal              prize   = BigDecimal.ZERO;
        String[]                betarrs = betSelect.split( "&" );
        LocalMethod             method  = LotteryCacheUtils.me.getLocalMethod( methodId );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 3 );
        if ( method == null ) {
            log.error( "非法投注:" + methodId );
            return BigDecimal.ZERO;
        }
        switch ( method.getName() ) {
        case "冠军单码":
            String tarCode = officialSpl[ 0 ];
            for ( String bt : betarrs ) {
                if ( !tarCode.equals( bt ) ) {
                    continue;
                }
                prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
                break;
            }
            break;
        case "冠军两面":
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
                    continue;
                }
            }
            break;
        case "冠亚和":
            int total = Integer.parseInt( officialSpl[ 0 ] ) + Integer.parseInt( officialSpl[ 1 ] );
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
                    continue;
                }
            }
            break;
        }
        return prize;
    }

    public static String getFirstDanShuang( String first ) {
        return Integer.parseInt( first ) % 2 == 0 ? "双" :"单";
    }

    public static String getFirstDaXiao( String first ) {
        return Integer.parseInt( first ) >= 6 ? "大" :"小";
    }

    public static String getZongDanShuang( Integer total ) {
        return total % 2 == 0 ? "和双" : "和单";
    }


    public static String getZongDaXiao( Integer total ) {
        return total >= 12 ? "和大" : "和小";
    }

    public String concatBetString( Map<String, BigDecimal> betMap ) {
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
        bt           = "和单";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "和双";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "和大";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "和小";
        betCountinfo = betCountinfo.concat( sp ).concat( bt ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "单";
        betCountinfo = betCountinfo.concat( sp ).concat( "A单" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "双";
        betCountinfo = betCountinfo.concat( sp ).concat( "A双" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "大";
        betCountinfo = betCountinfo.concat( sp ).concat( "A大" ).concat( ":" ).concat( betMap.get( bt ).toString() );
        bt           = "小";
        betCountinfo = betCountinfo.concat( sp ).concat( "A小" ).concat( ":" ).concat( betMap.get( bt ).toString() );

        return betCountinfo;
    }

    public BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        //冠军单码
        paijiangTotal = paijiangTotal.add( peiMap.get( list.get( 0 ) ) );

        //冠军两面
        paijiangTotal = paijiangTotal.add( peiMap.get( getFirstDanShuang( list.get( 0 ) ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getFirstDaXiao( list.get( 0 ) ) ) );
        //冠亚和
        int total = Integer.parseInt( list.get( 0 ) ) + Integer.parseInt( list.get( 1 ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getZongDanShuang( total ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getZongDaXiao( total ) ) );
        return paijiangTotal;
    }

    public List<String> randomResult(){
        return RandomUtils.randomWeight( 10, new HashMap<>( weightableMap ), true );
    }
}
