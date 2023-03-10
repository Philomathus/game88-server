package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NEW PROPERTY:
 * ReqJoinGame.apiAccount <br>
 * ASSUMPTIONS: <br>
 * * aesKey = reqJoinGame.getDes() <br>
 * * signKey = reqJoinGame.getMd5() <br>
 * * agentId = reqJoinGame.getAgent() <br>
 * * userId = reqJoinGame.getGameMemberId()
 * */
@Log4j2
@Repository( value = ConstantsGame.WALI + "GameProcessor" )
@SuppressWarnings( "unchecked" )
public class GameButtWali extends AbstractGameButt {

    private enum TransactionType {
        TRANSFER,
        WITHDRAW
    }

    /**
     * Ignore. <br>
     * 文件中几乎没有关于代币的信息。<br>
     * There is no information about tokens in the document.
     */
    @Override
    public void getToken(ReqJoinGame reqJoinGame) { }

    /**
     * Ignore. <br>
     * 3.2.1. transferV3: 如⽤户不存在，会⾃动创建。<br>
     * If the user does not exist, it will be created automatically. <br>
     * 3.3. ⽤户相关接⼝: ...也可以直接使⽤enterGame随操作⾃动注册。<br>
     * ...You can also use enterGame to automatically register with the operation.
     */
    @Override
    public void createAccount(ReqJoinGame reqJoinGame) { }

    /**
     * Optional: <br>
     * * game <br>
     * * ip <br>
     * * orderId <br>
     * * credit <br>
     */
    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        String userId  = reqJoinGame.getGameMemberId();
        String game    = reqJoinGame.getKindId();
        String ip      = reqJoinGame.getIp();
        String orderId = reqJoinGame.getOrderId();
        String credit  = String.valueOf( reqJoinGame.getTransferMoney() );

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "uid",     userId );
        paramMap.set( "game",    game );
        paramMap.set( "ip",      ip );
        paramMap.set( "orderId", orderId );
        paramMap.set( "credit",  credit );

        paramMap.values().removeIf( v -> v.isEmpty() || StringUtils.isBlank( v.get(0) ) );

        Map<String, Object> resultMap = executeGetRequest( "enterGame", reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "getJoinGameUrl:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                String gameUrl = String.valueOf( data.getOrDefault( "gameUrl", "" ) );
                reqJoinGame.setGameUrl( gameUrl );
            }
        }

        if( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, TransactionType.TRANSFER );
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, TransactionType.WITHDRAW );
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        String userId = reqJoinGame.getGameMemberId();

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "uid", userId );

        Map<String, Object> resultMap = executeGetRequest( "getBalance", reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "queryBalance:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                return new BigDecimal( String.valueOf( data.getOrDefault( "balance", 0 ) ) )
                        .setScale( 2, RoundingMode.HALF_UP );
            }
        }

        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        String orderId = reqJoinGame.getOrderId();

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "orderId", orderId );

        Map<String, Object> resultMap = executeGetRequest( "queryOrderV3", reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "queryTransfer:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                int status = Integer.parseInt( String.valueOf( data.getOrDefault( "status", "-3" ) ) );

                return status == 1;
            }
        }

        return false;
    }

    private void transact(ReqJoinGame reqJoinGame, TransactionType type) {
        String time    = String.valueOf( System.currentTimeMillis() );
        String agentId = reqJoinGame.getAgent();
        String userId  = reqJoinGame.getGameMemberId();
        String orderId = String.format( "%s_%s_%s", agentId, time, userId );
        String credit  = String.valueOf(
            switch(type) {
                case TRANSFER -> reqJoinGame.getTransferMoney();
                case WITHDRAW -> reqJoinGame.getTransferMoney().negate();
            }
        );

        reqJoinGame.setOrderId( orderId );

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "orderId",  orderId );
        paramMap.set( "uid",      userId );
        paramMap.set( "credit",   credit );

        Map<String, Object> resultMap = executeGetRequest( "transferV3", reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + type + ":{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                int status = Integer.parseInt( String.valueOf( data.getOrDefault( "status", "-3" ) ) );

                if( status == 0 || status == 1 ) {
                    return;
                }
            }
        }

        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    private Map<String, Object> executeGetRequest(String action, ReqJoinGame reqJoinGame, MultiValueMap<String, String> paramMap) {
        try {
            return restTemplate.execute(
                generateRequestUrl( reqJoinGame, action, paramMap ),
                HttpMethod.GET,
                restTemplate.httpEntityCallback( null ),
                response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                }
            );
        } catch(RestClientException ignored) {
            return null;
        }
    }

    private static String generateRequestUrl(ReqJoinGame reqJoinGame, String action, Map<String, ?> paramMap) {
        String unixTime   = String.valueOf( System.currentTimeMillis() / 1000 );
        String apiUrl     = reqJoinGame.getApiUrl();
        String apiAccount = reqJoinGame.getApiAccount();
        String aesKey     = reqJoinGame.getDes();
        String params;
        try {
            params = AESCoder.encryptByKey( assembleUrlParameters( paramMap ), aesKey );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        String sign = DigestUtils.md5Hex( params + unixTime + reqJoinGame.getMd5() );
        params = URLEncoder.encode( params, StandardCharsets.UTF_8 );

        // ${apiUrl}/${action}?a=${apiAccount}&t=${unixTime}&p=${params}&k=${sign}
        String url = String.format(
            "%s/%s?a=%s&t=%s&p=%s&k=%s",
            apiUrl,
            action,
            apiAccount,
            unixTime,
            params,
            sign
        );

        log.info( reqJoinGame.getGameCategory().getDes() + "URL: {}", url );

        return url;
    }

    private static String assembleUrlParameters(Map<String, ?> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( (k, v) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
