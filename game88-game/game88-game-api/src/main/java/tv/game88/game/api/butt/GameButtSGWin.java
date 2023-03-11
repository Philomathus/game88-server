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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.util.*;

@Log4j2
@Repository(value = ConstantsGame.SGWIN + "GameProcessor")
@SuppressWarnings( "unchecked" )
public class GameButtSGWin extends AbstractGameButt {

    private enum ActionType {
        JOIN_GAME, QUERY_BALANCE, TRANSFER, WITHDRAW, QUERY_TRANSFER;

        public String getActionCode() {
            return String.valueOf( ordinal() + 1 );
        }
    }

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        //Ignore
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        //Ignore
    }

    /**
     * 3.2.1 Login to the game <br>
     * returns the url <br>
     * and the token <br>
     * If the account does not exist, it will be created automatically.
     */
    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "ac",       ActionType.JOIN_GAME.getActionCode() );
        paramMap.set( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.set( "ip",       reqJoinGame.getIp() );
        paramMap.set( "gameId",   reqJoinGame.getKindId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "getJoinGameUrl:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                reqJoinGame.setGameUrl( String.valueOf( data.getOrDefault( "fullUrl", "" ) ) );
            }
        }

        if( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    /**
     * 3.2.3 Top Score <br>
     * If the account does not exist, it will be created automatically
     */
    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, ActionType.TRANSFER );
    }

    /**
     * 3.2.4 Lower division
     */
    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, ActionType.WITHDRAW );
    }

    /**
     * 3.2.2 Query user information <br>
     * returns the balance of the user
     */
    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "ac",       ActionType.QUERY_BALANCE.getActionCode() );
        paramMap.set( "userCode", reqJoinGame.getGameMemberId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "queryBalance:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                return new BigDecimal( String.valueOf( data.getOrDefault( "money", 0 ) ) )
                        .setScale( 2, RoundingMode.HALF_UP );
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * 3.2.5 Check the status of up and down orders
     */
    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "ac",       ActionType.QUERY_TRANSFER.getActionCode() );
        paramMap.set( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.set( "orderId",  reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "queryTransfer:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                int status = Integer.parseInt( String.valueOf( data.getOrDefault( "status", "-1" ) ) );

                return status == 2;
            }
        }

        return false;
    }

    private void transact(ReqJoinGame reqJoinGame, ActionType actionType) {
        switch(actionType) {
            case TRANSFER:
            case WITHDRAW:
                break;
            default:
                throw new IllegalArgumentException( "Action type must be transfer or withdraw!" );
        }

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "ac",       actionType.getActionCode() );
        paramMap.set( "userCode", reqJoinGame.getGameMemberId() );
        paramMap.set( "money",    String.valueOf( reqJoinGame.getTransferMoney() ) );
        paramMap.set( "orderId",  reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = executeGetRequest( reqJoinGame, paramMap );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "transferMoney:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );

            if( !data.isEmpty() ) {
                int code = Integer.parseInt( String.valueOf( data.getOrDefault( "code", "-1" ) ) );

                if( code == 0 ) {
                    return;
                }
            }
        }

        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    private Map<String, Object> executeGetRequest(ReqJoinGame reqJoinGame, MultiValueMap<String, String> paramMap) {
        try {
            return restTemplate.execute(
                    generateRequestUrl( reqJoinGame, paramMap ),
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
        } catch( RestClientException ignored ) {
            return null;
        }
    }

    private static String generateRequestUrl(ReqJoinGame reqJoinGame, Map<String, ?> paramMap) {
        long unixTime = System.currentTimeMillis();
        String params;
        try {
            params = AESCoder.encryptByKeyUrl( assembleParameters( paramMap ), reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        // ${apiUrl}?agentId=${agent}timestamp=${unixTime}&param=${params}&sign=${md5Hex}
        String url = UriComponentsBuilder
                .fromHttpUrl( reqJoinGame.getApiUrl() )
                .queryParam( "agentId", reqJoinGame.getAgent() )
                .queryParam( "timestamp", unixTime )
                .queryParam( "param", params )
                .queryParam( "sign", DigestUtils.md5Hex( reqJoinGame.getAgent() + unixTime + reqJoinGame.getMd5() ) )
                .build( true )
                .toUriString();

        log.info( reqJoinGame.getGameCategory().getDes() + "URL: {}", url );

        return url;
    }

    private static String assembleParameters(Map<String, ?> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( (k, v) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
