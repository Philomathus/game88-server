package tv.game88.game.api.butt;

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
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.RICH88 + "GameProcessor" )
@SuppressWarnings( "unchecked" )
public class GameButtRich88 extends AbstractGameButt {

    private static final String API_KEY   = "api_key";
    private static final String PF_ID     = "pf_id";
    private static final String TIMESTAMP = "timestamp";

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
        Map<String, String> params = new LinkedHashMap<>();
        params.put( "account", reqJoinGame.getGameMemberId() );
        addRequiredHeaders( reqJoinGame, params );
        log.info( "Join Game: {}", JsonUtil.object2Json( params ) );
        Map<String, Object> resultMap = execute( HttpMethod.POST, url, params );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "Join Game result:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            String              gameUrl = dataMap.getOrDefault( "url", "" ).toString();
            if ( StringUtils.isEmpty( gameUrl ) ) {
                throw new BusinessException( "Game url is empty" );
            } else {
                reqJoinGame.setGameUrl( gameUrl );
            }
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
        String url = String.format( "%s/v2/platform/balance/%s", reqJoinGame.getApiUrl(), reqJoinGame.getGameMemberId() );
        Map<String, String> params = new LinkedHashMap<>();
        addRequiredHeaders( reqJoinGame, params );
        log.info( "Query Balance: {}, params: {}", url, JsonUtil.object2Json( params ) );
        Map<String, Object> resultMap = execute( HttpMethod.GET, url, params );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "Query Balance result:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            return new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = String.format( "%s/v2/platform/transfer/%s", reqJoinGame.getApiUrl(), reqJoinGame.getOrderId() );
        Map<String, String> params = new LinkedHashMap<>();
        addRequiredHeaders( reqJoinGame, params );
        log.info( "Query Transfer: {}, params: {}", url, JsonUtil.object2Json( params ) );
        Map<String, Object> resultMap = execute( HttpMethod.GET, url, params );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "Query Transfer result:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            return "1".equals( dataMap.getOrDefault( "transfer_state", "" ).toString() );
        }
        throw new BusinessException( "查询结果为空,需要重试" );
    }

    private void addRequiredHeaders( ReqJoinGame reqJoinGame, Map<String, String> params ) {
        String timestamp = String.valueOf( Math.round( System.currentTimeMillis() / 1000.0 ) );
        String apiKey    = String.format( "%s%s%s", reqJoinGame.getAgent(), reqJoinGame.getMd5(), timestamp );
        params.put( API_KEY, DigestUtils.sha256Hex( apiKey ) );
        params.put( PF_ID, reqJoinGame.getAgent() );
        params.put( TIMESTAMP, timestamp );
    }

    private Map<String, Object> execute( HttpMethod method, String url, Map<String, String> params ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.setAccept( List.of( MediaType.APPLICATION_JSON ) );
        httpHeaders.set( API_KEY, params.get( API_KEY ) );
        httpHeaders.set( PF_ID, params.get( PF_ID ) );
        httpHeaders.set( TIMESTAMP, params.get( TIMESTAMP ) );

        HttpEntity<String> requestEntity = new HttpEntity<>(
                HttpMethod.GET == method ? null : JsonUtil.object2Json( params ), httpHeaders );

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
        String              url    = reqJoinGame.getApiUrl() + "/v2/platform/transfer";
        Map<String, String> params = new LinkedHashMap<>();
        params.put( "account", reqJoinGame.getGameMemberId() );
        params.put( "transfer_no", reqJoinGame.getOrderId() );
        params.put( "transfer_type", isDeposit ? "0" : "1" );
        params.put( "amount", reqJoinGame.getTransferMoney().toString() );
        addRequiredHeaders( reqJoinGame, params );
        log.info( "isDeposit: {}, Transfer Money: {}", isDeposit, JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( HttpMethod.POST, url, params );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "isDeposit: {}, Transfer Money result:{}; userId:{}", isDeposit, JsonUtil.object2Json( resultMap ),
                reqJoinGame.getGameMemberId() );
        if ( !isValid( resultMap ) ) {
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
        }
    }

    private boolean isValid( Map<String, Object> resultMap ) {
        boolean result = false;
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( !CollectionUtils.isEmpty( dataMap ) && "0".equals( resultMap.getOrDefault( "code", "" ) )
                    && "Success".equals( resultMap.getOrDefault( "msg", "" ) ) ) {
                result = true;
            }
        }
        return result;
    }
}
