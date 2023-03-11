package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
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
import java.net.*;
import java.time.Duration;
import java.util.*;

@Log4j2
@Repository(value = ConstantsGame.FG + "GameProcessor")
public class GameButtFG extends AbstractGameButt {

    private static final String HEADER_MERCHANT_NAME = "merchantname";
    private static final String HEADER_MERCHANT_CODE = "merchantcode";

    private static final String PARAMETER_MEMBER_CODE = "member_code";

    private static final String RESULT_DATA = "data";
    private static final String RESULT_TOKEN= "token";

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        if (!redisUtils.exists(Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId())) {

            String URL = reqJoinGame.getApiUrl() + "v3/launch_game/header";

            Map<String, Object> params = new LinkedHashMap<>();
            Map<String, Object> resultMap = getLaunchGameHeaderResultMap(URL, params, reqJoinGame);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "Get Token result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());

            if (isValid(resultMap)) {
                Map<String, Object> dataMap = (Map<String, Object>) resultMap.get(RESULT_DATA);
                reqJoinGame.setToken(String.valueOf(dataMap.get(RESULT_TOKEN)));
                redisUtils.strSet(Constants.GAME_TOKEN_PREX
                        + reqJoinGame.getPlatformId(), reqJoinGame.getToken(), Duration.ofMinutes(29));
            }
        } else {
            String token = redisUtils.strGet(Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId());
            reqJoinGame.setToken(token);
        }

        if (StringUtils.isBlank(reqJoinGame.getToken())) {
            throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 获取token失败");
        }
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {

        if (redisUtils.sIsMember(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId())) {
            return;
        }

        if (checkIfPlayerExists(reqJoinGame)) {
            log.error(reqJoinGame.getGameCategory().getDes() + " - 玩家已经存在 ");
            throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败");
        }

        String url = reqJoinGame.getApiUrl() + "v3/players";

        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_MEMBER_CODE, reqJoinGame.getMemberId());
        params.put("password", reqJoinGame.getDes());
        addRequiredHeaders(reqJoinGame, params);

        Map<String, Object> resultMap = execute(url, params, reqJoinGame);

        if (!isValid(resultMap) || resultMap.get(RESULT_DATA) == null) {
            log.error(reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json(resultMap));
            throw new BusinessException(reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败");
        } else {
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "创造玩家成功 result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
        }

        redisUtils.sAdd(Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId());
    }


    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        String URL = reqJoinGame.getApiUrl() + "v3/launch_game/header";

        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Object> resultMap = getLaunchGameHeaderResultMap(URL, params, reqJoinGame);
        log.info(reqJoinGame.getGameCategory().getDes()
                + "Join Game result:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());

        if (isValid(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get(RESULT_DATA);

            String gameUrl = dataMap.getOrDefault("game_url", "").toString();
            String token = dataMap.getOrDefault(RESULT_TOKEN, "").toString();

            try {
                URI uri = new URIBuilder(gameUrl).addParameter(RESULT_TOKEN, token).build();
                reqJoinGame.setGameUrl(uri.toString());
                reqJoinGame.setToken(token);
            } catch (URISyntaxException e) {
                throw new BusinessException("游戏url为空/无效");
            }
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        TransferOrWithrdawMoney(reqJoinGame);
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        TransferOrWithrdawMoney(reqJoinGame);
    }

    private void TransferOrWithrdawMoney(ReqJoinGame reqJoinGame) {
        String URL = reqJoinGame.getApiUrl() + "v3/player_uchips/member_code";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAMETER_MEMBER_CODE, reqJoinGame.getMemberId());
        params.put("amount", reqJoinGame.getTransferMoney().multiply(new BigDecimal(100)));
        params.put("externaltransactionid", reqJoinGame.getOrderId());

        try {
            Map<String, Object> resultMap = execute(URL, params, reqJoinGame);
            log.info(reqJoinGame.getGameCategory().getDes()
                    + "转移/撤回请求:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            if (CollectionUtils.isEmpty(resultMap) || !resultMap.containsKey(RESULT_DATA)) {
                throw new GameTransferException(reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GameTransferException(e.getMessage());
        }
    }

    private boolean checkIfPlayerExists(ReqJoinGame reqJoinGame) {
        String url = reqJoinGame.getApiUrl() + "v3/player_names";

        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_MEMBER_CODE, reqJoinGame.getMemberId());
        addRequiredHeaders(reqJoinGame, params);

        return isValid(execute(url, params, reqJoinGame));
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        String url = reqJoinGame.getApiUrl() + "v3/player_chips/member_code";

        Map<String, Object> params = new LinkedHashMap<>();
        addRequiredHeaders(reqJoinGame, params);
        params.put(PARAMETER_MEMBER_CODE, reqJoinGame.getMemberId());

        Map<String, Object> resultMap = execute(url, params, reqJoinGame);

        if (!isValid(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get(RESULT_DATA);
            BigDecimal coin = new BigDecimal(dataMap.getOrDefault("balance", "0").toString());

            log.info(reqJoinGame.getGameCategory().getDes()
                    + "查询余额:{}; userId:{}", JsonUtil.object2Json(resultMap), reqJoinGame.getGameMemberId());
            return coin;
        }

        log.error(reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json(resultMap));
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        String URL = reqJoinGame.getApiUrl() + "v3/player_uchips_check/";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("externaltransactionid", reqJoinGame.getOrderId());

        Map<String, Object> resultMap = execute(URL, params, reqJoinGame);

        try {
            return isValid(resultMap);
        } catch (Exception e) {
            log.error(reqJoinGame.getGameCategory().getDes()
                    + "查询结果为空,需要重试userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json(resultMap));
            throw new RuntimeException("查询结果为空,需要重试");
        }
    }

    private Map<String, Object> getLaunchGameHeaderResultMap(String url, Map<String, Object> params, ReqJoinGame reqJoinGame) {
        params.put(PARAMETER_MEMBER_CODE, reqJoinGame.getMemberId());
        params.put("game_type", reqJoinGame.getGameCategory().toString());
        params.put("language", "zh-cn");
        params.put("ip", reqJoinGame.getIp());
        params.put("return_url", reqJoinGame.getGameUrl());

        log.info("Before API Call: {}", JsonUtil.object2Json(params));
        return execute(url, params, reqJoinGame);
    }

    private boolean isValid(Map<String, Object> resultMap) {
        if (!CollectionUtils.isEmpty(resultMap)) {
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get(RESULT_DATA);
            return !CollectionUtils.isEmpty(dataMap) && "0".equals(resultMap.getOrDefault("code", "")) &&
                    "success".equals(resultMap.getOrDefault("msg", ""));
        }
        return false;
    }

    private void addRequiredHeaders(ReqJoinGame reqJoinGame, Map<String, Object> params) {
        params.put(HEADER_MERCHANT_NAME, reqJoinGame.getAgent());
        params.put(HEADER_MERCHANT_CODE, reqJoinGame.getDes());
    }

    private Map<String, Object> execute(String url, Map<String, Object> params, ReqJoinGame reqJoinGame) {

        addRequiredHeaders(reqJoinGame, params);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.set(HEADER_MERCHANT_NAME, String.valueOf(params.get(HEADER_MERCHANT_NAME)));
        httpHeaders.set(HEADER_MERCHANT_CODE, String.valueOf(params.get(HEADER_MERCHANT_CODE)));

        HttpEntity<String> requestEntity = new HttpEntity<>(JsonUtil.object2Json(params), httpHeaders);

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
}
