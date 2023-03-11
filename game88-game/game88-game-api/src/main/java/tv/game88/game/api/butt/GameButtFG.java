package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.*;

@Log4j2
@Repository( value = ConstantsGame.FG + "GameProcessor" )
public class GameButtFG extends AbstractGameButt {

    private enum TransactionType {
        //
        TRANSFER,
        WITHDRAW
    }

    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {
        if ( redisUtils.sIsMember( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() ) ) {
            return;
        }

        String              url       = reqJoinGame.getApiUrl() + "/v3/player_names/" + reqJoinGame.getGameMemberId();
        Map<String, Object> resultMap = execute( url, Map.of(), reqJoinGame );
        if ( isValid( resultMap ) ) {
            redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
        } else {
            url = reqJoinGame.getApiUrl() + "/v3/players";

            Map<String, Object> params = new HashMap<>();
            params.put( "member_code", reqJoinGame.getGameMemberId() );
            params.put( "password", reqJoinGame.getGameMemberId() + "!@#$1234" );

            resultMap = execute( url, params, reqJoinGame );

            if ( isValid( resultMap ) ) {
                redisUtils.sAdd( Constants.GAME_USERS_PREX + reqJoinGame.getPlatformId(), reqJoinGame.getGameMemberId() );
                return;
            }
            log.error( reqJoinGame.getGameCategory().getDes() + " 创建玩家失败 ->{}", JsonUtil.object2Json( resultMap ) );
            throw new BusinessException( reqJoinGame.getGameCategory().getDes() + " - 创建玩家失败" );
        }
    }


    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + "/v3/launch_game";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put( "member_code", reqJoinGame.getGameMemberId() );
        params.put( "game_code", reqJoinGame.getKindId() );
        params.put( "game_type", "h5" );
        params.put( "language", "zh-cn" );
        params.put( "ip", reqJoinGame.getIp() );
        params.put( "return_url", "" );

        Map<String, Object> resultMap = execute( url, params, reqJoinGame );

        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            reqJoinGame.setGameUrl( dataMap.getOrDefault( "game_url", "" ).toString() );
        }

        if ( StringUtils.isBlank( reqJoinGame.getGameUrl() ) ) {
            log.error( reqJoinGame.getGameCategory().getDes()
                    + "获取游戏链接失败:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
            throw new BusinessException( "获取游戏链接失败" );
        }
    }

    @Override
    public void transferMoney( ReqJoinGame reqJoinGame ) {
        TransferOrWithrdawMoney( reqJoinGame, TransactionType.TRANSFER );
    }

    @Override
    public void withdrawal( ReqJoinGame reqJoinGame ) {
        TransferOrWithrdawMoney( reqJoinGame, TransactionType.WITHDRAW );
    }

    private void TransferOrWithrdawMoney( ReqJoinGame reqJoinGame, TransactionType type ) {
        String URL = reqJoinGame.getApiUrl() + "/v3/player_uchips/member_code/" + reqJoinGame.getGameMemberId();

        Map<String, Object> params = new LinkedHashMap<>();
        BigDecimal transferMoney = switch ( type ) {
            case TRANSFER -> reqJoinGame.getTransferMoney();
            case WITHDRAW -> reqJoinGame.getTransferMoney().negate();
        };
        params.put( "amount", transferMoney.multiply( new BigDecimal( 100 ) ).intValue() );
        params.put( "externaltransactionid", reqJoinGame.getOrderId() );

        Map<String, Object> resultMap = null;
        try {
            resultMap = execute( URL, params, reqJoinGame );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new GameTransferException( e.getMessage() );
        }

        String action = type == TransactionType.TRANSFER ? "上" : "下";
        log.info( reqJoinGame.getGameCategory().getDes() + action
                + "分信息:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( isValid( resultMap ) ) {
            return;
        }
        throw new GameTransferException(
                reqJoinGame.getGameCategory().getDes() + action + "分异常 - " + action + "分失败或数据为空" );
    }

    @Override
    public BigDecimal queryBalance( ReqJoinGame reqJoinGame ) {
        String url = reqJoinGame.getApiUrl() + "/v3/player_chips/member_code/" + reqJoinGame.getGameMemberId();

        Map<String, Object> resultMap = execute( url, Map.of(), reqJoinGame );

        if ( isValid( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            return new BigDecimal( dataMap.getOrDefault( "balance", "0" ).toString() );
        }

        log.error( reqJoinGame.getGameCategory().getDes()
                + "查询余额失败userId：{},rep:{}", reqJoinGame.getGameMemberId(), JsonUtil.object2Json( resultMap ) );
        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer( ReqJoinGame reqJoinGame ) {
        String URL = reqJoinGame.getApiUrl() + "/v3/player_uchips_check/" + reqJoinGame.getOrderId();

        Map<String, Object> resultMap = execute( URL, Map.of(), reqJoinGame );

        log.info( reqJoinGame.getGameCategory().getDes()
                + "查询转账:{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", -1 ).toString() );
            if ( code == 0 ) {
                return true;
            } else if ( code == 208 ) {
                try {
                    Thread.sleep( 4000L );
                } catch ( InterruptedException e ) {
                    throw new RuntimeException( e );
                }
                throw new RuntimeException( "查询订单正在处理,需要重试" );
            } else {
                return false;
            }
        }
        throw new RuntimeException( "查询结果为空,需要重试" );
    }

    private boolean isValid( Map<String, Object> resultMap ) {
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            return "0".equals( resultMap.getOrDefault( "code", "" ) );
        }
        return false;
    }

    private Map<String, Object> execute( String url, Map<String, Object> params, ReqJoinGame reqJoinGame ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.setAccept( List.of( MediaType.APPLICATION_JSON ) );
        httpHeaders.set( "merchantname", reqJoinGame.getDes() );
        httpHeaders.set( "merchantcode", reqJoinGame.getMd5() );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>( params, httpHeaders );

        return restTemplate.execute( url, HttpMethod.POST, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
    }
}
