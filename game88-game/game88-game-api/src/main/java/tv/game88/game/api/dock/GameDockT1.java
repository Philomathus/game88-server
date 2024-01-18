package tv.game88.game.api.dock;

import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.T1 + "GameProcessor" )
public class GameDockT1 extends AbstractGameDock {

    private static final String ENDPOINT_GENERATE_TOKEN = "generate_token";
    private static final String ENDPOINT_CREATE_PLAYER = "create_player";
    private static final String ENDPOINT_LAUNCH_GAME = "chain/query_game_launcher";
    private static final String ENDPOINT_TRANSFER_PLAYER_FUND = "transfer_player_fund";
    private static final String ENDPOINT_QUERY_PLAYER_BALANCE = "query_player_balance";
    private static final String ENDPOINT_QUERY_TRANSACTION = "query_transaction";

    private static final String ACTION_DEPOSIT = "deposit";
    private static final String ACTION_WITHDRAW = "withdraw";

    /**
     * 生成密钥
     *
     * @param reqJoinGame 游戏参数
     *
     */
    private String generateSecureKey(final ReqJoinGame reqJoinGame){
        final String convertTime = LocalDateTimeUtils.format( LocalDate.now( ZoneId.of( "America/Caracas" ) ),
                LocalDateTimeUtils.YYYYMMDD_FORMATTER );
        return DigestUtils.md5Hex( reqJoinGame.getDes() + convertTime );
    }

    private String getURL(final String apiURL, final String endpoint){
        return apiURL + "/gameapi/v2/" + endpoint;
    }

    private boolean isSuccessCode(final Map<String, Object> resultMap){
        final Object code = resultMap.get( "code" );
        return "0".equals(String.valueOf(code));
    }

    private Map<String, Object> execute( final String url, final HttpMethod method, final Map<String, Object> params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        return restTemplate.execute( url, method, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    private void transact( final ReqJoinGame reqJoinGame, final String actionType ) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "action_type", actionType);
        params.put( "amount", reqJoinGame.getTransferMoney() );
        params.put( "auth_token", reqJoinGame.getToken() );
        params.put( "external_trans_id", reqJoinGame.getOrderId() );
        params.put( "merchant_code", reqJoinGame.getMd5() );
        params.put( "sign", generateSecureKey(reqJoinGame));
        params.put( "username", reqJoinGame.getGameMemberId());

        final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_TRANSFER_PLAYER_FUND);
        final Map<String, Object> resultMap = execute( url, HttpMethod.POST, params );

        final String action = ACTION_DEPOSIT.equals(actionType) ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap) ) {
            final Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                //true if operation succeed；操作成功时为 true
                if(Boolean.parseBoolean(data.getOrDefault( "updated", "false" ).toString())) {
                    return;
                }
            }
        }

        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空" );

    }

    @Override
    public void getToken(final ReqJoinGame reqJoinGame ) {
        if ( StringUtils.isBlank( reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        final String key = Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() + ":" + reqJoinGame.getGameMemberId();
        if ( !redisUtils.exists( key ) ) {
            final Map<String, Object> params = new LinkedHashMap<>();
            params.put( "merchant_code", reqJoinGame.getMd5() );
            params.put( "secure_key", reqJoinGame.getDes() );
            params.put( "sign", generateSecureKey(reqJoinGame) );

            final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_GENERATE_TOKEN);
            final Map<String, Object> resultMap = execute( url, HttpMethod.POST, params );

            if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap) ) {
                Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
                if ( !data.isEmpty() ) {
                    final String token = data.getOrDefault( "auth_token", "" ).toString();
                    if (StringUtils.isBlank(token)) {
                        log.error( reqJoinGame.getGameCategory().getDes() + " 获取token失败 ->{}", JsonUtil.object2Json( resultMap ) );
                        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取token失败" );
                    }
                    reqJoinGame.setToken( token );
                    redisUtils.strSet( key, token, Duration.ofMinutes( 9 ) );
                }
            }

        } else {
            reqJoinGame.setToken( redisUtils.strGet( key ) );
        }
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        final String key = Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId();
        if ( redisUtils.sIsMember( key, reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "auth_token", reqJoinGame.getToken() );
        params.put( "merchant_code", reqJoinGame.getMd5() );
        params.put( "sign", generateSecureKey(reqJoinGame) );
        params.put( "username", reqJoinGame.getGameMemberId());

        final String json = JsonUtil.object2Json( params );
        log.info( "Create Account: {}", json );

        final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_CREATE_PLAYER);
        final Map<String, Object> resultMap = execute( url, HttpMethod.POST, params );

        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap)) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "detail", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                redisUtils.sAdd( key, reqJoinGame.getGameMemberId() );
                return;
            }
        }

        log.error(
                reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}; url:{}", JsonUtil.object2Json( resultMap ), url );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "auth_token", reqJoinGame.getToken() );
        params.put( "game_code", reqJoinGame.getKindId() );
        params.put( "language", "pt-BR" );
        params.put( "merchant_code", reqJoinGame.getMd5() );
        params.put( "sign", generateSecureKey(reqJoinGame));
        params.put( "username", reqJoinGame.getGameMemberId());

        final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_LAUNCH_GAME);
        final Map<String, Object> resultMap = execute( url, HttpMethod.GET, params );

        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap)) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                reqJoinGame.setGameUrl( String.valueOf( data.getOrDefault( "game_url", "" ) ) );
            }
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame, ACTION_DEPOSIT);
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame, ACTION_WITHDRAW);
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "auth_token", reqJoinGame.getToken() );
        params.put( "merchant_code", reqJoinGame.getMd5() );
        params.put( "sign", generateSecureKey(reqJoinGame) );
        params.put( "username", reqJoinGame.getGameMemberId());

        final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_QUERY_PLAYER_BALANCE);
        final Map<String, Object> resultMap = execute( url, HttpMethod.GET, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap)) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                return new BigDecimal( ( String.valueOf( data.getOrDefault( "game_platform_balance", "" ) ) )).setScale( 2, RoundingMode.DOWN );
            }
        }

        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "auth_token", reqJoinGame.getToken() );
        params.put( "merchant_code", reqJoinGame.getMd5() );
        params.put( "sign", generateSecureKey(reqJoinGame) );
        params.put( "external_trans_id", reqJoinGame.getOrderId());

        final String url = this.getURL(reqJoinGame.getApiUrl(), ENDPOINT_QUERY_TRANSACTION);
        final Map<String, Object> resultMap = execute( url, HttpMethod.GET, params );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if ( !CollectionUtils.isEmpty( resultMap ) && isSuccessCode(resultMap)) {
            Map<String, Object> data = ( Map<String, Object> ) resultMap.getOrDefault( "detail", Collections.emptyMap() );
            if ( !data.isEmpty() ) {
                //statuses: 1: processing 转账进行中 2: ok 转账成功 3: failed 转账失败
                return "2".equals(data.getOrDefault( "status", "0" ).toString() );
            }
        }

        throw new RuntimeException( "查询结果为空,需要重试" );
    }

}
