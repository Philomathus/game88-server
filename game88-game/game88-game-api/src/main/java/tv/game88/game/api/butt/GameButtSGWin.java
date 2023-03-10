package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        String time = System.currentTimeMillis() + "";
        String params = String.format( "ac=%s&userCode=%s&ip=%s&gameId=%s&money=0",
                        //ac
                        reqJoinGame.getGameMemberId(),
                        //userCode
                        reqJoinGame.getMemberId(),
                        //ip
                        reqJoinGame.getIp(),
                        //gameID
                        Objects.equals( reqJoinGame.getDev(), 1 ) ? 4 : 2, reqJoinGame.getKindId());
        String param = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agentId", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() )
                .queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) ) {
                String url = d.getOrDefault( "url", "" ).toString();
                reqJoinGame.setGameUrl( url );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                            + "获取游戏链接失败:{}; userId:{}; url:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId(),
                    uriComponents
                            .toUri().toString() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {

    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {

    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {

        String time   = System.currentTimeMillis() + "";
        String params = String.format( "ac=%s&userCode=%s",
                reqJoinGame.getGameMemberId(),
                reqJoinGame.getLinecode() );
        String param  = null;
        try {
            param = AESCoder.encryptByKeyUrl( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agentId", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() )
                .queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( null ), response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return JsonUtil.json2Map( text );
                } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) ) {
                int        code  = Integer.parseInt( d.getOrDefault( "code", "-1" ).toString() );
                BigDecimal money = new BigDecimal( d.getOrDefault( "money", "0" ).toString() );
                if ( code == 0 ) {
                    return money;
                }
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        //3.2.5
        return false;
    }
}
