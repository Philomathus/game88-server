package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
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
import tv.game88.common.utils.StringUtils;
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
public class GameButtSGWin extends AbstractGameButt {

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {
        //Ignore
    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {
        //Ignore
    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("ac", "1");
        param.set( "userCode", reqJoinGame.getGameMemberId());
        param.set( "ip", reqJoinGame.getIp());
        param.set( "gameId", reqJoinGame.getKindId());

        Map<String, Object> resultMap = executeGet( reqJoinGame, param );

        if(!CollectionUtils.isEmpty( resultMap )){
            Map<String,Object> data = (Map<String, Object>)
                    resultMap.getOrDefault("data", Collections.emptyMap());
            if(!data.isEmpty()){
                reqJoinGame.setGameUrl(String.valueOf(data.getOrDefault("fullUrl","")));
            }
        }
        if( StringUtils.isBlank( reqJoinGame.getGameUrl() )){
            throw new BusinessException("获取游戏链接失败");
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame,"3");
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact(reqJoinGame,"4");
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("ac", "2");
        param.set("userCode",reqJoinGame.getGameMemberId());

        Map<String,Object> resultMap = executeGet( reqJoinGame, param);

        if(!CollectionUtils.isEmpty(resultMap)){
            Map<String, Object> data = ( Map<String, Object> )
                    resultMap.getOrDefault("data", Collections.emptyMap());
            if(!data.isEmpty()){
                return new BigDecimal( String.valueOf(
                        data.getOrDefault("money",0)))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("ac", "5");
        param.set("userCode", reqJoinGame.getGameMemberId());
        param.set("orderId", reqJoinGame.getOrderId());

        Map<String, Object> resultMap = executeGet(reqJoinGame,param);

        if(!CollectionUtils.isEmpty(resultMap)){
            Map<String,Object> data = (Map<String, Object>)
                    resultMap.getOrDefault("data", Collections.emptyMap());
            if(!data.isEmpty()){
                int status = Integer.parseInt(String.valueOf(
                        data.getOrDefault("status","-1")));
                return status == 2;
            }
        }
        return false;
    }

    public void transact(ReqJoinGame reqJoinGame, String actionType){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("ac", actionType);
        params.set("userCode", reqJoinGame.getMemberId());
        params.set("money", String.valueOf(reqJoinGame.getTransferMoney()));
        params.set("orderId",reqJoinGame.getOrderId());

        Map<String, Object> requestMap = executeGet (reqJoinGame,params);

        if( !CollectionUtils.isEmpty( requestMap )){
            Map<String,Object> data = (Map<String,Object>)
                    requestMap.getOrDefault( "data", Collections.emptyMap());
            if(!data.isEmpty()){
                int status = Integer.parseInt(String.valueOf( data.getOrDefault("status", "-1")));
                if(status == 0){
                    return;
                }
            }
        }

        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );

    }

    //http://<server>/agentHandle?agentId=XX&timestamp=xx&param=XX&sign=XX
    private Map<String, Object> executeGet(ReqJoinGame reqJoinGame, MultiValueMap<String,String> params){
        try {
            return restTemplate.execute(
                    generateReqUrl(reqJoinGame, params),
                    HttpMethod.GET,
                    restTemplate.httpEntityCallback(null),
                    response -> {
                        InputStream bodyStream = response.getBody();
                        String text;
                        try (Reader reader = new InputStreamReader(bodyStream)) {
                            text = IOUtils.toString(reader);
                        }
                        return JsonUtil.json2Map(text);
                    }
            );
        } catch(RestClientException ignored){
            return null;
        }
    }

    private static String generateReqUrl(ReqJoinGame reqJoinGame, Map<String,?> paramMap){
        long unixTimeStamp = System.currentTimeMillis();
        String param;
        try{
            param = AESCoder.encryptByKeyUrl( assembleParameters( paramMap ), reqJoinGame.getDes());
        } catch (Exception e) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agentId", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", String.valueOf(unixTimeStamp) );
        requestMap.set( "param", param );
        requestMap.set( "sign", DigestUtils.md5Hex(reqJoinGame.getGameMemberId()
                + unixTimeStamp + reqJoinGame.getMd5()));

        // ${apiUrl}/${action}?a=${apiAccount}&t=${unixTimeSeconds}&p=${params}&k=${sign}
        String url = UriComponentsBuilder
                .fromHttpUrl( reqJoinGame.getApiUrl() )
                .queryParams( requestMap )
                .build( true )
                .toUriString();

        log.info( reqJoinGame.getGameCategory().getDes() + "URL: {}", url);

        return url;
    }

    private static String assembleParameters(Map<String, ?> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach( (k, v) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        return sb.substring( 0, sb.length() - 1 );
    }
}
