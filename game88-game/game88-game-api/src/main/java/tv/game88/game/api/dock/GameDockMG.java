package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MG + "GameProcessor" )
public class GameDockMG extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() ) ) {
            HttpHeaders                   headers = new HttpHeaders();
            MultiValueMap<String, String> params  = new LinkedMultiValueMap<>();
            params.add( "client_id", reqJoinGame.getAgent() );
            params.add( "client_secret", reqJoinGame.getMd5() );
            params.add( "grant_type", "client_credentials" );
            headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>( params, headers );

            Map<String, Object> resultMap = restTemplate.postForObject( reqJoinGame.getRecordUrl(), httpEntity, Map.class );
            Object              obj       = resultMap.get( "access_token" );
            String              token     = obj == null ? null : obj.toString();
            if ( StringUtils.isBlank( token ) ) {
                throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 获取token失败" );
            }
            reqJoinGame.setToken( token );
            redisUtils.strSet( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId(), token, Duration.ofMinutes( 50 ) );
        } else {
            String token = redisUtils.strGet( Constants.GAME_TOKEN_PREX + reqJoinGame.getPlatformId() );
            reqJoinGame.setToken( token );
        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/players?agentCode=" + reqJoinGame.getAgent();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "playerId", reqJoinGame.getGameMemberId() );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseEntity = restTemplate.exchange( url, HttpMethod.POST, requestEntity, Map.class );
        Map                 result         = responseEntity.getBody();
        log.info( JsonUtil.object2Json( result ) );
        if ( responseEntity.getStatusCode().is2xxSuccessful() ) {
            redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
            return;
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( result ) );
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/players/" + reqJoinGame.getGameMemberId()
                + "/sessions?agentCode=" + reqJoinGame.getAgent() + "&playerId=" + reqJoinGame.getGameMemberId();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "contentCode", reqJoinGame.getKindId() );
        params.add( "langCode", "zh-CN" );
        params.add( "platform", "Mobile" );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseEntity = restTemplate.exchange( url, HttpMethod.POST, requestEntity, Map.class );
        Map                 result         = responseEntity.getBody();
        if ( responseEntity.getStatusCode().is2xxSuccessful() ) {
            reqJoinGame.setGameUrl( result.get( "gameURL" ).toString() );
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( result ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/WalletTransactions?agentCode=" + reqJoinGame.getAgent();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "externalTransactionId", reqJoinGame.getOrderId() );
        params.add( "playerId", reqJoinGame.getGameMemberId() );
        params.add( "type", "Deposit" );
        params.add( "amount", reqJoinGame.getTransferMoney() );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseGameResult = null;
        try {
            responseGameResult = restTemplate.exchange( url, HttpMethod.POST, requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分失败" );
        }
        Map result = responseGameResult.getBody();
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( result ), reqJoinGame.getGameMemberId() );
        if ( responseGameResult.getStatusCode().is2xxSuccessful() ) {
            if ( result.get( "status" ).equals( "Succeeded" ) ) {
                return;
            }
        }
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + "上分失败" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/WalletTransactions?agentCode=" + reqJoinGame.getAgent();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "externalTransactionId", reqJoinGame.getOrderId() );
        params.add( "playerId", reqJoinGame.getGameMemberId() );
        params.add( "type", "Withdraw" );
        params.add( "amount", reqJoinGame.getTransferMoney() );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseGameResult = null;
        try {
            responseGameResult = restTemplate.exchange( url, HttpMethod.POST, requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分失败" );
        }
        Map result = responseGameResult.getBody();
        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( result ), reqJoinGame.getGameMemberId() );
        if ( responseGameResult.getStatusCode().is2xxSuccessful() ) {
            if ( result.get( "status" ).equals( "Succeeded" ) ) {
                return;
            }
        }
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + "下分失败" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/players/" + reqJoinGame.getGameMemberId()
                + "?properties=balance";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        log.warn( url + " ::: "  + reqJoinGame.getToken());
        ResponseEntity<Map> responseGameResult = restTemplate.exchange( url, HttpMethod.GET, requestEntity, Map.class );
        if ( responseGameResult.getStatusCode().is2xxSuccessful() ) {
            Map result    = responseGameResult.getBody();
            Map resultMap = ( Map ) result.get( "balance" );
            return new BigDecimal( resultMap.get( "total" ).toString() );
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + reqJoinGame.getAgent() + "/WalletTransactions?agentCode=" + reqJoinGame.getAgent()
                + "&idempotencyKey=" + reqJoinGame.getOrderId();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "agentCode", reqJoinGame.getAgent() );
        params.add( "idempotencyKey", reqJoinGame.getOrderId() );

        HttpHeaders headers = new HttpHeaders();
        headers.add( "Authorization", "Bearer " + reqJoinGame.getToken() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( params, headers );

        ResponseEntity<Map> responseGameResult = restTemplate.exchange( url, HttpMethod.GET, requestEntity, Map.class );
        Map                 resultMap          = responseGameResult.getBody();
        log.info( reqJoinGame.getGameCategory().getDes()
                + "用户:{}获交易明细返回结果:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        if ( responseGameResult.getStatusCode().is2xxSuccessful() ) {
            return "Succeeded".equals( resultMap.get( "status" ) );
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
