package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.GAMING_365 + "GameProcessor" )
public class GameButt365 extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        String key = Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() + ":" + reqJoinGame.getGameMemberId();
        if ( !redisUtils.exists( key ) ) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add( "cert", reqJoinGame.getMd5() );
            params.add( "user", reqJoinGame.getGameMemberId() );
            params.add( "userName", reqJoinGame.getGameMemberId().split( "_" )[ 1 ] );
            params.add( "extension1", reqJoinGame.getAgent() );
            params.add( "currency", "CNY" );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

            Map<String, Object> resultMap = restTemplate.postForObject(
                    reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/getKey", requestEntity, Map.class );
            String token = resultMap.getOrDefault( "key", "" ).toString();
            if ( StringUtils.isBlank( token ) ) {
                log.error( reqJoinGame.getGameCategory().getDes() + " 获取token失败 ->{}", JsonUtil.object2Json( resultMap ) );
                throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取token失败" );
            }
            reqJoinGame.setToken( token );
            redisUtils.strSet( key, token, Duration.ofMinutes( 9 ) );
        } else {
            String token = redisUtils.strGet( key );
            reqJoinGame.setToken( token );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        // 获取token时已创建账号, 忽略
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "key", reqJoinGame.getToken() );
        params.add( "user", reqJoinGame.getGameMemberId() );
        params.add( "userName", reqJoinGame.getGameMemberId().split( "_" )[ 1 ] );
        params.add( "extension1", reqJoinGame.getAgent() );
        params.add( "gameId", reqJoinGame.getKindId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = restTemplate.postForObject(
                reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/loginV2", requestEntity, Map.class );

    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        return null;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        return false;
    }
}
