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
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@Repository(value = ConstantsGame.JDB + "GameProcessor")
public class GameButtJDB extends AbstractGameButt {

    private final static String TRANSFORMATION = "AES/CBC/NoPadding";

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        //ignore
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        String time = System.currentTimeMillis() + "";
        if (redisUtils.sIsMember(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId())) {
            return;
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "12");
        params.put("ts", time);
        params.put("parent", reqJoinGame.getAgent());
        params.put("uid", reqJoinGame.getMemberId());
        params.put("name", reqJoinGame.getGameMemberId());
        String json = JsonUtil.object2Json(params);
        log.info("Create Account: {}", json);
        try {
            String encodedParam = AESCoder.encryptByKeyUrlIv(json, reqJoinGame.getDes(), reqJoinGame.getMd5(), TRANSFORMATION);
            Map<String, Object> resultMap = execute(evaluateUrl(reqJoinGame.getApiUrl()), encodedParam);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "Create Account:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (!CollectionUtils.isEmpty(resultMap) && "0000".equals(resultMap.get("status").toString())) {
                redisUtils.sAdd(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "11");
        params.put("ts", System.currentTimeMillis() + "");
        params.put("uid", reqJoinGame.getMemberId());
        String json = JsonUtil.object2Json(params);
        log.info("Get Join Game Url: {}", json);
        try {
            String encodedParam = AESCoder.encryptByKeyUrlIv(json, reqJoinGame.getDes(), reqJoinGame.getMd5(), TRANSFORMATION);
            Map<String, Object> resultMap = execute(evaluateUrl(reqJoinGame.getApiUrl()), encodedParam);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "Result Join Game:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (!CollectionUtils.isEmpty(resultMap) && "0000".equals(resultMap.get("status").toString())) {
                reqJoinGame.setGameUrl(resultMap.getOrDefault("path", "").toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
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

    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "15");
        params.put("ts", System.currentTimeMillis() + "");
        params.put("parent", reqJoinGame.getAgent());
        params.put("uid", reqJoinGame.getMemberId());
        String json = JsonUtil.object2Json(params);
        log.info("Query Balance: {}", json);
        try {
            String encodedParam = AESCoder.encryptByKeyUrlIv(json, reqJoinGame.getDes(), reqJoinGame.getMd5(), TRANSFORMATION);
            Map<String, Object> resultMap = execute(evaluateUrl(reqJoinGame.getApiUrl()), encodedParam);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "查询余额:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (!CollectionUtils.isEmpty(resultMap) && "0000".equals(resultMap.get("status").toString())) {
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.getOrDefault("data", new HashMap<>());
                if (!CollectionUtils.isEmpty(dataMap)) {
                    return new BigDecimal(dataMap.getOrDefault("balance", "0").toString());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "55");
        params.put("ts", System.currentTimeMillis() + "");
        params.put("parent", reqJoinGame.getAgent());
        params.put("serialNo", reqJoinGame.getOrderId());
        String json = JsonUtil.object2Json(params);
        log.info("Query Transfer: {}", json);
        try {
            String encodedParam = AESCoder.encryptByKeyUrlIv(json, reqJoinGame.getDes(), reqJoinGame.getMd5(), TRANSFORMATION);
            Map<String, Object> resultMap = execute(evaluateUrl(reqJoinGame.getApiUrl()), encodedParam);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "Query Transfer:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (!CollectionUtils.isEmpty(resultMap) && "0000".equals(resultMap.get("status").toString())) {
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.getOrDefault("data", new HashMap<>());
                if (!CollectionUtils.isEmpty(dataMap)) {
                    return reqJoinGame.getOrderId().equals(dataMap.getOrDefault("pid", "").toString());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        throw new BusinessException("查询结果为空,需要重试");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> execute(String url, String param) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(Map.of("dc", param), httpHeaders);

        return restTemplate.execute(url, HttpMethod.POST,
                restTemplate.httpEntityCallback(requestEntity), response -> {
                    InputStream bodyStream = response.getBody();
                    String text;
                    try (Reader reader = new InputStreamReader(bodyStream)) {
                        text = IOUtils.toString(reader);
                    }
                    return JsonUtil.json2Map(text);
                });
    }

    private String evaluateUrl(String apiUrl) {
        return String.format("%s/apiRequest.do", apiUrl);
    }

    private void transact(ReqJoinGame reqJoinGame, boolean isDeposit) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("action", "19");
        params.put("ts", System.currentTimeMillis() + "");
        params.put("parent", reqJoinGame.getAgent());
        params.put("uid", reqJoinGame.getMemberId());
        params.put("serialNo", reqJoinGame.getOrderId());

        params.put("amount", isDeposit ? reqJoinGame.getTransferMoney().toString() :
                reqJoinGame.getTransferMoney().negate().toString());
        params.put("remark", isDeposit ? "deposit" : "withdraw");
        String json = JsonUtil.object2Json(params);
        log.info("Is Deposit: {}, Transact: {}", isDeposit, json);

        try {
            String encodedParam = AESCoder.encryptByKeyUrlIv(json, reqJoinGame.getDes(), reqJoinGame.getMd5(), TRANSFORMATION);
            Map<String, Object> resultMap = execute(evaluateUrl(reqJoinGame.getApiUrl()), encodedParam);
            log.info(reqJoinGame.getGameCategory().getDes()
                            + "isDeposit: {}, result:{}; userId:{}",
                    isDeposit, JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (!CollectionUtils.isEmpty(resultMap) && "0000".equals(resultMap.get("status").toString()) &&
                    reqJoinGame.getOrderId().equals(resultMap.getOrDefault("serialNo", "").toString())) {
                return;
            }
            throw new GameTransferException(reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
