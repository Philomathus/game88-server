package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NEW PROPERTY:
 * ReqJoinGame.apiAccount <br>
 * ASSUMPTIONS: <br>
 * * aesKey = reqJoinGame.getDes() <br>
 * * signKey = reqJoinGame.getMd5() <br>
 * * agentId = reqJoinGame.getAgent() <br>
 * * userId = reqJoinGame.getGameMemberId()
 */
@Log4j2
@Repository( value = ConstantsGame.WALI + "GameProcessor" )
public class GameDockWali extends AbstractGameDock {

    private enum TransactionType {
        //
        TRANSFER,
        WITHDRAW
    }

    /**
     * Ignore. <br>
     * 文件中几乎没有关于token的信息。<br>
     * There is no information about tokens in the document.
     */
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
    }

    /**
     * Ignore. <br>
     * 3.2.1. transferV3: 如⽤户不存在，会⾃动创建。<br>
     * If the user does not exist, it will be created automatically. <br>
     * 3.3. ⽤户相关接⼝: ...也可以直接使⽤enterGame随操作⾃动注册。<br>
     * ...You can also use enterGame to automatically register with the operation.
     */
    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "uid", reqJoinGame.getGameMemberId() );
        paramMap.put( "ip", reqJoinGame.getIp() );

        Map<String, Object> resultMap = executeGetRequest( "/register", reqJoinGame, paramMap );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( "1".equals( String.valueOf( data.getOrDefault( "status", "2" ) ) ) ) {
                log.info( reqJoinGame.getGameCategory().getDes() + " 创建玩家成功 ->{}", JsonUtil.object2Json( resultMap ) );
                return;
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    /**
     * Optional: <br>
     * * game <br>
     * * ip <br>
     * * orderId <br>
     * * credit <br>
     */
    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "uid", reqJoinGame.getGameMemberId() );
        paramMap.put( "game", reqJoinGame.getKindId() );
        paramMap.put( "ip", reqJoinGame.getIp() );

        Map<String, Object> resultMap = executeGetRequest( "/enterGame", reqJoinGame, paramMap );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            reqJoinGame.setGameUrl( String.valueOf( data.getOrDefault( "gameUrl", "" ) ) );
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, TransactionType.TRANSFER );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, TransactionType.WITHDRAW );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "uid", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = executeGetRequest( "/getBalance", reqJoinGame, paramMap );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            String              code = String.valueOf( resultMap.getOrDefault( "code", "-1" ) );
            if ( "0".equals( code ) ) {
                return new BigDecimal( String.valueOf( data.getOrDefault( "transferable", 0 ) ) ).setScale( 2,
                        RoundingMode.DOWN );
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "orderId", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = executeGetRequest( "/queryOrderV3", reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data   = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            String              status = String.valueOf( data.getOrDefault( "status", "-1" ) );
            if ( "1".equals( status ) ) {
                return true;
            } else if ( "-1".equals( status ) || "2".equals( status ) ) {
                return false;
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }

    private void transact( ReqJoinGame reqJoinGame, TransactionType type ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "orderId", reqJoinGame.getOrderId() );
        paramMap.put( "uid", reqJoinGame.getGameMemberId() );
        paramMap.put( "credit", String.valueOf( switch ( type ) {
            case TRANSFER -> reqJoinGame.getTransferMoney();
            case WITHDRAW -> reqJoinGame.getTransferMoney().negate();
        } ) );

        Map<String, Object> resultMap = null;
        try {
            resultMap = executeGetRequest( "/transferV3", reqJoinGame, paramMap );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        String action = type == TransactionType.TRANSFER ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( "1".equals( String.valueOf( data.getOrDefault( "status", "2" ) ) ) ) {
                return;
            }
        }
        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空" );
    }

    private Map<String, Object> executeGetRequest( String action, ReqJoinGame reqJoinGame, Map<String, String> paramMap ) {
        UriComponents uriComponents = generateRequestUrl( reqJoinGame, action, paramMap );
        // log.info( reqJoinGame.getGameCategory().getDes() + "的访问URL: {}", uriComponents.toUriString() );
        return restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private static UriComponents generateRequestUrl( ReqJoinGame reqJoinGame, String action, Map<String, ?> paramMap ) {
        String unixTimeSeconds = String.valueOf( System.currentTimeMillis() / 1000 );
        String params;
        try {
            params = AESCoder.encryptByKey( assembleParameters( paramMap ), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        return UriComponentsBuilder.fromHttpUrl( reqJoinGame.getApiUrl() ).path( action )
                                   .queryParam( "a", reqJoinGame.getLinecode() ).queryParam( "t", unixTimeSeconds )
                                   .queryParam( "p", UriUtils.encode( params, StandardCharsets.UTF_8 ) )
                                   .queryParam( "k", DigestUtils.md5Hex( params + unixTimeSeconds + reqJoinGame.getMd5() ) )
                                   .build( true );
    }

    private static String assembleParameters( Map<String, ?> paramMap ) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
