package tv.game88.game.api.dock;

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
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.MEIBO + "GameProcessor" )
public class GameDockMeiBo extends AbstractGameDock {
    private static final String MD5 = "WCPT";

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String time = System.currentTimeMillis() + "";
        String params = String.format( "s=%s&account=%s&lineCode=%s&KindID=%s&language=chinese_zh&loginHall=true&backHall"
                + "=false", 0, reqJoinGame.getGameMemberId(), reqJoinGame.getLinecode(), reqJoinGame.getKindId() );
        String param = null;
        try {
            param = AESCoder.encryptByKeyIv( params, reqJoinGame.getDes(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "agent=%s&timestamp=%s&MD5Key=%s", reqJoinGame.getAgent(), time, MD5 );

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "agent", reqJoinGame.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/third/login";
        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
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
                reqJoinGame.setGameUrl( d.getOrDefault( "url", "" ).toString() );
            }
        }
        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}; url:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId(), url );
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
            param = AESCoder.encryptByKeyIv( params, reqJoinGame.getDes(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "agent=%s&timestamp=%s&MD5Key=%s", reqJoinGame.getAgent(), time, MD5 );

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "agent", reqJoinGame.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String              url       = reqJoinGame.getApiUrl() + "/third/addPlayerMoney";
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
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
            param = AESCoder.encryptByKeyIv( params, reqJoinGame.getDes(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "agent=%s&timestamp=%s&MD5Key=%s", reqJoinGame.getAgent(), time, MD5 );

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "agent", reqJoinGame.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String              url       = reqJoinGame.getApiUrl() + "/third/lowerPlayerMoney";
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ),
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
        String params = String.format( "s=%s&account=%s", 8, reqJoinGame.getGameMemberId() );
        String param  = null;
        try {
            param = AESCoder.encryptByKeyIv( params, reqJoinGame.getDes(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "agent=%s&timestamp=%s&MD5Key=%s", reqJoinGame.getAgent(), time, MD5 );

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "agent", reqJoinGame.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/third/queryPlayerGold";
        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
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
                BigDecimal money = new BigDecimal( d.getOrDefault( "freeMoney", "0" ).toString() );
                if ( code == 0 ) {
                    return money.compareTo( BigDecimal.ZERO )
                            > 0 ? money.divide( new BigDecimal( 100 ), 2, RoundingMode.HALF_UP ) : BigDecimal.ZERO;
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
            param = AESCoder.encryptByKeyIv( params, reqJoinGame.getDes(), reqJoinGame.getMd5() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String keyParams = String.format( "agent=%s&timestamp=%s&MD5Key=%s", reqJoinGame.getAgent(), time, MD5 );

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "agent", reqJoinGame.getAgent() );
        requestMap.put( "timestamp", time );
        requestMap.put( "param", param );
        requestMap.put( "key", DigestUtils.md5Hex( keyParams ) );

        log.warn( JsonUtil.object2Json( requestMap ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>( requestMap, httpHeaders );

        String url = reqJoinGame.getApiUrl() + "/third/queryOrderId";
        Map<String, Object> resultMap = restTemplate.execute( url, HttpMethod.POST,
                restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> d = ( Map<String, Object> ) resultMap.getOrDefault( "d", new HashMap<>() );

            int code   = Integer.parseInt( d.getOrDefault( "code", "-1" ).toString() );
            int status = Integer.parseInt( d.getOrDefault( "status", "-1" ).toString() );
            if ( status != 3 ) {
                return code == 0 && status == 0;
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }
}
