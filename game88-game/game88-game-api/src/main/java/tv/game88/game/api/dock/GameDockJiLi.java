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
import org.apache.commons.lang3.RandomStringUtils;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.JILI + "GameProcessor" )
public class GameDockJiLi extends AbstractGameDock {

    private static final String ENDPOINT_CREATE_MEMBER = "/CreateMember";
    private static final String ENDPOINT_LOGIN_GAME = "/LoginWithoutRedirect";
    private static final String ENDPOINT_BALANCE_TRANSFER = "/ExchangeTransferByAgentId";
    private static final String ENDPOINT_QUERY_CREDIT_TRANSFER = "/CheckTransferByTransactionId";
    private static final String ENDPOINT_QUERY_MEMBER_STATUS = "/GetMemberInfo";

    private static final String LANG = "pt-BR";

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
        //httpHeaders.setCacheControl("no-cache");
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
     *<pre>  Key is generated with this formula: Key = { 6 any characters } + MD5( All request parameters + KeyG) + { 6 any characters }
     * Where:
     *    - 6 any characters: Generated randomly.
     *    - All request parameters: Concentrate API parameters by the format: key1=value1&key2=value2
     *    - KeyG = MD5(DateTime.Now.ToString(“yyMMd”) + AgentId + AgentKey) </pre>
     *
     * @param params contains the parameters in key value pair.
     * @param reqJoinGame reqJoinGame that contains AgentId and Agent Key.
     *
     * @return API Key ({ 6 any characters } + MD5( All request parameters + KeyG) + { 6 any characters }).
     */
    private String getKey(final Map<String, Object> params, final ReqJoinGame reqJoinGame){
        final String now = LocalDateTimeUtils.format(LocalDate.now(ZoneId.of("UTC-4")),
                DateTimeFormatter.ofPattern("yyMMd"));
        final String keyG = DigestUtils.md5Hex(now + reqJoinGame.getAgent() + reqJoinGame.getMd5()); //KeyG = MD5(DateTime.Now.ToString(“yyMMd”) + AgentId + AgentKey)

        final String queryString = keyValStringFormat(params); //E.g.: key1=value1&key2=value2
        final String md5string = DigestUtils.md5Hex(queryString + keyG);

        final String preRandomText = RandomStringUtils.randomAlphabetic(6);
        final String postRandomText = RandomStringUtils.randomAlphabetic(6);

        //{ 6 any characters } + MD5( All request parameters + KeyG) + { 6 any characters }
        return preRandomText + md5string + postRandomText;
    }


    /**
     * Format the specified parameters.
     *  E.g.: key1=value1&key2=value2
     *
     * @param params parameters.
     * @return formatted key value String.
     */
    private String keyValStringFormat(final Map<String, Object> params){
        return params.keySet().stream()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.joining("&"));
    }

    /**
     * Check whether the result map has error.
     *
     * @param resultMap the map where to get the error code.
     * @return true if the error code is equal to 0, otherwise false.
     */
    private boolean isSuccess(final Map<String, Object> resultMap){
        final Object code = resultMap.get( "ErrorCode" );
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
                .append(endpoint)
                .append("?")
                .append(keyValStringFormat(params));
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

        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "Account", reqJoinGame.getMemberId() );

        params.put( "AgentId", reqJoinGame.getAgent() );
        params.put( "Key", getKey(params, reqJoinGame) );

        final String json = JsonUtil.object2Json( params );
        log.info( "Create Account: {}", json );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_CREATE_MEMBER, params);
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
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "Account", reqJoinGame.getMemberId() );
        params.put( "GameId", reqJoinGame.getKindId() );
        params.put( "Lang", LANG );

        params.put( "AgentId", reqJoinGame.getAgent() );
        params.put( "Key", getKey(params, reqJoinGame) );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_LOGIN_GAME, params);
        final Map<String, Object> resultMap = execute(url);

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            reqJoinGame.setGameUrl(resultMap.getOrDefault("Data", "").toString());
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
        final Map<String, Object> params = new LinkedHashMap<>();
        int transferType = isDeposit ? 2 : 3;

        params.put( "Account", reqJoinGame.getMemberId() );
        params.put( "TransactionId", reqJoinGame.getOrderId() );
        params.put( "Amount", reqJoinGame.getTransferMoney() );
        params.put( "TransferType", transferType );

        params.put( "AgentId", reqJoinGame.getAgent() );
        params.put( "Key", getKey(params, reqJoinGame) );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_BALANCE_TRANSFER, params);
        final Map<String, Object> resultMap = execute(url);

        final String action = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            final Map<String, Object> data = (Map<String, Object>) resultMap.getOrDefault("Data", Collections.emptyMap());
            //Status: 1 - Sucess, 2 - Failed
            if (!data.isEmpty() && "1".equals(data.getOrDefault("Status", "").toString())) {
                return;
            }
        }

        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空");

    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "Account", reqJoinGame.getMemberId() );

        params.put( "AgentId", reqJoinGame.getAgent() );
        params.put( "Key", getKey(params, reqJoinGame) );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_QUERY_MEMBER_STATUS, params);
        final Map<String, Object> resultMap = execute(url);

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额 - userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            final Map<String, Object> data = (Map<String, Object>) resultMap.getOrDefault("Data", Collections.emptyMap());
            if (!data.isEmpty()) {
                return new BigDecimal((String.valueOf(resultMap.getOrDefault("Balance", "")))).setScale(2, RoundingMode.DOWN);
            }
        }

        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );

        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        final Map<String, Object> params = new LinkedHashMap<>();
        params.put( "TransactionId", reqJoinGame.getOrderId() );

        params.put( "AgentId", reqJoinGame.getAgent() );
        params.put( "Key", getKey(params, reqJoinGame) );

        final String url = getURL(reqJoinGame.getApiUrl(), ENDPOINT_QUERY_CREDIT_TRANSFER, params);
        final Map<String, Object> resultMap = execute(url);

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            final Map<String, Object> data = (Map<String, Object>) resultMap.getOrDefault("Data", Collections.emptyMap());
            if (!data.isEmpty()) {
                //Status: 1 - Sucess, 2 - Failed, 3 - Processing
                return "1".equals(data.getOrDefault("Status", "").toString());
            }
        }

        throw new RuntimeException( "查询结果为空,需要重试" );
    }

}
