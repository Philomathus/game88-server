package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.dto.XiaFenResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.ICG + "GameProcessor" )
public class GameButtICG extends AbstractGameButt {

    private static final String LOGIN      = "/login";
    private static final String COMMON_URL = "/api/v1/players";
    private static final String TO_ICG     = "/deposit";
    private static final String TO_QIQI    = "/withdraw";
    private static final String GAME       = "/api/v1/games";

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + ConstantsGame.ICG ) ) {
            Map<String, String> map = new HashMap<>();
            map.put( "username", reqJoinGame.getAgent() );
            map.put( "password", reqJoinGame.getDes() );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_JSON );
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( map, httpHeaders );

            Map<String, Object> resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + LOGIN, httpEntity, Map.class );
            String              token     = resultMap.get( "token" ) == null ? null : resultMap.get( "token" ).toString();
            if ( StringUtils.isBlank( token ) ) {
                throw new BusinessException( "ICG - 获取token失败" );
            }
            reqJoinGame.setToken( token );
            redisUtils.strSet( Constants.GAME_TOKEN_PREX + ConstantsGame.ICG, token, Duration.ofDays( 80 ) );
        } else {
            String token = redisUtils.strGet( Constants.GAME_TOKEN_PREX + ConstantsGame.ICG );
            reqJoinGame.setToken( token );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( !redisUtils.sIsMember( Constants.GAME_USERS_PREX + ConstantsGame.ICG, reqJoinGame.getGameMemberId() ) ) {
            Map<String, String> map = new HashMap<>();
            map.put( "username", reqJoinGame.getGameMemberId() );
            map.put( "nickname", reqJoinGame.getGameMemberId() );
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_JSON );
            httpHeaders.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
            HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( map, httpHeaders );
            Map<String, Object>             resultMap  = null;
            try {
                resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + COMMON_URL, httpEntity, Map.class );
            } catch ( Exception e ) {
                if ( !e.getMessage().contains( "username already exists" ) ) {
                    log.error( "ICG - 创建玩家失败 - 失败原因:" + e.getMessage(), e );
                    throw new BusinessException( "ICG - 创建玩家失败" );
                }
            }
            if ( resultMap.get( "data" ) == null ) {
                log.error( "ICG 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
                throw new BusinessException( "ICG - 创建玩家失败" );
            }
            redisUtils.sAdd( Constants.GAME_USERS_PREX + ConstantsGame.ICG, reqJoinGame.getGameMemberId() );
        }
    }

    @Override
    public String getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        return null;
    }

    @Override
    public boolean transferMoney( ReqJoinGame reqJoinGame ) {

        return false;
    }

    @Override
    public XiaFenResult withdrawal( ReqJoinGame reqJoinGame ) {
        return null;
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        return null;
    }
}
