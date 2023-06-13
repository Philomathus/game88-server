package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.DesCoder;
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
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.SGWIN + "GameProcessor" )
public class GameDockSGWin extends AbstractGameDock {

    private enum ActionType {
        //
        JOIN_GAME,
        QUERY_BALANCE,
        TRANSFER,
        WITHDRAW,
        QUERY_TRANSFER;

        public String getActionCode() {
            return String.valueOf( ordinal() + 1 );
        }
    }

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        //Ignore
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        //Ignore
    }

    /**
     * 3.2.1 Login to the game <br>
     * returns the url <br>
     * and the token <br>
     * If the account does not exist, it will be created automatically.
     */
    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "ac", ActionType.JOIN_GAME.getActionCode() );
        paramMap.put( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.put( "ip", reqJoinGame.getIp() );
        paramMap.put( "gameId", reqJoinGame.getKindId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                reqJoinGame.setGameUrl( String.valueOf( data.getOrDefault( "fullUrl", "" ) ) );
            }
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    /**
     * 3.2.3 Top Score <br>
     * If the account does not exist, it will be created automatically
     */
    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, ActionType.TRANSFER );
    }

    /**
     * 3.2.4 Lower division
     */
    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        transact( reqJoinGame, ActionType.WITHDRAW );
    }

    /**
     * 3.2.2 Query user information <br>
     * returns the balance of the user
     */
    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "ac", ActionType.QUERY_BALANCE.getActionCode() );
        paramMap.put( "userCode", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                log.info( reqJoinGame.getGameCategory().getDes()
                        + "查询余额 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
                return new BigDecimal( String.valueOf( data.getOrDefault( "money", 0 ) ) ).setScale( 2, RoundingMode.DOWN );
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    /**
     * 3.2.5 Check the status of up and down orders
     */
    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "ac", ActionType.QUERY_TRANSFER.getActionCode() );
        paramMap.put( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.put( "orderId", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if ( !data.isEmpty() ) {
                int code   = Integer.parseInt( data.getOrDefault( "code", "-1" ).toString() );
                int status = Integer.parseInt( data.getOrDefault( "status", "-1" ).toString() );
                if ( status != 1 ) {
                    return code == 0 && status == 2;
                }
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }

    private void transact( ReqJoinGame reqJoinGame, ActionType actionType ) {
        switch ( actionType ) {
        case TRANSFER:
        case WITHDRAW:
            break;
        default:
            throw new IllegalArgumentException( "Action type must be transfer or withdraw!" );
        }

        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put( "ac", actionType.getActionCode() );
        paramMap.put( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.put( "money", String.valueOf( reqJoinGame.getTransferMoney() ) );
        paramMap.put( "orderId", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = null;
        try {
            resultMap = executeGetRequest( reqJoinGame, paramMap );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        String action = actionType == ActionType.TRANSFER ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                int    code    = Integer.parseInt( String.valueOf( data.getOrDefault( "code", "-1" ) ) );
                String orderId = String.valueOf( data.getOrDefault( "orderId", "" ) );
                if ( code == 0 && reqJoinGame.getOrderId().equals( orderId ) ) {
                    return;
                }
            }
        }

        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空" );
    }

    private Map<String, Object> executeGetRequest( ReqJoinGame reqJoinGame, Map<String, String> paramMap ) {
        return restTemplate.execute( generateRequestUrl( reqJoinGame, paramMap ), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private static String generateRequestUrl( ReqJoinGame reqJoinGame, Map<String, ?> paramMap ) {
        long   unixTime = System.currentTimeMillis();
        String params;
        try {
            params = DesCoder.encrypt( assembleParameters( paramMap ), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        // ${apiUrl}?agentId=${agent}timestamp=${unixTime}&param=${params}&sign=${md5Hex}
        String url = UriComponentsBuilder.fromHttpUrl( reqJoinGame.getApiUrl() ).queryParam( "agentId", reqJoinGame.getAgent() )
                                         .queryParam( "timestamp", unixTime )
                                         .queryParam( "param", URLEncoder.encode( params, StandardCharsets.UTF_8 ) )
                                         .queryParam( "sign", DigestUtils.md5Hex(
                                                 reqJoinGame.getAgent() + unixTime + reqJoinGame.getMd5() ) ).build( true )
                                         .toUriString();

        log.info( reqJoinGame.getGameCategory().getDes() + "的访问URL: {}", url );
        return url;
    }

    private static String assembleParameters( Map<String, ?> paramMap ) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
