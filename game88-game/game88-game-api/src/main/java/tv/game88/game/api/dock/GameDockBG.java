package tv.game88.game.api.dock;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
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
@Repository( value = ConstantsGame.BG + "GameProcessor" )
public class GameDockBG extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String method     = "open.user.create";
        String id         = IdWorker.get32UUID();
        String sn         = reqJoinGame.getAgent();
        String secretCode = Base64.encodeBase64String( DigestUtils.sha1( reqJoinGame.getDes() ) );

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", reqJoinGame.getGameMemberId() );
        params.put( "agentLoginId", reqJoinGame.getLinecode() );
        params.put( "fromIp", reqJoinGame.getIp() );
        params.put( "digest", DigestUtils.md5Hex( id + sn + secretCode ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "error" ) == null ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) && BooleanUtils.toBoolean( result.getOrDefault( "success", "false" )
                                                                                     .toString() ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    private HttpEntity<Map<String, Object>> getRequestHttpEntity( String id, String method, Map<String, Object> params ) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "id", id );
        requestMap.put( "method", method );
        requestMap.put( "jsonrpc", "2.0" );
        requestMap.put( "params", params );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        return new HttpEntity<>( requestMap, httpHeaders );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String method  = "open.game.bg.url";
        String id      = IdWorker.get32UUID();
        String sn      = reqJoinGame.getAgent();
        String loginId = reqJoinGame.getGameMemberId();

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", loginId );
        params.put( "isMobileUrl", 1 );
        params.put( "fromIp", reqJoinGame.getIp() );
        params.put( "locale", "zh_CN" );
        if ( StringUtils.isNotBlank( reqJoinGame.getKindId() )
                && Arrays.asList( "105", "411", "484" )// 105:BG捕鱼大师; 411:西游捕鱼; 484:大仙捕鱼
                         .contains( reqJoinGame.getKindId() ) ) {
            params.put( "gameType", reqJoinGame.getKindId() );
        } else {
            params.put( "gameType", 443 );
            if ( StringUtils.isNotBlank( reqJoinGame.getKindId() ) ) {
                params.put( "gameId", reqJoinGame.getKindId() );
            }
        }
        params.put( "sign", DigestUtils.md5Hex( id + sn + reqJoinGame.getMd5() ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.warn( JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "result" ) != null && resultMap.get( "error" ) == null ) {
            reqJoinGame.setGameUrl( resultMap.getOrDefault( "result", "" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String     method     = "open.balance.transfer";
        String     id         = IdWorker.get32UUID();
        String     sn         = reqJoinGame.getAgent();
        BigDecimal amount     = reqJoinGame.getTransferMoney();
        String     loginId    = reqJoinGame.getGameMemberId();
        String     secretCode = Base64.encodeBase64String( DigestUtils.sha1( reqJoinGame.getDes() ) );

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", loginId );
        params.put( "amount", amount );
        params.put( "bizId", reqJoinGame.getOrderId() );
        params.put( "checkBizId", "1" );
        params.put( "digest", DigestUtils.md5Hex( id + sn + loginId + amount + secretCode ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                    + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "result" ) != null && resultMap.get( "error" ) == null ) {
            return;
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String     method     = "open.balance.transfer";
        String     id         = IdWorker.get32UUID();
        String     sn         = reqJoinGame.getAgent();
        BigDecimal amount     = reqJoinGame.getTransferMoney().negate();
        String     loginId    = reqJoinGame.getGameMemberId();
        String     secretCode = Base64.encodeBase64String( DigestUtils.sha1( reqJoinGame.getDes() ) );

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", loginId );
        params.put( "amount", amount );
        params.put( "bizId", reqJoinGame.getOrderId() );
        params.put( "checkBizId", "1" );
        params.put( "digest", DigestUtils.md5Hex( id + sn + loginId + amount + secretCode ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                    + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "result" ) != null && resultMap.get( "error" ) == null ) {
            return;
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String method     = "open.balance.get";
        String id         = IdWorker.get32UUID();
        String sn         = reqJoinGame.getAgent();
        String loginId    = reqJoinGame.getGameMemberId();
        String secretCode = Base64.encodeBase64String( DigestUtils.sha1( reqJoinGame.getDes() ) );

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", loginId );
        params.put( "digest", DigestUtils.md5Hex( id + sn + loginId + secretCode ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "result" ) != null && resultMap.get( "error" ) == null ) {
            return new BigDecimal( resultMap.getOrDefault( "result", "0" ).toString() );
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String method  = "open.balance.transfer.query";
        String id      = IdWorker.get32UUID();
        String sn      = reqJoinGame.getAgent();
        String loginId = reqJoinGame.getGameMemberId();

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", loginId );
        params.put( "bizId", reqJoinGame.getOrderId() );
        params.put( "sign", DigestUtils.md5Hex( id + sn + reqJoinGame.getMd5() ) );

        HttpEntity<Map<String, Object>> httpEntity = getRequestHttpEntity( id, method, params );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl() + "/"
                + method, HttpMethod.POST, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.get( "error" ) == null ) {
            List<Map<String, Object>> items = ( List<Map<String, Object>> ) resultMap.getOrDefault( "items", new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( items ) ) {
                Map<String, Object> resp_data = items.get( 0 );
                return reqJoinGame.getOrderId().equals( resp_data.getOrDefault( "bizId", "" ).toString() );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
