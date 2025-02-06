package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.BAISHENG + "GameProcessor" )
public class GameDockBaiSheng extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String params = String.format( "action=1&account=%s&money=0&nickname=%s&sub_channel_id=%s&server_type=%s&lang=cn"
                + "&switch_lang=1", reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(), reqJoinGame.getLinecode(),
                reqJoinGame.getKindId() );

        Map<String, Object> resultMap = execute( reqJoinGame, params );

        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            String              url    = result.getOrDefault( "url", "" ).toString();
            reqJoinGame.setGameUrl( url );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String params = String.format( "action=2&account=%s&money=%s&money_type=RMB&order_id=%s", reqJoinGame.getGameMemberId()
                , reqJoinGame.getTransferMoney(), reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( reqJoinGame, params );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", "-1" ).toString() );
            if ( code == 0 ) {
                return;
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String params = String.format( "action=20&money_type=RMB&account=%s&order_id=%s&money=%s",
                reqJoinGame.getGameMemberId(), reqJoinGame.getOrderId(), reqJoinGame.getTransferMoney() );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( reqJoinGame, params );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", "-1" ).toString() );
            if ( code == 0 ) {
                return;
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    private Map<String, Object> execute( ReqJoinGame reqJoinGame, String params ) {
        String time  = System.currentTimeMillis() + "";
        String param = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "channel_id", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( reqJoinGame.getApiUrl() ).queryParams( requestMap )
                .build( true );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "请求URL:{}; userId:{}", uriComponents.toUriString(), reqJoinGame.getGameMemberId() );

        return restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getMoneyType() != null && reqJoinGame.getMoneyType() == 2 ) { // 提现时必须登出玩家,否则无法下分
            SpringUtils.getBean( GameDockBaiSheng.class ).kickMember( reqJoinGame );
        }

        String params = String.format( "action=6&account=%s&money_type=RMB", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( reqJoinGame, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                int        code  = Integer.parseInt( resultMap.getOrDefault( "code", "-1" ).toString() );
                BigDecimal money = new BigDecimal( result.getOrDefault( "totalMoney", "0" ).toString() );
                if ( code == 0 ) {
                    return money;
                }
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Retryable( retryFor = Exception.class, noRetryFor = GameTransferException.class, backoff = @Backoff( delay = 2000 ),
            maxAttempts = 3 )
    protected void kickMember( ReqJoinGame reqJoinGame ) {
        String params = String.format( "action=8&account=%s&forbidden_user=0", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = execute( reqJoinGame, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "强制登出玩家 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( Integer.parseInt( resultMap.getOrDefault( "code", "-1" ).toString() ) == 0 ) {
                return;
            }
            throw new RuntimeException( JsonUtil.object2Json( resultMap ) );
        }
        throw new RuntimeException( reqJoinGame.getGameCategory().getDes() + "强制登出玩家失败" );
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String params = String.format( "action=5&order_id=%s", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = execute( reqJoinGame, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                int code   = Integer.parseInt( resultMap.getOrDefault( "code", "-1" ).toString() );
                int status = Integer.parseInt( result.getOrDefault( "status", "-1" ).toString() );
                return code == 0 && status == 2;
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
