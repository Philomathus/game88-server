package tv.game88.game.api.butt;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import tv.game88.common.utils.JsonUtil;
import tv.game88.game.api.base.AbstractGameButt;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.ReqJoinGame;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.OG + "GameProcessor" )
public class GameButtOG extends AbstractGameButt {
    @Override
    public void getToken( ReqJoinGame reqJoinGame ) {

        HttpHeaders headers = new HttpHeaders();
        headers.set( "X-Operator", reqJoinGame.getDes() );
        headers.set( "x-key", reqJoinGame.getMd5() );
        headers.setContentType( MediaType.APPLICATION_FORM_URLENCODED );
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>( headers );

        Map<String, Object> resultMap = restTemplate.execute( reqJoinGame.getApiUrl()
                + "/token", HttpMethod.GET, restTemplate.httpEntityCallback( requestEntity ), response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return JsonUtil.json2Map( text );
        } );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {

        }
    }

    @Override
    public void createAccount( ReqJoinGame reqJoinGame ) {

    }

    @Override
    public void getJoinGameUrl( ReqJoinGame reqJoinGame ) {

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
