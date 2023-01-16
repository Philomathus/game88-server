package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
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
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository(value = ConstantsGame.CQ9 + "GameProcessor")
@SuppressWarnings("unchecked")
public class GameButtCQ9 extends AbstractGameButt {

    private final static String DEPOSIT = "Deposit";
    private final static String WITHDRAW = "Withdraw";

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        String key = Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() + ":" + reqJoinGame.getGameMemberId();
        if (redisUtils.exists(key)) {
            String token = redisUtils.strGet(key);
            reqJoinGame.setToken(token);
        } else {
            String url = reqJoinGame.getApiUrl() + "/gameboy/player/login";
            Map<String, String> params = new LinkedHashMap<>();
            params.put("account", reqJoinGame.getAgent());
            params.put("password", reqJoinGame.getDes());
            log.info("Get Token: {}", JsonUtil.object2Json(params));
            Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "Token result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());

            if (isValid(resultMap)) {
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
                String token = dataMap.getOrDefault("usertoken", "").toString();
                if (StringUtils.isBlank(token)) {
                    log.error(reqJoinGame.getGameCategory().getDes() + " 获取token失败 ->{}", JsonUtil.object2Json(resultMap));
                    throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 获取token失败");
                }
                reqJoinGame.setToken(token);
                redisUtils.strSet(key, token, Duration.ofMinutes(9L));
            }
        }
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        if (redisUtils.sIsMember(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId())) {
            return;
        }
        String url = reqJoinGame.getApiUrl() + "/gameboy/player";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("account", reqJoinGame.getGameMemberId());
        params.put("password", reqJoinGame.getDes());
        log.info("Create Account: {}", JsonUtil.object2Json(params));
        Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Create Account result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        if (isValid(resultMap)) {
            redisUtils.sAdd(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId());
        } else {
            throw new BusinessException("Cannot create account");
        }
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        String url = reqJoinGame.getApiUrl() + "/gameboy/player/gamelink";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("usertoken", reqJoinGame.getToken());
        params.put("gamehall", reqJoinGame.getKindId());
        params.put("gamecode", reqJoinGame.getLinecode());
        params.put("gameplat", "mobile");
        params.put("lang", "zh-cn");
        log.info("Join Game: {}", JsonUtil.object2Json(params));
        Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Join Game result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        if (isValid(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            String gameUrl = dataMap.getOrDefault("url", "").toString();
            if (StringUtils.isEmpty(gameUrl)) {
                throw new BusinessException("Game url is empty");
            } else {
                reqJoinGame.setGameUrl(gameUrl);
            }
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        String url = reqJoinGame.getApiUrl() + "/gameboy/player/deposit";
        transact(reqJoinGame, url, DEPOSIT);
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        String url = reqJoinGame.getApiUrl() + "/gameboy/player/withdraw";
        transact(reqJoinGame, url, WITHDRAW);
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/gameboy/player/balance/%s", reqJoinGame.getApiUrl(), reqJoinGame.getGameMemberId());
        log.info("Query Balance: {}", url);
        Map<String, Object> resultMap = execute(HttpMethod.GET, url, null);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Query Balance result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        if (isValid(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            return new BigDecimal(dataMap.getOrDefault("balance", "0").toString());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/gameboy/transaction/record/%s", reqJoinGame.getApiUrl(), reqJoinGame.getOrderId());
        log.info("Query Transfer: {}", url);
        Map<String, Object> resultMap = execute(HttpMethod.GET, url, null);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Query Transfer result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        if (isValid(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            return reqJoinGame.getOrderId().equals(dataMap.getOrDefault("mtcode", "").toString());
        }
        throw new BusinessException("查询结果为空,需要重试");
    }

    private Map<String, Object> execute(HttpMethod method, String url, Map<String, String> params) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> requestEntity = method == HttpMethod.GET ? null :
                new HttpEntity<>(params, httpHeaders);

        return restTemplate.execute(url, method,
                restTemplate.httpEntityCallback(requestEntity), response -> {
                    InputStream bodyStream = response.getBody();
                    String text;
                    try (Reader reader = new InputStreamReader(bodyStream)) {
                        text = IOUtils.toString(reader);
                    }
                    return JsonUtil.json2Map(text);
                });
    }

    private static boolean isValid(Map<String, Object> resultMap) {
        boolean result = false;
        if (!CollectionUtils.isEmpty(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            Map<String, Object> statusMap = (Map<String, Object>) resultMap.get("status");
            if (!CollectionUtils.isEmpty(dataMap) && !CollectionUtils.isEmpty(statusMap) &&
                    "0".equals(statusMap.getOrDefault("code", "")) &&
                    "Success".equals(statusMap.getOrDefault("message", ""))) {
                result = true;
            }
        }
        return result;
    }

    private void transact(ReqJoinGame reqJoinGame, String url, String type) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("account", reqJoinGame.getGameMemberId());
        params.put("mtcode", reqJoinGame.getOrderId());
        params.put("amount", reqJoinGame.getTransferMoney().toString());
        log.info("Type: {}, Transfer Money: {}", type, JsonUtil.object2Json(params));
        Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                        + "Type: {}, Transfer Money result:{}; userId:{}", type,
                JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        if (!isValid(resultMap)) {
            throw new GameTransferException(reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空");
        }
    }
}
