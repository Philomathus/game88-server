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
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;

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
        params.add( "Nonce", reqJoinGame.getOrderId() );
        params.add( "game_code", reqJoinGame.getKindId() );
        params.add( "op_return_type", 3 );
        params.add( "Sign", DigestUtils.sha1Hex( reqJoinGame.getMd5() + reqJoinGame.getOrderId() + time ) );

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
