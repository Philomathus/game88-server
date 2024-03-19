package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.JDB + "GameProcessor" )
public class GameDockJDB extends AbstractGameDock {

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        //ignore
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 12 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "uid", reqJoinGame.getGameMemberId() );
        params.put( "name", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.get( "status" ).toString();
            // 7602是已存在账号错误
            if ( "0000".equals( status ) || "7602".equals( status ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 11 );
        params.put( "lang", "cn" );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "uid", reqJoinGame.getGameMemberId() );
        if ( StringUtils.isNotBlank( reqJoinGame.getKindId() ) ) {
            String[] split = reqJoinGame.getKindId().split( "-" );
            params.put( "gType", split[ 0 ] );
            params.put( "mType", split[ 1 ] );
            params.put( "windowMode", 2 );
        } else {
            params.put( "windowMode", 1 );
        }
        params.put( "isAPP", true );

        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );

        if ( !CollectionUtils.isEmpty( resultMap ) && "0000".equals( resultMap.get( "status" ).toString() ) ) {
            reqJoinGame.setGameUrl( resultMap.getOrDefault( "path", "" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, true );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, false );
    }

    @Retryable( retryFor = Exception.class, noRetryFor = GameTransferException.class, backoff = @Backoff( delay = 2000 ),
            maxAttempts = 3 )
    protected void kickMember( ReqJoinGame reqJoinGame ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 17 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "uid", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "强制登出玩家 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = String.valueOf( resultMap.get( "status" ) );
            if ( "0000".equals( status ) || "7405".equals( status ) ) {
                return;
            }
            throw new RuntimeException( String.valueOf( resultMap.get( "Message" ) ) );
        }
        throw new RuntimeException( reqJoinGame.getGameCategory().getDes() + "强制登出玩家失败" );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() != null && reqJoinGame.getMoneyType() == 2 ) { // 提现时必须登出玩家,否则无法下分
            SpringUtils.getBean( GameDockJDB.class ).kickMember( reqJoinGame );
            this.sleep( 5 );
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 15 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "uid", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( resultMap.get( "status" ).toString().equals( "0000" ) ) {
                List<Map<String, Object>> dataMapList = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data",
                        new ArrayList<>() );
                if ( !CollectionUtils.isEmpty( dataMapList ) ) {
                    Map<String, Object> dataMap = dataMapList.get( 0 );
                    return new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
                }
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        Map<String, Object> params = new HashMap<>();
        params.put( "action", 55 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "serialNo", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            return "0000".equals( resultMap.getOrDefault( "status", "" ).toString() );
        }
        throw new BusinessException( "查询结果为空,需要重试" );
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, Object> execute( String url, Map<String, Object> params, ReqJoinGame reqJoinGame ) {
        String json         = JsonUtil.object2Json( params );
        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        Map<String, String> dataMap = Map.of( "dc", reqJoinGame.getLinecode(), "x", encodedParam );
        log.warn( JsonUtil.object2Json( dataMap ) );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( dataMap, httpHeaders );

        log.warn( "URL: {} ::: params:{}", url, json );
        return restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private String evaluateUrl( String apiUrl ) {
        return String.format( "%s/apiRequest.do", apiUrl );
    }

    private void transact( ReqJoinGame reqJoinGame, boolean isDeposit ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 19 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "uid", reqJoinGame.getGameMemberId() );
        params.put( "serialNo", reqJoinGame.getOrderId() );

        params.put( "amount", isDeposit ? reqJoinGame.getTransferMoney() : reqJoinGame.getTransferMoney().negate() );
        params.put( "remark", isDeposit ? "deposit" : "withdraw" );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), params, reqJoinGame );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "game Deposit failed" );
        }
        String des = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + des
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String status = resultMap.getOrDefault( "status", "" ).toString();
            if ( "0000".equals( status ) && reqJoinGame
                    .getOrderId()
                    .equals( resultMap.getOrDefault( "serialNo", "" ).toString() ) ) {
                return;
            }
            if ( "6901".equals( status ) ) {
                throw new RuntimeException( "会员正在游戏中" );
            }
        }
        throw new GameTransferException( String.format( "%s%s分异常 - %s分失败或数据为空", reqJoinGame
                .getGameCategory()
                .getDes(), des, des ) );
    }
}
