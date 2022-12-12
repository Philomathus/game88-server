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
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.KAI_YUAN + "GameProcessor" )
public class GameButtKaiYuan extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
        // 开元没有token,忽略
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        // 开元无需创建账号,进入游戏附带创建账号,忽略
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String time = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&account=%s&money=0&orderid=%s&ip=%s&lineCode=%s&KindID=%s", 0,
                reqJoinGame.getGameMemberId(), reqJoinGame.getOrderId(), reqJoinGame.getIp(), reqJoinGame.getLinecode(),
                reqJoinGame.getKindId() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", reqJoinGame.getAgent() );
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
                String url = d.getOrDefault( "url", "" ).toString();
                reqJoinGame.setGameUrl( url );
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
        String time = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&account=%s&money=%s&orderid=%s", 2, reqJoinGame.getGameMemberId(),
                reqJoinGame.getTransferMoney(), reqJoinGame.getOrderId() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() )
                .queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
                    response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "上分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) ) {
                int code = Integer.parseInt( d.getOrDefault( "code", "-1" ).toString() );
                if ( code == 0 ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String time = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&account=%s&money=%s&orderid=%s", 3, reqJoinGame.getGameMemberId(),
                reqJoinGame.getTransferMoney(), reqJoinGame.getOrderId() );
        String param = null;
        try {
            param = AESCoder.encryptByKey( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", reqJoinGame.getAgent() );
        requestMap.set( "timestamp", time );
        requestMap.set( "param", param );
        requestMap.set( "key", key );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( reqJoinGame.getApiUrl() )
                .queryParams( requestMap )
                .build( true );

        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null ),
                    response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            } );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }
        log.info( reqJoinGame.getGameCategory().getDes()
                + "下分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );
            if ( !CollectionUtils.isEmpty( d ) ) {
                int code = Integer.parseInt( d.getOrDefault( "code", "-1" ).toString() );
                if ( code == 0 ) {
                    return;
                }
            }
        }
        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "下分异常 - 下分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String time   = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&account=%s", 1, reqJoinGame.getGameMemberId() );
        String param  = null;
        try {
            param = AESCoder.encryptByKey( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", reqJoinGame.getAgent() );
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
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String time   = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&orderid=%s", 4, reqJoinGame.getOrderId() );
        String param  = null;
        try {
            param = AESCoder.encryptByKey( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( reqJoinGame.getAgent() + time + reqJoinGame.getMd5() );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "agent", reqJoinGame.getAgent() );
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
                int code   = Integer.parseInt( d.getOrDefault( "code", "-1" ).toString() );
                int status = Integer.parseInt( d.getOrDefault( "status", "-1" ).toString() );
                return code == 0 && status == 0;
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
