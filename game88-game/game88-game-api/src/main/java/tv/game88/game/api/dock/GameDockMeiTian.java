package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MEITIAN + "GameProcessor" )
public class GameDockMeiTian extends AbstractGameDock {

    private static final String CREATE_USER    = "/services/dg/player/playerCreate2";
    private static final String QUERY_BALANCE  = "/services/dg/player/getPlayerBalance";
    private static final String TO_DEPOSIT     = "/services/dg/player/deposit2";
    private static final String TO_WITHDRAW    = "/services/dg/player/withdraw2";
    private static final String LOGIN          = "/services/dg/player/playerPlatformUrl";
    private static final String QUERY_TRANSFER = "/services/dg/player/queryTransbyId";
    private static final String LOG_OUT        = "/services/dg/player/logOutGame";

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        Map<String, String> rawData = new HashMap<>();
        rawData.put( "nickname", reqJoinGame.getGameMemberId() );
        rawData.put( "playerLevel", "0" );
        String rawDataStr = JsonUtil.object2Json( rawData );

        StringBuilder url = new StringBuilder( reqJoinGame.getApiUrl() + CREATE_USER );
        url
                .append( "/" )
                .append( reqJoinGame.getGameMemberId() )
                .append( "/" )
                .append( reqJoinGame.getAgent() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getGameMemberId() ).toLowerCase() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getMd5() + rawDataStr ).toLowerCase() )
                .append( "/" )
                .append( Base64.toBase64String( rawDataStr.getBytes() ) );
        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, String>             resultMap   = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String resultCode = resultMap.get( "resultCode" );
            if ( "1".equals( resultCode ) || "5".equals( resultCode ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {

        Map<String, String> rawData = new HashMap<>();
        if ( Arrays.asList( "0", "1", "2", "3", "4" ).contains( reqJoinGame.getKindId() ) ) {
            rawData.put( "gameHall", reqJoinGame.getKindId() );
        } else {
            rawData.put( "gameCode", reqJoinGame.getKindId() );
            rawData.put( "roomID", "" );
        }
        rawData.put( "lang ", "ZH-CN" );
        String rawDataStr = JsonUtil.object2Json( rawData );

        StringBuilder url = new StringBuilder( reqJoinGame.getApiUrl() + LOGIN );
        url
                .append( "/" )
                .append( reqJoinGame.getAgent() )
                .append( "/" )
                .append( reqJoinGame.getGameMemberId() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getGameMemberId() ).toLowerCase() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getMd5() + rawDataStr ).toLowerCase() )
                .append( "/" )
                .append( Base64.toBase64String( rawDataStr.getBytes() ) );

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, String>             resultMap   = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.get( "resultCode" ) ) && resultMap.containsKey( "url" ) ) {
                reqJoinGame.setGameUrl( resultMap.get( "url" ) );
                log.warn( reqJoinGame.getGameUrl() );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + " 获取游戏链接失败:{}; userId:{}; url:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId(), url );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        Map<String, String> rawData = new LinkedHashMap<>();
        rawData.put( "merchantId", reqJoinGame.getAgent() );
        rawData.put( "playerName", reqJoinGame.getGameMemberId() );
        rawData.put( "extTransId", reqJoinGame.getOrderId() );
        rawData.put( "coins", reqJoinGame.getTransferMoney().setScale( 4, RoundingMode.HALF_UP ).toString() );
        String rawDataStr = JsonUtil.object2Json( rawData );

        StringBuilder url = new StringBuilder( reqJoinGame.getApiUrl() + TO_DEPOSIT );
        url
                .append( "/" )
                .append( reqJoinGame.getAgent() )
                .append( "/" )
                .append( reqJoinGame.getGameMemberId() )
                .append( "/" )
                .append( reqJoinGame.getTransferMoney().setScale( 4, RoundingMode.HALF_UP ).toString() )
                .append( "/" )
                .append( reqJoinGame.getOrderId() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getMd5() + rawDataStr ) )
                .append( "/" )
                .append( Base64.toBase64String( rawDataStr.getBytes() ) );
        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, Object>             resultMap;
        try {
            resultMap = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !"1".equals( resultMap.getOrDefault( "resultCode", -1 ).toString() ) ) {
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + " 上分异常 - 上分失败或数据为空" );
        }
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        Map<String, String> rawData = new LinkedHashMap<>();
        rawData.put( "merchantId", reqJoinGame.getAgent() );
        rawData.put( "playerName", reqJoinGame.getGameMemberId() );
        rawData.put( "extTransId", reqJoinGame.getOrderId() );
        rawData.put( "coins", reqJoinGame.getTransferMoney().setScale( 4, RoundingMode.HALF_UP ).toString() );
        String rawDataStr = JsonUtil.object2Json( rawData );

        StringBuilder url = new StringBuilder( reqJoinGame.getApiUrl() + TO_WITHDRAW );
        url
                .append( "/" )
                .append( reqJoinGame.getAgent() )
                .append( "/" )
                .append( reqJoinGame.getGameMemberId() )
                .append( "/" )
                .append( reqJoinGame.getTransferMoney().setScale( 4, RoundingMode.HALF_UP ).toString() )
                .append( "/" )
                .append( reqJoinGame.getOrderId() )
                .append( "/" )
                .append( DigestUtils.md5Hex( reqJoinGame.getMd5() + rawDataStr ) )
                .append( "/" )
                .append( Base64.toBase64String( rawDataStr.getBytes() ) );
        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, Object>             resultMap;
        try {
            resultMap = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !"1".equals( resultMap.getOrDefault( "resultCode", -1 ).toString() ) ) {
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + " 下分异常 - 下分失败或数据为空" );
        }
    }

    @Retryable( retryFor = Exception.class, noRetryFor = GameTransferException.class, backoff = @Backoff( delay = 2000 ),
            maxAttempts = 3 )
    protected void kickMember( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + LOG_OUT + "/" + reqJoinGame.getAgent() + "/" + reqJoinGame.getGameMemberId();

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );

        Map<String, String> resultMap = restTemplate.postForObject( url, httpEntity, Map.class );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "强制登出玩家 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.get( "resultCode" ) ) ) {
                return;
            }
            throw new RuntimeException( JsonUtil.object2Json( resultMap ) );
        }
        throw new RuntimeException( reqJoinGame.getGameCategory().getDes() + "强制登出玩家失败" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() != null && reqJoinGame.getMoneyType() == 2 ) { // 提现时必须登出玩家,否则无法下分
            SpringUtils.getBean( GameDockMeiTian.class ).kickMember( reqJoinGame );
        }

        String url = reqJoinGame.getApiUrl() + QUERY_BALANCE + "/" + reqJoinGame.getGameMemberId() + "/" + reqJoinGame.getAgent();

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );

        Map<String, String> resultMap = restTemplate.postForObject( url, httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.get( "resultCode" ) ) ) {
                return new BigDecimal( resultMap.get( "coinBalance" ) ).setScale( 2, RoundingMode.FLOOR );
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + QUERY_TRANSFER + "/" + reqJoinGame.getGameMemberId() + "/" + reqJoinGame.getAgent()
                + "/" + reqJoinGame.getOrderId();

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, String>             resultMap   = restTemplate.postForObject( url, httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            return StringUtils.equals( "1", resultMap.get( "resultCode" ) )
                    && StringUtils.equals( "1", resultMap.get( "status" ) );
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
