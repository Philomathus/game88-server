package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Repository( value = ConstantsGame.RR + ConstantsGame.GAME_PULL_PROCESSOR )
public class GamePullDockRR extends AbstractGamePull {

    private static final Map<String, BigDecimal> RATE_MAP = Map.of( "IDR", BigDecimal.ONE, "INR", BigDecimal.ONE );

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        final Map<String, Object> params = new TreeMap<>();
        params.put( "traderId", gamePlatform.getAgent() );
        params.put( "transitionId", gamePlatform.getVersionValue() );
        params.put( "dateTime", System.currentTimeMillis() );
        params.put( "size", 50000 );
        params.put( "cert", getHash( params, gamePlatform.getMd5() ) );

        String url = String.format( "%s/trader/gameHistory/api/getTransactions", gamePlatform.getApiUrl() );

        final Map<String, Object> resultMap = this.sendPostMap( url, packageJson( params ) );

        // log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "status", "-1" ).toString() ) ) {
                List<Object> transactions = ( List<Object> ) resultMap.getOrDefault( "data", new ArrayList<>() );
                if ( !CollectionUtils.isEmpty( transactions ) ) {
                    Map<String, Object> last = ( Map<String, Object> ) transactions.getLast();
                    // 状态正常,无论是否有数据,从结束时间开始查询
                    gamePlatform.setVersionValue( String.valueOf( last.get( "id" ) ) );
                }
                return transactions;
            }
        }
        log.error( url + ":::" + JsonUtil.object2Json( resultMap ) );
        return null;
    }

    private String getHash( final Map<String, Object> params, final String secret ) {
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        sb.append( "key=" ).append( secret );
        String param = sb.toString();
        log.warn( param );
        return DigestUtils.md5Hex( param );
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        GameDataRecord      gameDataRecord  = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "id" ) ) );
        String   logId    = this.createRecordId( gamePlatform, gameDataRecord.getGameId() );
        String[] accounts = assemblyAccount( String.valueOf( remoteGameDatum.get( "user_id" ) ) );
        if ( StringUtils.isEmpty( accounts ) ) {
            log.error( "accounts is empty - data:{}", JsonUtil.object2Json( remoteGameDatum ) );
            return null;
        }
        gameDataRecord.setAgent( accounts[ 0 ] );
        gameDataRecord.setAccount( accounts[ 1 ] );
        gameDataRecord.setId( logId );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "game_id" ) ) );
        String     currency = String.valueOf( remoteGameDatum.get( "currency" ) );
        BigDecimal RATE     = RATE_MAP.get( currency );

        BigDecimal chip = new BigDecimal( remoteGameDatum.get( "chip" ).toString() ).multiply( RATE );
        gameDataRecord.setCellScore( chip.toString() );
        gameDataRecord.setAllBet( chip.toString() );
        BigDecimal allGetMoney = new BigDecimal( remoteGameDatum.getOrDefault( "allgetmoney", "0" ).toString() ).multiply( RATE );
        gameDataRecord.setProfit( allGetMoney.subtract( chip ).toString() );

        String createTime = remoteGameDatum.get( "create_time" ).toString();
        String payoffTime = remoteGameDatum.get( "payoff_time" ).toString();
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC7ToDefault( createTime,
                LocalDateTimeUtils.RFC3339_NOMZ_FORMATTER ) ) );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( LocalDateTimeUtils.convertUTC7ToDefault( payoffTime,
                LocalDateTimeUtils.RFC3339_NOMZ_FORMATTER ) ) );
        gameDataRecord.setCurrency( currency );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }
}
