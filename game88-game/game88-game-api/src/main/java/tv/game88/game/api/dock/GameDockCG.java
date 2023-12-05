package tv.game88.game.api.dock;

import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Repository( value = ConstantsGame.CG + "GameProcessor" )
public class GameDockCG extends AbstractGameDock {

    private static final String ENDPOINT_CREATE_PLAYER = "/td_create_account";
    private static final String ENDPOINT_USER_WALLET_TRANSACTION ="/td_userwallet_transaction";
    private static final String ENDPOINT_CHECK_ORDER_STATUS ="/td_userwallet_transaction_status";
    private static final String ENDPOINT_CHECK_BALANCE = "/td_balance";

    private static final String CURRENCY_BRL = "BRL";

    private static final String LANG = "PT";

    private static final String VERSION = "1.0";



    /**
     * Check whether the result map has error.
     *
     * @param resultMap the map where to get the error code.
     * @return true if the error code is equal to 0, otherwise false.
     */
    private boolean isSuccess(final Map<String, Object> resultMap){
        final Object code = resultMap.get( "errorCode" );
        return "0".equals(String.valueOf(code));
    }

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {

    }

    /**
     * Encrypt value in Map<String, Object> form using AES
     * @param dataMap the message or value to encrypt
     * @param key The Key to be used in the encryption.
     * @param IV The IV to be used in the encryption.
     * @return Encrypted message
     */
    private String getEncryptedData(final Map<String, Object> dataMap, final String key, final String IV) {
        return getEncryptedData(JsonUtil.object2Json(dataMap), key, IV);
    }

    /**
     * Encrypt the String value form using AES
     * @param content the message or value to encrypt
     * @param key The Key to be used in the encryption.
     * @param IV The IV to be used in the encryption.
     * @return Encrypted message
     */
    private String getEncryptedData(final String content, final String key, final String IV) {
        try {
            return AESCoder.encryptByKeyIv(content, key, IV);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypt the specified string value using AES
     * @param response the String value to decrypt
     * @param reqJoinGame contains the Key and IV
     * @return Decrypted message
     */
    private String getDecryptedResponse(final String response, final ReqJoinGame reqJoinGame) {
        try {
            return AESCoder.decryptByKeyIv(response, reqJoinGame.getDes(), reqJoinGame.getMd5());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send HTTP request to the specified URL.
     *
     * @param reqJoinGame contains information about the game
     * @param data field data of the request
     * @param endpoint endpoint of the request
     * @return the response of the HTTP request.
     */
    private Map<String, Object> execute(final ReqJoinGame reqJoinGame, final Map<String, Object> data, final String endpoint){
        final LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("version", VERSION);
        params.put("channelId", reqJoinGame.getPlatformId().toString());
        params.put("data", getEncryptedData(data, reqJoinGame.getDes(), reqJoinGame.getMd5())); //Encrypt the data with Key and IV.

        String body = params.keySet().stream()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.joining("&"));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( null ); //Header content is empty
        HttpEntity<String> requestEntity = new HttpEntity<>( body, httpHeaders );

        String url = reqJoinGame.getApiUrl() + endpoint;

        //Response is encrypted String and must be decrypted using Key and IV.
        final String response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class ).getBody();
        return JsonUtil.json2Object(getDecryptedResponse(response,reqJoinGame), Map.class);
    }
    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        final String rKey = Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId();
        if ( redisUtils.sIsMember( rKey, reqJoinGame.getGameMemberId() ) ) {
            return;
        }

        final Map<String, Object> data = new LinkedHashMap<>();
        data.put( "accountId", reqJoinGame.getMemberId() );
        data.put( "currency", CURRENCY_BRL );

        final Map<String, Object> resultMap = execute(reqJoinGame,data,ENDPOINT_CREATE_PLAYER);
        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            redisUtils.sAdd(rKey, reqJoinGame.getGameMemberId());
            return;
        }

        final String url = reqJoinGame + ENDPOINT_CREATE_PLAYER;
        log.error(reqJoinGame.getGameCategory().getDes()
                + " 创建玩家失败 ->{}; url:{}", JsonUtil.object2Json(resultMap), url);

        throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败");
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        String token = reqJoinGame.getDes() + LocalDateTimeUtils.format( LocalDate.now( ZoneId.of( "America/Caracas" ) ),
                LocalDateTimeUtils.YYYYMMDD_FORMATTER );
        token = getEncryptedData(token, reqJoinGame.getDes(), reqJoinGame.getMd5()); //Encrypt the Token
        token = URLEncoder.encode( token, StandardCharsets.UTF_8 ); //UrlEncode the token

        //Refer to the Excel file for a list of available games for launch game names
        final String gameName = reqJoinGame.getKindId();

        final String params = String.format( "/%s/?version=%s&language=%s&channelId=%s&data=%s",
                gameName, VERSION, LANG, reqJoinGame.getPlatformId().toString(), token);

        final String url = reqJoinGame.getApiUrl() + params;

        final ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            reqJoinGame.setGameUrl(url);
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error(reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", url, reqJoinGame.getGameMemberId());

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
        //Wallet transaction type: 1 - Deposit, 0 - Withdraw
        final int type = isDeposit ? 1 : 0;
        final Map<String, Object> data = new LinkedHashMap<>();
        data.put( "accountId", reqJoinGame.getMemberId() );
        data.put( "currency", CURRENCY_BRL );
        data.put( "serialNumber", reqJoinGame.getOrderId() );
        data.put( "type", type);
        data.put( "amount", reqJoinGame.getTransferMoney() );

        final String now = LocalDateTimeUtils.format(LocalDate.now(ZoneId.of("UTC")), LocalDateTimeUtils.YYYY_MM_DDTHH_MM_SS_FORMATTER);
        data.put("time", now); //The time format is RFC3339, E.g: 2023-10-03T06:55:03.504Z
        data.put( "timevalue", 5 ); //Abort execution after 5 minutes

        final Map<String, Object> resultMap = execute(reqJoinGame,data,ENDPOINT_USER_WALLET_TRANSACTION);

        final String action = isDeposit ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            return;
        }

        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空");

    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        final Map<String, Object> data = new LinkedHashMap<>();
        data.put( "accountId", reqJoinGame.getMemberId() );

        final Map<String, Object> resultMap = execute(reqJoinGame,data,ENDPOINT_CHECK_BALANCE);

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

        final Map<String, Object> data = new LinkedHashMap<>();
        data.put( "currency", CURRENCY_BRL );
        data.put( "serialNumber", reqJoinGame.getOrderId() );

        final Map<String, Object> resultMap = execute(reqJoinGame,data,ENDPOINT_CHECK_ORDER_STATUS);

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        if (!CollectionUtils.isEmpty(resultMap) && isSuccess(resultMap)) {
            //Order Status: -1:Order does not exist, 0: Order completed, 1: Order failure, 2, Order processing
            return "0".equalsIgnoreCase(resultMap.getOrDefault("status", "").toString());
        }

        throw new RuntimeException( "查询结果为空,需要重试" );

    }

}
