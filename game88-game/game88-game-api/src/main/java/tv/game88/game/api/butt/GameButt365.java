package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
            params.add( "currency", 3 );

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

            String url = reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/getKey";

            Map<String, Object> resultMap = restTemplate.postForObject( url, requestEntity, Map.class );
            String              token     = resultMap.getOrDefault( "key", "" ).toString();
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

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/loginV2", requestEntity, String.class );
        if ( responseEntity.getStatusCode() == HttpStatus.FOUND ) {
            URI location = responseEntity.getHeaders().getLocation();
            reqJoinGame.setGameUrl( location == null ? null : location.toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( responseEntity ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "cert", reqJoinGame.getMd5() );
        params.add( "user", reqJoinGame.getGameMemberId() );
        params.add( "extension1", reqJoinGame.getAgent() );
        params.add( "ts_code", reqJoinGame.getOrderId() );
        params.add( "balance", reqJoinGame.getTransferMoney() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/deposit";

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.postForObject( url, requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals( resultMap.getOrDefault( "status", 0 ).toString() ) ) {
            return;
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "cert", reqJoinGame.getMd5() );
        params.add( "user", reqJoinGame.getGameMemberId() );
        params.add( "extension1", reqJoinGame.getAgent() );
        params.add( "ts_code", reqJoinGame.getOrderId() );
        params.add( "withdrawtype", 0 );
        params.add( "balance", reqJoinGame.getTransferMoney() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/withdraw";

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.postForObject( url, requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals( resultMap.getOrDefault( "status", 0 ).toString() ) ) {
            BigDecimal withdrawBalance = new BigDecimal( resultMap.getOrDefault( "withdrawBalance", "-1" ).toString() );
            if ( withdrawBalance.compareTo( reqJoinGame.getTransferMoney() ) == 0 ) {
                return;
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "cert", reqJoinGame.getMd5() );
        params.add( "users", reqJoinGame.getGameMemberId() );
        params.add( "alluser", 0 );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/getBalance";

        Map<String, Object> resultMap = restTemplate.postForObject( url, requestEntity, Map.class );
        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals( resultMap.getOrDefault( "status", 0 ).toString() ) ) {
            List<Map<String, Object>> results = ( List<Map<String, Object>> ) resultMap.getOrDefault( "results",
                    new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( results ) ) {
                Map<String, Object> result = results.get( 0 );
                return new BigDecimal( result.get( "balance" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "cert", reqJoinGame.getMd5() );
        params.add( "ts_code", reqJoinGame.getOrderId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/api/" + reqJoinGame.getDes() + "/getBalanceOperationLog";

        Map<String, Object> resultMap = restTemplate.postForObject( url, requestEntity, Map.class );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "1".equals( resultMap.getOrDefault( "status", 0 ).toString() ) ) {
            List<Map<String, Object>> result = ( List<Map<String, Object>> ) resultMap.getOrDefault( "result",
                    new ArrayList<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                Map<String, Object> result_ = result.get( 0 );
                return result_.getOrDefault( "tscode", "" ).toString().equals( reqJoinGame.getOrderId() );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
