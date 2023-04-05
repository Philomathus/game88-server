package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.JDB + "GameProcessor" )
public class GameButtJDB extends AbstractGameButt {

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
        String json = JsonUtil.object2Json( params );
        log.info( "Create Account: {}", json );
        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam,
                reqJoinGame.getLinecode() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0000".equals( resultMap.get( "status" ).toString() ) ) {
            redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
            return;
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
        String json = JsonUtil.object2Json( params );
        log.info( "Get Join Game Url: {}", json );

        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam,
                reqJoinGame.getLinecode() );

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

    @Override
    @SuppressWarnings( "unchecked" )
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() != null && reqJoinGame.getMoneyType() == 2 ) { // 提现时必须登出玩家,否则无法下分
            Map<String, Object> params = new LinkedHashMap<>();
            params.put( "action", 17 );
            params.put( "ts", System.currentTimeMillis() );
            params.put( "parent", reqJoinGame.getAgent() );
            params.put( "uid", reqJoinGame.getGameMemberId() );
            String json = JsonUtil.object2Json( params );
            log.info( "Force Logout: {}", json );
            String encodedParam = null;
            try {
                encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
                throw new BusinessException( e.getMessage() );
            }
            Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam,
                    reqJoinGame.getLinecode() );

            log.info( reqJoinGame.getGameCategory().getDes()
                    + "强制登出玩家 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action", 15 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "uid", reqJoinGame.getGameMemberId() );
        String json = JsonUtil.object2Json( params );
        log.info( "Query Balance: {}", json );
        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam,
                reqJoinGame.getLinecode() );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0000".equals( resultMap.get( "status" ).toString() ) ) {
            List<Map<String, Object>> dataMapList = ( List<Map<String, Object>> ) resultMap.getOrDefault( "data",
                    new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( dataMapList ) ) {
                Map<String, Object> dataMap = dataMapList.get( 0 );
                return new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        Map<String, Object> params = new HashMap<>();
        params.put( "action", 55 );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "parent", reqJoinGame.getAgent() );
        params.put( "serialNo", reqJoinGame.getOrderId() );
        String json = JsonUtil.object2Json( params );
        log.info( "Query Transfer: {}", json );

        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        Map<String, Object> resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam,
                reqJoinGame.getLinecode() );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            return "0000".equals( resultMap.getOrDefault( "status", "" ).toString() );
        }
        throw new BusinessException( "查询结果为空,需要重试" );
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, Object> execute( String url, String param, String dc ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        Map<String, String> dataMap = Map.of( "dc", dc, "x", param );
        log.warn( JsonUtil.object2Json( dataMap ) );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( dataMap, httpHeaders );

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
        String json = JsonUtil.object2Json( params );
        log.info( "Is Deposit: {}, Transact: {}", isDeposit, json );

        String encodedParam = null;
        try {
            encodedParam = AESCoder.encryptByKeyIvNoPadding( json, reqJoinGame.getMd5(), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( evaluateUrl( reqJoinGame.getApiUrl() ), encodedParam, reqJoinGame.getLinecode() );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }
        String des = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + des
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0000".equals( resultMap.getOrDefault( "status", "" ).toString() )
                && reqJoinGame.getOrderId().equals( resultMap.getOrDefault( "serialNo", "" ).toString() ) ) {
            return;
        }
        throw new GameTransferException( String.format( "%s%s分异常 - %s分失败或数据为空", reqJoinGame.getGameCategory()
                                                                                                      .getDes(), des, des ) );
    }
}
