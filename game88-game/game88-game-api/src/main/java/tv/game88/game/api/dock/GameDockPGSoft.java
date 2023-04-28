package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static tv.game88.core.config.constants.Constants.GAME_PGSOFT_KEY;
import static tv.game88.core.config.constants.Constants.GAME_PGSOFT_OPS;
import static tv.game88.core.config.constants.Constants.GAME_PGSOFT_OT;

@Log4j2
@Repository(value = ConstantsGame.PG_SOFT + "GameProcessor")
@SuppressWarnings("unchecked")
public class GameDockPGSoft extends AbstractGameDock {
    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        //ignore
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        String token = UUID.randomUUID().toString();
        redisUtils.strSet(GAME_PGSOFT_OT + reqJoinGame.getGameMemberId(), reqJoinGame.getDes());
        redisUtils.strSet(GAME_PGSOFT_KEY + reqJoinGame.getGameMemberId(), reqJoinGame.getMd5());
        redisUtils.strSet(GAME_PGSOFT_OPS + reqJoinGame.getGameMemberId(), token);
        String logs = String.format("[Ot: %s, Key: %s, Ops: %s]", reqJoinGame.getDes(), reqJoinGame.getMd5(), token);
        log.info("Create Account: ID - {}, Info - {}", reqJoinGame.getGameMemberId(), logs);
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/%s/index.html", reqJoinGame.getApiUrl(), reqJoinGame.getPlatformId());
        Map<String, String> params = new LinkedHashMap<>();
        params.put("btt", "3");
        params.put("ot", reqJoinGame.getDes());
        params.put("ops", redisUtils.strGet(GAME_PGSOFT_OPS + reqJoinGame.getGameMemberId()));
        params.put("op", reqJoinGame.getGameMemberId());
        url = String.format("%s?%s", url, assemblyUrl(params));
        Map<String, Object> resultMap = execute(HttpMethod.GET, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Join Game result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/Cash/v3/TransferIn", reqJoinGame.getApiUrl());
        transact(reqJoinGame, url);
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/Cash/v3/TransferOut", reqJoinGame.getApiUrl());
        transact(reqJoinGame, url);
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        String url = String.format("%s/Cash/v3/GetPlayerWallet", reqJoinGame.getApiUrl());
        Map<String, String> params = new LinkedHashMap<>();
        params.put("operator_token", reqJoinGame.getDes());
        params.put("secret_key", reqJoinGame.getMd5());
        params.put("player_name", reqJoinGame.getGameMemberId());
        Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Query Balance result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        Map<String, Object> dataMap = getDataMapIfValid(resultMap);
        if (!CollectionUtils.isEmpty(dataMap)) {
            return new BigDecimal(dataMap.getOrDefault("totalBalance", "0").toString())
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        return false;
    }

    private void transact(ReqJoinGame reqJoinGame, String url) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("operator_token", reqJoinGame.getDes());
        params.put("secret_key", reqJoinGame.getMd5());
        params.put("player_name", reqJoinGame.getGameMemberId());
        params.put("amount", reqJoinGame.getTransferMoney().toString());
        params.put("transfer_reference", reqJoinGame.getOrderId());
        params.put("currency", "CNY");
        Map<String, Object> resultMap = execute(HttpMethod.POST, url, params);
        log.info(reqJoinGame.getGameCategory().getDes()
                        + "Transact result:{}; userId:{}",
                JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        Map<String, Object> dataMap = getDataMapIfValid(resultMap);
        if (!CollectionUtils.isEmpty(dataMap)) {
            if (reqJoinGame.getOrderId().equals(dataMap.getOrDefault("transactionId", "").toString())) {
                return;
            }
        }
        throw new GameTransferException(reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空");
    }

    private static String assemblyUrl(Map<String, ?> bodyMap) {
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        return sb.substring(0, sb.length() - 1);
    }

    private Map<String, Object> execute(HttpMethod method, String url, Map<String, String> params) {
        String traceId = UUID.randomUUID().toString();
        String param = JsonUtil.object2Json(params);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("trace_id", traceId);
        log.info("Request - url: {}, params: {}", url, param);
        HttpEntity<String> requestEntity =
                new HttpEntity<>(HttpMethod.GET == method ? null : param, httpHeaders);

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

    private Map<String, Object> getDataMapIfValid(Map<String, Object> resultMap) {
        if (!CollectionUtils.isEmpty(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            Map<String, Object> errorMap = (Map<String, Object>) resultMap.get("error");
            if (CollectionUtils.isEmpty(errorMap) && !CollectionUtils.isEmpty(dataMap)) {
                return dataMap;
            }
        }
        return null;
    }
}
