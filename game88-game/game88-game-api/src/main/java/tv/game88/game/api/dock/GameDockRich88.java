package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.RICH88 + "GameProcessor" )
@SuppressWarnings( "unchecked" )
public class GameDockRich88 extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        //ignore
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        //ignore
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String              url    = reqJoinGame.getApiUrl() + "/v2/platform/login";
        Map<String, Object> params = new HashMap<>();
        params.put( "account", reqJoinGame.getGameMemberId() );
        params.put( "game_code", reqJoinGame.getKindId() );
        params.put( "lang", "zh-CN" );

        Map<String, Object> resultMap = execute( HttpMethod.POST, url, params, reqJoinGame );

        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            reqJoinGame.setGameUrl( dataMap.getOrDefault( "url", "" ).toString() );
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
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() == 2 ) { // 提现时必须登出玩家,否则无法下分
            String url = String.format( "%s/v2/platform/logout/%s", reqJoinGame.getApiUrl(), reqJoinGame.getGameMemberId() );

            Map<String, Object> resultMap = execute( HttpMethod.POST, url, null, reqJoinGame );

            log.info( reqJoinGame.getGameCategory().getDes()
                    + "强制登出玩家 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        }
        String url = String.format( "%s/v2/platform/balance/%s", reqJoinGame.getApiUrl(), reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( HttpMethod.GET, url, null, reqJoinGame );

        if ( isValid( resultMap ) ) {
            log.info( reqJoinGame.getGameCategory().getDes()
                    + "查询余额 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            return new BigDecimal( dataMap.getOrDefault( "free_balance", "0" ).toString() );
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/v2/platform/transfer/%s", reqJoinGame.getApiUrl(), reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = execute( HttpMethod.GET, url, null, reqJoinGame );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap       = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            String              transferState = dataMap.getOrDefault( "transfer_state", "" ).toString();
            if ( "1".equals( transferState ) || "2".equals( transferState ) ) {
                return "1".equals( transferState );
            }
        }
        if ( !CollectionUtils.isEmpty( resultMap ) && "15001".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            return false;
        }
        throw new BusinessException( "查询结果为空,需要重试" );
    }

    private Map<String, Object> execute( HttpMethod method, String url, Map<String, Object> params, ReqJoinGame reqJoinGame ) {
        String timestamp = String.valueOf( System.currentTimeMillis() / 1000 );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.setAccept( List.of( MediaType.APPLICATION_JSON ) );
        httpHeaders.set( "api_key", DigestUtils.sha256Hex( reqJoinGame.getAgent() + reqJoinGame.getMd5() + timestamp ) );
        httpHeaders.set( "pf_id", reqJoinGame.getAgent() );
        httpHeaders.set( "timestamp", timestamp );

        if ( params != null ) {
            log.warn( JsonUtil.object2Json( params ) );
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(
                params == null ? new HashMap<>() : params, httpHeaders );

        return restTemplate.execute( url, method, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private void transact( ReqJoinGame reqJoinGame, boolean isDeposit ) {
        String url = reqJoinGame.getApiUrl() + "/v2/platform/transfer";

        Map<String, Object> params = new HashMap<>();
        params.put( "account", reqJoinGame.getGameMemberId() );
        params.put( "transfer_no", reqJoinGame.getOrderId() );
        params.put( "transfer_type", isDeposit ? "0" : "1" );
        params.put( "amount", reqJoinGame.getTransferMoney() );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( HttpMethod.POST, url, params, reqJoinGame );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        String action = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !isValid( resultMap ) ) {
            throw new GameTransferException(
                    reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空" );
        }
    }

    private boolean isValid( Map<String, Object> resultMap ) {
        return !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.getOrDefault( "code", "" ).toString() );
    }
}
