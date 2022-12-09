package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MEITIAN + "GameProcessor" )
public class GameButtMeiTian extends AbstractGameButt {

    private static final String CREATE_USER    = "/services/dg/player/playerCreate2";
    private static final String QUERY_BALANCE  = "/services/dg/player/getPlayerBalance";
    private static final String TO_DEPOSIT     = "/services/dg/player/deposit2";
    private static final String TO_WITHDRAW    = "/services/dg/player/withdraw2";
    private static final String LOGIN          = "/services/dg/player/playerPlatformUrl";
    private static final String QUERY_TRANSFER = "/services/dg/player/queryTransbyId";

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
                .append( Base64Utils.encodeToString( rawDataStr.getBytes() ) );
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
        log.error( "meitian 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( "meitian - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {

        Map<String, String> rawData = new HashMap<>();
        rawData.put( "gameHall", reqJoinGame.getKindId() );
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
                .append( Base64Utils.encodeToString( rawDataStr.getBytes() ) );

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, String>             resultMap   = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.get( "resultCode" ) ) && resultMap.containsKey( "url" ) ) {
                reqJoinGame.setGameUrl( resultMap.get( "url" ) );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( "meitian 获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
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
                .append( Base64Utils.encodeToString( rawDataStr.getBytes() ) );
        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, Object>             resultMap;
        try {
            resultMap = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( "meitian上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !"1".equals( resultMap.getOrDefault( "resultCode", -1 ).toString() ) ) {
            throw new GameTransferException( "meitian 上分异常 - 上分失败或数据为空" );
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
                .append( Base64Utils.encodeToString( rawDataStr.getBytes() ) );
        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );
        Map<String, Object>             resultMap;
        try {
            resultMap = restTemplate.postForObject( url.toString(), httpEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( "meitian下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( CollectionUtils.isEmpty( resultMap ) || !"1".equals( resultMap.getOrDefault( "resultCode", -1 ).toString() ) ) {
            throw new GameTransferException( "meitian 下分异常 - 下分失败或数据为空" );
        }
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + QUERY_BALANCE + "/" + reqJoinGame.getGameMemberId() + "/" + reqJoinGame.getAgent();

        HttpHeaders                     httpHeaders = new HttpHeaders();
        HttpEntity<Map<String, String>> httpEntity  = new HttpEntity<>( httpHeaders );

        Map<String, String> resultMap = restTemplate.postForObject( url, httpEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "1", resultMap.get( "resultCode" ) ) ) {
                return new BigDecimal( resultMap.get( "coinBalance" ) ).setScale( 2, RoundingMode.FLOOR );
            }
        }
        log.error( "meitian查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
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
