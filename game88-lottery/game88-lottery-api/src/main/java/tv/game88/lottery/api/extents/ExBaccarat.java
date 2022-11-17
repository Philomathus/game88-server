package tv.game88.lottery.api.extents;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.lottery.api.base.AbstractExLottery;
import tv.game88.lottery.api.cache.LotteryCacheUtils;

import java.math.BigDecimal;
import java.util.*;

@Log4j2
@Repository( value = "Ex11Processor" )
public class ExBaccarat extends AbstractExLottery {

    private static final List<String> zeroList = Arrays.asList( "10", "J", "Q", "K" );

    private static final String[] suits      = { "C", "D", "H", "S" };
    private static final String[] cardValues = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };

    public BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect ) {
        BigDecimal              prize   = BigDecimal.ZERO;
        List<String>            offList = Arrays.asList( officialSpl );
        String                  analyse = getBaccaratAnalyse( offList );
        String[]                betarrs = betSelect.split( "&" );
        Map<String, BigDecimal> oddsMap = LotteryCacheUtils.me.getOddsMap( 11 );
        for ( String bt : betarrs ) {
            if ( analyse.equals( bt ) ) {
                prize = prize.add( chip.multiply( oddsMap.get( bt ) ) );
            }
            if ( analyse.equals( "和" ) && ( bt.equals( "闲" ) || bt.equals( "庄" ) ) ) {
                prize = prize.add( chip );
            }
            // 闲对
            if ( isPairs( officialSpl[ 0 ] ) && bt.equals( "闲对" ) ) {
                prize = prize.add( chip.multiply( oddsMap.get( "闲对" ) ) );
            }
            // 庄对
            if ( isPairs( officialSpl[ 1 ] ) && bt.equals( "庄对" ) ) {
                prize = prize.add( chip.multiply( oddsMap.get( "庄对" ) ) );
            }
        }
        return prize;
    }

    public String concatBetString( Map<String, BigDecimal> betMap ) {
        return null;
    }

    public BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap ) {
        String     detailAnalyse = getBaccaratAnalyse( list );
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        //System.out.println(detailAnalyse);
        paijiangTotal = paijiangTotal.add( peiMap.get( detailAnalyse ) );
        if ( detailAnalyse.equals( "和" ) ) {
            paijiangTotal = paijiangTotal.add( peiMap.get( "庄和" ) );
            paijiangTotal = paijiangTotal.add( peiMap.get( "闲和" ) );
            //System.out.println("庄和 闲和");
        }
        // 闲对
        if ( isPairs( list.get( 0 ) ) ) {
            paijiangTotal = paijiangTotal.add( peiMap.get( "闲对" ) );
            //System.out.println("闲对");
        }
        // 庄对
        if ( isPairs( list.get( 1 ) ) ) {
            paijiangTotal = paijiangTotal.add( peiMap.get( "庄对" ) );
            //System.out.println("庄对");
        }
        return paijiangTotal;
    }

    public List<String> randomResult() {
        List<String> deck = new ArrayList<>( 52 );
        for ( String suit : suits ) {
            for ( String cardValue : cardValues ) {
                deck.add( cardValue + suit );
            }
        }
        List<String> playShuffledDeck = new ArrayList<>();
        Random       chooser          = new Random();
        //生成闲家手牌
        for ( int i = 0; i < 2; i++ ) {
            int playSelection = chooser.nextInt( deck.size() );
            playShuffledDeck.add( deck.remove( playSelection ) );
        }
        //生成庄家手牌
        LinkedList<String> bankShuffledDeck = new LinkedList<>();
        for ( int i = 0; i < 2; i++ ) {
            int bankSelection = chooser.nextInt( deck.size() );
            bankShuffledDeck.add( deck.remove( bankSelection ) );
        }
        //先判定闲家和庄家是否为例牌 例牌直接定胜负
        if ( isNatural( playShuffledDeck ) || isNatural( bankShuffledDeck ) ) {
            //返回结果集
            return Arrays.asList( String.join( ",", playShuffledDeck ), String.join( ",", bankShuffledDeck ) );
        }
        //闲家是否有补牌
        int playerCard = 0;
        //判定闲家牌大小  小于6则补一张牌
        if ( isOutsourcingByPlayer( playShuffledDeck ) ) {
            int playSelection = chooser.nextInt( deck.size() );
            playShuffledDeck.add( deck.remove( playSelection ) );

            //获取闲家补的牌
            playerCard = countCard( playShuffledDeck.get( playShuffledDeck.size() - 1 ) );
        }
        if ( isOutsourcingByBank( bankShuffledDeck, playerCard ) ) {
            int bankSelection = chooser.nextInt( deck.size() );
            bankShuffledDeck.add( deck.remove( bankSelection ) );
        }
        // 计算总和
        //        int playerTotal = countTotal(playShuffledDeck);
        //        int bankerTotal = countTotal(bankShuffledDeck);
        //        // 控制开合几率
        //        if (playerTotal == bankerTotal && chooser.nextInt(5) == 4) {
        //            return randomResult();
        //        }
        return Arrays.asList( String.join( ",", playShuffledDeck ), String.join( ",", bankShuffledDeck ) );
    }

    private static int countCard( String card ) {
        String numStr;
        if ( card.length() == 2 ) {
            numStr = card.substring( 0, 1 );
        } else {
            numStr = card.substring( 0, 2 );
        }
        if ( !zeroList.contains( numStr ) ) {
            return Integer.parseInt( numStr );
        }
        return 0;
    }

    /**
     * 计算总和
     */
    private static int countTotal( List<String> shuffledDeck ) {
        int totel = 0;
        for ( String s : shuffledDeck ) {
            String numStr;
            if ( s.length() == 2 ) {
                numStr = s.substring( 0, 1 );
            } else {
                numStr = s.substring( 0, 2 );
            }
            if ( !zeroList.contains( numStr ) ) {
                totel += Integer.parseInt( numStr );
            }
        }
        return totel % 10;
    }

    /**
     * 判定是否是对子
     *
     * @param list
     */
    public static Boolean isPairs( List<String> list ) {
        String oneCard;
        String twoCard;
        if ( list.get( 0 ).length() == 2 ) {
            oneCard = list.get( 0 ).substring( 0, 1 );
        } else {
            oneCard = list.get( 0 ).substring( 0, 2 );
        }
        if ( list.get( 1 ).length() == 2 ) {
            twoCard = list.get( 1 ).substring( 0, 1 );
        } else {
            twoCard = list.get( 1 ).substring( 0, 2 );
        }
        return oneCard.equals( twoCard );
    }

    public static Boolean isPairs( String result ) {
        return isPairs( Arrays.asList( result.split( "," ) ) );
    }

    /**
     * 判定百家乐牌型是否是例牌
     *
     * @param shuffledDeck
     */
    private static boolean isNatural( List<String> shuffledDeck ) {
        return countTotal( shuffledDeck ) > 7;
    }

    /**
     * 判定闲家是否需要补牌
     *
     * @param shuffledDeck
     */
    private static boolean isOutsourcingByPlayer( List<String> shuffledDeck ) {
        return countTotal( shuffledDeck ) < 6;
    }


    /**
     * 判定庄家是否需要补牌
     *
     * @param shuffledDeck 庄家手牌
     * @param card         闲家补的第三张牌
     */
    private static boolean isOutsourcingByBank( List<String> shuffledDeck, int card ) {
        int total = countTotal( shuffledDeck );
        //如果小于三 补一张牌
        if ( total < 3 ) {
            return true;
        }
        if ( total >= 7 ) {
            return false;
        }
        //如果闲家补得第三张牌是8点，不须补牌，其他则需补牌
        if ( total == 3 && card == 8 ) {
            return false;
        }
        //如果闲家补得第三张牌是0,1,8,9点，不须补牌，其他则需补牌
        List<Integer> fourList = Arrays.asList( 0, 1, 8, 9 );
        if ( total == 4 && fourList.contains( card ) ) {
            return false;
        }
        //如果闲家补得第三张牌是0,1,2,3,8,9点，不须补牌，其他则需补牌
        List<Integer> fifList = Arrays.asList( 0, 1, 2, 3, 8, 9 );
        if ( total == 5 && fifList.contains( card ) ) {
            return false;
        }
        //如果闲家需补牌,而补得第三张牌是6或7点，补一张牌，其他则不需补牌
        List<Integer> etnList = Arrays.asList( 6, 7 );
        return total != 6 || etnList.contains( card );
    }

    /**
     * 获取常规百家乐Analyse
     */
    public static String getBaccaratAnalyse( List<String> resultList ) {
        List<String> playerResultList = Arrays.asList( resultList.get( 0 ).split( "," ) );
        List<String> bankerResultList = Arrays.asList( resultList.get( 1 ).split( "," ) );
        //计算总和
        int playerTotal = countTotal( playerResultList );
        int bankerTotal = countTotal( bankerResultList );
        if ( bankerTotal > playerTotal ) {
            return "庄";
        } else if ( bankerTotal < playerTotal ) {
            return "闲";
        } else {
            return "和";
        }
    }

    /**
     * 获取百家乐赢家牌型
     */
    public static String getBaccaratWinBrand( List<String> resultList ) {
        List<String> playerResultList = Arrays.asList( resultList.get( 0 ).split( "," ) );
        List<String> bankerResultList = Arrays.asList( resultList.get( 1 ).split( "," ) );
        //计算总和
        int playerTotal = countTotal( playerResultList );
        int bankerTotal = countTotal( bankerResultList );
        if ( bankerTotal > playerTotal ) {
            return resultList.get( 1 ); // 庄
        } else if ( bankerTotal < playerTotal ) {
            return resultList.get( 0 ); // 闲
        } else {
            return resultList.get( 1 ); // 庄
        }
    }

    /**
     * 根据游戏结果获取描述： player:1 2 3
     *
     * @param code
     */
    public static Map<String, String> getBaccaratResults( String code ) {
        Map<String, String> map              = new HashMap<>();
        String              playerCard       = code.substring( 0, code.indexOf( " " ) );
        String              bankerCard       = code.substring( code.indexOf( " " ) + 1 );
        List<String>        playerResultList = Arrays.asList( playerCard.split( "," ) );
        List<String>        bankerResultList = Arrays.asList( bankerCard.split( "," ) );
        int                 playerTotal      = countTotal( playerResultList );
        int                 bankerTotal      = countTotal( bankerResultList );
        if ( bankerTotal > playerTotal ) {
            map.put( "PlayOrBank", "庄" );
            map.put( "result", bankerCard );
        } else if ( playerTotal > bankerTotal ) {
            map.put( "PlayOrBank", "闲" );
            map.put( "result", playerCard );
        } else {
            map.put( "PlayOrBank", "和" );
            map.put( "result", bankerCard );
        }
        return map;
    }
}
