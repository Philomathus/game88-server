package tv.game88.game.api.dock;

import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
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
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.PP + "GameProcessor" )
public class GameDockPP extends AbstractGameDock {

    private static final String ENDPOINT_CREATE_PLAYER = "/player/account/create/";
    private static final String ENDPOINT_START_GAME = "/game/start/";
    private static final String ENDPOINT_TRANSFER = "/balance/transfer/";
    private static final String ENDPOINT_GET_BALANCE = "/balance/current/";
    private static final String ENDPOINT_GET_TRANSFER_STATUS = "/balance/transfer/status/";

    private static final String CURRENCY_ISO4217_YUAN = "BRL";

    /**
     * Send HTTP request to the specified URL.
     *
     * @param url specifies the request destination.
     *
     * @return Map of response from the response of the URL.
     */
    private Map<String, Object> execute(String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        httpHeaders.setCacheControl("no-cache");
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(  httpHeaders );

        return restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }

    /**
     *<pre>  Hash code is calculated with following formula:
     * From request post parameters, all parameters are taken (except hash) and appending to string:
     *    1.Sort all parameter by keys in alphabetical order.
     *    2.Append them (if the value is not null or empty) in key1=value1&key2=value2.
     *    3.Append secret key, e.g.: key1=value1&key2=value2SECRET
     *    4.Calculate the hash by using MD5.
     *    5.Compare with hash parameter. In the case of failure Casino Operator should send the error code 5.</pre>
     *
     * @param params contains the parameters in key value pair.
     * @param secret secret value for the hash.
     *
     * @return md5 hash of the parameters.
     */
    private String getHash(final Map<String, Object> params, final String secret){
        return DigestUtils.md5Hex(keyValStringFormat(params, "",secret));
    }

    /**
     * Format the specified parameters with prefix and postfix.
     *  E.g.: prefix_key1=value1&key2=value2_postfix
     *
     * @param params parameters.
     * @param prefix prefix of the formatted String.
     * @param postfix postfix of the formatted String.
     * @return formatted key value String.
     */
    private String keyValStringFormat(final Map<String, Object> params, final String prefix, final String postfix){
        return params.keySet().stream()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.joining("&", prefix, postfix));
    }

    /**
     * Check whether the result map has error.
     *
     * @param resultMap the map where to get the error code.
     * @return true if the error code is equal to 0, otherwise false.
     */
    private boolean isSuccess(final Map<String, Object> resultMap){
        final Object code = resultMap.get( "error" );
        return "0".equals(String.valueOf(code));
    }

    /**
     * Get the Actual URL of the Endpoint.
     *
     * @param apiURL API host
     * @param endpoint actual path of the endpoint
     * @param params parameters to be appended in the URL.
     *
     * @return formatted URL.
     */
    private String getURL(final String apiURL, final String endpoint, final Map<String, Object> params){
        final StringBuilder sb = new StringBuilder()
                .append(apiURL)
                .append("/IntegrationService/v3/http/CasinoGameAPI")
                .append(endpoint)
                .append(keyValStringFormat(params, "?", ""));
        return sb.toString();
    }

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {

    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        final String key = Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId();
        if ( redisUtils.sIsMember( key, reqJoinGame.getGameMemberId() ) ) {
            return;
        }

        final Map<String, Object> params = new TreeMap<>();
        params.put( "secureLogin", reqJoinGame.getMemberId() );
        params.put( "externalPlayerId", reqJoinGame.getGameMemberId() );
        params.put( "currency", CURRENCY_ISO4217_YUAN);
        params.put( "hash", getHash(params, reqJoinGame.getMd5()) ); //should be added as last param

        final String json = JsonUtil.object2Json( params );
        log.info( "Create Account: {}", json );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_CREATE_PLAYER, params);
        final Map<String, Object> resultMap = execute(url);

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            redisUtils.sAdd(key, reqJoinGame.getGameMemberId());
            return;
        }

        log.error(reqJoinGame.getGameCategory().getDes()
                + " 创建玩家失败 ->{}; url:{}", JsonUtil.object2Json(resultMap), url);

        throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败");
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new TreeMap<>();
        params.put( "secureLogin", reqJoinGame.getMemberId() );
        params.put( "externalPlayerId", reqJoinGame.getGameMemberId() );
        params.put( "gameId", reqJoinGame.getKindId());
        params.put( "language", "pt" );
        params.put( "hash", getHash(params, reqJoinGame.getMd5()) ); //should be added as last param

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_START_GAME, params);
        final Map<String, Object> resultMap = execute(url);

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            reqJoinGame.setGameUrl(resultMap.getOrDefault("gameURL", "").toString());
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error(reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());

            throw new BusinessException("获取游戏链接失败");
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame, true);
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame, false);
    }

    /**
     * Transfer Fund whether deposit or withdrawal.
     *
     * @param reqJoinGame contains parameter information.
     * @param isDeposit indicates whether the transfer or transaction is deposit or withdrawal.
     *                  If isDeposit is true, transfer positive amount, otherwise amount is negative.
     */
    private void transact( final ReqJoinGame reqJoinGame, final boolean isDeposit ) {
        final Map<String, Object> params = new TreeMap<>();
        params.put( "secureLogin", reqJoinGame.getMemberId() );
        params.put( "externalPlayerId", reqJoinGame.getGameMemberId() );
        params.put( "externalTransactionId", reqJoinGame.getOrderId());
        BigDecimal amount = reqJoinGame.getTransferMoney();
        amount = isDeposit ? amount : amount.negate();
        params.put( "amount", amount);
        params.put( "hash", getHash(params, reqJoinGame.getMd5()) ); //should be added as last param

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_TRANSFER, params);
        final Map<String, Object> resultMap = execute(url);

        final String action = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)
                && !resultMap.getOrDefault("transactionId", "").toString().isEmpty()) {
            return;
        }

        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空");

    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new TreeMap<>();
        params.put( "secureLogin", reqJoinGame.getMemberId() );
        params.put( "externalPlayerId", reqJoinGame.getGameMemberId() );
        params.put( "hash", getHash(params, reqJoinGame.getMd5()) ); //should be added as last param

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_GET_BALANCE, params);
        final Map<String, Object> resultMap = execute(url);

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            return new BigDecimal((String.valueOf(resultMap.getOrDefault("balance", "")))).setScale(2, RoundingMode.DOWN);
        }

        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new TreeMap<>();
        params.put( "secureLogin", reqJoinGame.getMemberId() );
        params.put( "externalPlayerId", reqJoinGame.getGameMemberId() );
        params.put( "hash", getHash(params, reqJoinGame.getMd5()) ); //should be added as last param

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_GET_TRANSFER_STATUS, params);
        final Map<String, Object> resultMap = execute(url);

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            //'Success' – The transaction was successfully processed, 'Not found' – The transaction was not found (was not processed)
            return "Success".equalsIgnoreCase(resultMap.getOrDefault("status", "").toString());
        }

        throw new RuntimeException( "查询结果为空,需要重试" );
    }

}
