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
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.exception.GameTransferException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.WALI + "GameProcessor" )
@SuppressWarnings( "unchecked" )
public class GameButtWali extends AbstractGameButt {

    /*
    * apiUrl = reqJoinGame.getApiUrl
    * ${apiUrl}/${action}?a=${apiAccount}&t=${unixTime}&p=${params}&k=${sign}
    *
    * Assumptions:
    * aesKey = reqJoinGame.getDes()
    * signKey = reqJoinGame.getMd5()
    *
    * agentId = reqJoinGame.getAgent()
    * userId = reqJoinGame.getGameMemberId()
    *
    * */

    @Override
    public void getToken(ReqJoinGame reqJoinGame) {

    }

    @Override
    public void createAccount(ReqJoinGame reqJoinGame) {

    }

    @Override
    public void getJoinGameUrl(ReqJoinGame reqJoinGame) {

    }

    @Override
    public void transferMoney(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, "上分" );
    }

    @Override
    public void withdrawal(ReqJoinGame reqJoinGame) {
        transact( reqJoinGame, "下分" );
    }

    @Override
    public BigDecimal queryBalance(ReqJoinGame reqJoinGame) {

        return BigDecimal.ZERO;
    }

    @Override
    public boolean queryTransfer(ReqJoinGame reqJoinGame) {
        return false;
    }

    private void transact(ReqJoinGame reqJoinGame, String type) {
        String time = String.valueOf( System.currentTimeMillis() );
        String agentId = reqJoinGame.getAgent();
        String userId = reqJoinGame.getGameMemberId();
        String credit = String.valueOf( reqJoinGame.getTransferMoney() );

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.set( "orderId",  String.format( "%s_%s_%s", agentId, time, userId ) );
        paramMap.set( "uid", userId );
        paramMap.set( "credit", credit );

        Map<String, Object> resultMap = executeGetRequest( "transferV3", reqJoinGame, paramMap );

        if( !CollectionUtils.isEmpty( resultMap ) ) {
            int status = Integer.parseInt( String.valueOf( resultMap.getOrDefault( "status", "-1" ) ) );

            if( status == 0 || status == 1 ) {
                return;
            }
        }

        log.info( reqJoinGame.getGameCategory().getDes()
                + type + ":{}; userId:{}", JsonUtil.object2Json( resultMap ), reqJoinGame.getGameMemberId() );

        throw new GameTransferException( reqJoinGame.getGameCategory().getDes() + "上分异常 - 上分失败或数据为空" );
    }

    private Map<String, Object> executeGetRequest(String action, ReqJoinGame reqJoinGame, MultiValueMap<String, String> paramMap) {
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( generateRequestUrl( reqJoinGame, action, paramMap ) )
                .build();

        Map<String, Object> resultMap = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
            restTemplate.httpEntityCallback( null ),
            response -> {
                InputStream bodyStream = response.getBody();
                String      text;
                try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                    text = IOUtils.toString( reader );
                }
                return JsonUtil.json2Map( text );
            }
        );

        return resultMap;
    }

    private static String assembleUrlParameters(Map<String, ?> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        return sb.substring(0, sb.length() - 1);
    }

    private static String generateRequestUrl(ReqJoinGame reqJoinGame, String action, Map<String, ?> paramMap) {
        String time = String.valueOf( System.currentTimeMillis() / 1000 );
        String apiUrl = reqJoinGame.getApiUrl();
        String apiAccount = reqJoinGame.getApiAccount();
        String aesKey = reqJoinGame.getDes();
        String params;
        try {
            params = AESCoder.encryptByKey( assembleUrlParameters( paramMap ), aesKey);
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            throw new BusinessException( e.getMessage() );
        }

        String sign = DigestUtils.md5Hex( params + time + reqJoinGame.getMd5() );
        params = URLEncoder.encode( params, StandardCharsets.UTF_8 );

        return String.format(
                "%s/%s?a=%s&t=%s&p=%s&k=%s",
                apiUrl,
                action,
                apiAccount,
                time,
                params,
                sign
        );
    }
}
