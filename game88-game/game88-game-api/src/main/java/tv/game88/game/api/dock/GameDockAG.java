package tv.game88.game.api.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Document;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.DesCoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.XmlUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameDock;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Log4j2
@Repository( value = ConstantsGame.AG + "GameProcessor" )
public class GameDockAG extends AbstractGameDock {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }
        String params = String.format( "cagent=%s/\\\\\\\\/method=lg/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password"
                + "=%s/\\\\\\\\/cur=CNY/\\\\\\\\/oddtype=A", reqJoinGame.getAgent(), reqJoinGame.getGameMemberId(),
                reqJoinGame.getGameMemberId() );
        Document document = request( params, reqJoinGame );

        String status = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "info" )
                                .getTextContent();
        if ( "0".equals( status ) ) {
            redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
            return;
        }
        throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String params = String.format( "cagent=%s/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password=%s/\\\\\\\\/cur=CNY"
                        + "/\\\\\\\\/oddtype=A/\\\\\\\\/dm=https://88"
                        + ".tv?AGGameQuit/\\\\\\\\/sid=%s/\\\\\\\\/lang=1/\\\\\\\\/gameType=%s", reqJoinGame.getAgent(),
                reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(),
                reqJoinGame.getAgent() + reqJoinGame.getOrderId(), reqJoinGame.getKindId() );
        String targetParams = null;
        try {
            targetParams = DesCoder.encrypt( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( targetParams + reqJoinGame.getMd5() );
        String url = reqJoinGame.getRecordUrl() + "forwardGame.do?params=" + targetParams + "&key=" + key;
        reqJoinGame.setGameUrl( url );
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        String params = String.format( "cagent=%s/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password=%s/\\\\\\\\/cur=CNY"
                + "/\\\\\\\\/method=tc/\\\\\\\\/billno=%s/\\\\\\\\/type=IN/\\\\\\\\/credit=%s", reqJoinGame.getAgent(),
                reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(), reqJoinGame.getOrderId(),
                reqJoinGame.getTransferMoney() );
        Document document = null;
        try {
            document = request( params, reqJoinGame );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }

        String msg = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "msg" ).getTextContent();
        if ( StringUtils.isBlank( msg ) ) {
            String paramsC = String.format( "cagent=%s/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password=%s/\\\\\\\\/cur"
                    + "=CNY/\\\\\\\\/flag=1/\\\\\\\\/method=tcc/\\\\\\\\/billno=%s/\\\\\\\\/type=IN/\\\\\\\\/credit=%s",
                    reqJoinGame.getAgent(), reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(),
                    reqJoinGame.getOrderId(), reqJoinGame.getTransferMoney() );
            Document documentC = null;
            try {
                documentC = request( paramsC, reqJoinGame );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
                throw new GameTransferException( "确认上分失败" );
            }
            String status = documentC.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "info" )
                                     .getTextContent();
            if ( status.equals( "0" ) ) {
                return;
            }
        }
        throw new GameTransferException( "确认上分失败" );
    }

    private Document request( String params, ReqJoinGame reqJoinGame ) {
        String targetParams = null;
        try {
            targetParams = DesCoder.encrypt( params, reqJoinGame.getDes() );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }
        String key = DigestUtils.md5Hex( targetParams + reqJoinGame.getMd5() );
        String url = reqJoinGame.getApiUrl() + "doBusiness.do?params=" + targetParams + "&key=" + key;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "User-Agent", "WEB_LIB_GI_GY9_AGIN" );
        HttpEntity<?> httpEntity = new HttpEntity<>( httpHeaders );

        String resultXml = restTemplate.execute( url, HttpMethod.GET, restTemplate.httpEntityCallback( httpEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return text;
        } );
        if ( StringUtils.isNotBlank( resultXml ) ) {
            try {
                return XmlUtil.getDocument( resultXml );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
                throw new BusinessException( e.getMessage() );
            }
        }
        log.error( reqJoinGame.getGameCategory().getDes() + " 获取数据失败 ->{}", resultXml );
        throw new BusinessException( "获取数据失败" );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        String params = String.format( "cagent=%s/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password=%s/\\\\\\\\/cur=CNY"
                + "/\\\\\\\\/method=tc/\\\\\\\\/billno=%s/\\\\\\\\/type=OUT/\\\\\\\\/credit=%s", reqJoinGame.getAgent(),
                reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(), reqJoinGame.getOrderId(),
                reqJoinGame.getTransferMoney() );
        Document document = null;
        try {
            document = request( params, reqJoinGame );
        } catch ( Exception e ) {
            throw new GameTransferException( e.getMessage() );
        }

        String msg = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "msg" ).getTextContent();
        if ( StringUtils.isBlank( msg ) ) {
            String paramsC = String.format( "cagent=%s/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1/\\\\\\\\/password=%s/\\\\\\\\/cur"
                    + "=CNY/\\\\\\\\/flag=1/\\\\\\\\/method=tcc/\\\\\\\\/billno=%s/\\\\\\\\/type=OUT/\\\\\\\\/credit=%s",
                    reqJoinGame.getAgent(), reqJoinGame.getGameMemberId(), reqJoinGame.getGameMemberId(),
                    reqJoinGame.getOrderId(), reqJoinGame.getTransferMoney() );
            Document documentC = null;
            try {
                documentC = request( paramsC, reqJoinGame );
            } catch ( BusinessException e ) {
                log.error( e.getMessage(), e );
                throw new BusinessException( "确认下分失败" );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
                throw new GameTransferException( "确认下分失败" );
            }
            String status = documentC.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "info" )
                                     .getTextContent();
            if ( status.equals( "0" ) ) {
                return;
            }
        }
        throw new BusinessException( "确认下分失败" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String params = String.format( "cagent=%s/\\\\\\\\/cur=CNY/\\\\\\\\/actype=1/\\\\\\\\/loginname=%s/\\\\\\\\/actype=1"
                + "/\\\\\\\\/method=gb/\\\\\\\\/password=%s", reqJoinGame.getAgent(), reqJoinGame.getGameMemberId(),
                reqJoinGame.getGameMemberId() );
        Document document = request( params, reqJoinGame );
        String money = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "info" )
                               .getTextContent();
        String msg = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "msg" ).getTextContent();
        return StringUtils.isNotBlank( msg ) ? BigDecimal.ZERO : new BigDecimal( money ).setScale( 2, RoundingMode.HALF_UP );
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String params = String.format( "cagent=%s/\\\\\\\\/cur=CNY/\\\\\\\\/actype=1/\\\\\\\\/method=qos/\\\\\\\\/billno=%s",
                reqJoinGame.getAgent(), reqJoinGame.getOrderId() );
        Document document = request( params, reqJoinGame );
        String status = document.getElementsByTagName( "result" ).item( 0 ).getAttributes().getNamedItem( "info" )
                                .getTextContent();
        return "0".equals( status );
    }
}
