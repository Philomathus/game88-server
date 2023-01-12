package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Log4j2
@Repository( value = ConstantsGame.XINGYUN + "GameProcessor" )
public class GameButtXingYun extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", reqJoinGame.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        if ( StringUtils.isNotBlank( reqJoinGame.getKindId() ) ) {
            params.put( "gameid", reqJoinGame.getKindId() );
        }
        params.put( "username", reqJoinGame.getGameMemberId() );
        params.put( "requestip", reqJoinGame.getIp() );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + reqJoinGame.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", reqJoinGame.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/Game/goinGame", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                reqJoinGame.setGameUrl( result.getOrDefault( "game_address", "" ).toString() );
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
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", reqJoinGame.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "orderno", reqJoinGame.getOrderId() );
        params.put( "type", 1 );
        params.put( "username", reqJoinGame.getGameMemberId() );
        params.put( "currency", reqJoinGame.getTransferMoney() );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + reqJoinGame.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", reqJoinGame.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/Transfer/platformTransferToGame", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                if ( reqJoinGame.getOrderId().equals( result.get( "orderno" ) ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", reqJoinGame.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "orderno", reqJoinGame.getOrderId() );
        params.put( "type", 2 );
        params.put( "username", reqJoinGame.getGameMemberId() );
        params.put( "currency", reqJoinGame.getTransferMoney() );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + reqJoinGame.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", reqJoinGame.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/Transfer/platformTransferToGame", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                if ( reqJoinGame.getOrderId().equals( result.get( "orderno" ) ) ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", reqJoinGame.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "username", reqJoinGame.getGameMemberId() );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + reqJoinGame.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", reqJoinGame.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/Users/userBalance", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询余额:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                return new BigDecimal( result.getOrDefault( "amount", "0" ).toString() );
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "platformno", reqJoinGame.getAgent() );
        params.put( "requesttime", System.currentTimeMillis() / 1000 );
        params.put( "orderno", reqJoinGame.getOrderId() );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign  = DigestUtils.md5Hex( sb + "key=" + reqJoinGame.getMd5() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( sb + "sign=" + sign.toUpperCase(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "platformno", reqJoinGame.getAgent() );
        requestMap.put( "parameter", param );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/Transfer/verifyTransferResults", HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
                response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "0".equals( resultMap.get( "code" ).toString() ) ) {
            Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( result ) ) {
                return reqJoinGame.getOrderId().equals( result.get( "orderno" ) ) && reqJoinGame.getGameMemberId()
                                                                                                .equals( result.get( "username" ) );
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
