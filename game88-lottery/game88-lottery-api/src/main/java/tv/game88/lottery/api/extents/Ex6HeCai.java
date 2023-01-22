package tv.game88.lottery.api.extents;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.common.utils.RandomUtils;
import tv.game88.lottery.api.base.AbstractExLottery;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.LocalMethod;
import tv.game88.lottery.api.utils.LunarAnimalUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = "Ex4Processor" )
public class Ex6HeCai extends AbstractExLottery {
    //methodID:methods
    public static Map<String, Integer> weightableMap = new HashMap<>();

    static {
        for ( int i = 1; i <= 49; i++ ) {
            weightableMap.put( String.format( "%02d", i ), 100 );
        }
    }

    public BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
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
        String   localDate = LocalDate.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );
        String   animal    = LunarAnimalUtils.getAnimal( localDate );
        String[] animals   = LunarAnimalUtils.getLeftOverAnimals( animal );
        return switch ( Integer.parseInt( code ) ) {
            case 1, 13, 25, 37, 49 -> animals[ 0 ];
            case 2, 14, 26, 38 -> animals[ 1 ];
            case 3, 15, 27, 39 -> animals[ 2 ];
            case 4, 16, 28, 40 -> animals[ 3 ];
            case 5, 17, 29, 41 -> animals[ 4 ];
            case 6, 18, 30, 42 -> animals[ 5 ];
            case 7, 19, 31, 43 -> animals[ 6 ];
            case 8, 20, 32, 44 -> animals[ 7 ];
            case 9, 21, 33, 45 -> animals[ 8 ];
            case 10, 22, 34, 46 -> animals[ 9 ];
            case 11, 23, 35, 47 -> animals[ 10 ];
            case 12, 24, 36, 48 -> animals[ 11 ];
            default -> "";
        };

    }

    public String concatBetString( Map<String, BigDecimal> betMap ) {
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

    public BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        String     tarCode       = list.get( list.size() - 1 );
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        paijiangTotal = paijiangTotal.add( peiMap.get( getDanShuang( tarCode ) ) );
        paijiangTotal = paijiangTotal.add( peiMap.get( getDaXiao( tarCode ) ) );

        paijiangTotal = paijiangTotal.add( peiMap.get( getShengXiao( tarCode ) ) );

        paijiangTotal = paijiangTotal.add( peiMap.get( getColor( tarCode ) ) );

        return paijiangTotal;
    }

    public List<String> randomResult() {
        return RandomUtils.randomWeight( 7, new HashMap<>( weightableMap ), true );
    }
}
