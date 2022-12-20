package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.BOLE + "GameProcessor" )
public class GameButtBoLe extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "player_account", reqJoinGame.getGameMemberId() );
        params.add( "country", "zh" );
        params.add( "lang", "zh_CN" );
        params.add( "ip", reqJoinGame.getIp() );
        params.add( "AccessKeyId", reqJoinGame.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.add( "Timestamp", time );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        params.add( "Nonce", nonce );
        params.add( "game_code", reqJoinGame.getKindId() );
        params.add( "op_return_type", 3 );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + nonce + time ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            String url = reqJoinGame.getApiUrl() + "/v1/player/login";
            log.warn( reqJoinGame.getGameCategory().getDes()
                    + "进入游戏 - url : {} ; data : {}", url, JsonUtil.object2Json( params ) );
            resultMap = restTemplate.postForObject( url, requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> resp_msg = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( resp_msg.getOrDefault( "code", "0" ).toString() ) ) {
                Map<String, Object> resp_data = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                reqJoinGame.setGameUrl( resp_data.getOrDefault( "url", "" ).toString() );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "operator_order_id", reqJoinGame.getOrderId() );
        params.add( "player_account", reqJoinGame.getGameMemberId() );
        params.add( "amount", reqJoinGame.getTransferMoney() );
        params.add( "AccessKeyId", reqJoinGame.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.add( "Timestamp", time );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        params.add( "Nonce", nonce );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + nonce + time ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + "/v1/order/coin_in", requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> resp_msg = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( resp_msg.getOrDefault( "code", "0" ).toString() ) ) {
                Map<String, Object> resp_data = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                if ( "2".equals( resp_data.getOrDefault( "status", "" ).toString() ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "operator_order_id", reqJoinGame.getOrderId() );
        params.add( "player_account", reqJoinGame.getGameMemberId() );
        params.add( "amount", reqJoinGame.getTransferMoney() );
        params.add( "AccessKeyId", reqJoinGame.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.add( "Timestamp", time );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        params.add( "Nonce", nonce );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + nonce + time ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.postForObject( reqJoinGame.getApiUrl() + "/v1/order/coin_out", requestEntity, Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> resp_msg = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( resp_msg.getOrDefault( "code", "0" ).toString() ) ) {
                Map<String, Object> resp_data = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                if ( "2".equals( resp_data.getOrDefault( "status", "" ).toString() ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "player_account", reqJoinGame.getGameMemberId() );
        params.add( "AccessKeyId", reqJoinGame.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.add( "Timestamp", time );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        params.add( "Nonce", nonce );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + nonce + time ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = restTemplate.postForObject(
                reqJoinGame.getApiUrl() + "/v1/player/get_info", requestEntity, Map.class );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> resp_msg = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( resp_msg.getOrDefault( "code", "0" ).toString() ) ) {
                Map<String, Object> resp_data = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                if ( resp_data.containsKey( "gold" ) ) {
                    return new BigDecimal( resp_data.getOrDefault( "gold", "0" ).toString() );
                }
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add( "operator_order_id", reqJoinGame.getOrderId() );
        params.add( "AccessKeyId", reqJoinGame.getDes() );
        long time = System.currentTimeMillis() / 1000;
        params.add( "Timestamp", time );
        String nonce = GenerateOrderCacheUtils.me.getOrderId( "", 5 );
        params.add( "Nonce", nonce );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + nonce + time ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = restTemplate.postForObject(
                reqJoinGame.getApiUrl() + "/v1/order/get_info", requestEntity, Map.class );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> resp_msg = ( Map<String, Object> ) resultMap.getOrDefault( "resp_msg", new HashMap<>() );
            if ( "200".equals( resp_msg.getOrDefault( "code", "0" ).toString() ) ) {
                Map<String, Object> resp_data = ( Map<String, Object> ) resultMap.getOrDefault( "resp_data", new HashMap<>() );
                return "2".equals( resp_data.getOrDefault( "status", "" ).toString() );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
