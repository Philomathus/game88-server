package tv.game88.game.admin;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.game.api.service.GameDataService;
import tv.game88.core.game.type.EnumGameCategory;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class beatTest {
    @Resource
    private GameDataService gameDataService;

    @Test
    public void beatGameCodeAgent() {
        LocalDateTime endDay  = LocalDateTime.now();
        LocalDateTime starDay = endDay.minusHours( 4 );
        String        begin   = LocalDateTimeUtils.format( starDay );
        String        end     = LocalDateTimeUtils.format( endDay );
        gameDataService.beatGameCodeAgent( begin, begin, end, null, EnumGameCategory.KAIXUAN_X );
    }

    @Test
    public void beatLotteryCode() {
        LocalDateTime endDay  = LocalDateTime.now();
        LocalDateTime starDay = endDay.minusMonths( 2 );
        String        begin   = LocalDateTimeUtils.format( starDay );
        String        end     = LocalDateTimeUtils.format( endDay );
        gameDataService.beatLotteryCode( begin, end );
    }

    @Resource
    private RestTemplate restTemplate;

    @Test
    public void createBGAgentAccount() {
        String method = "open.agent.create";
        String id     = IdWorker.get32UUID();
        String sn     = "am00";

        Map<String, Object> params = new HashMap<>();
        params.put( "random", id );
        params.put( "sn", sn );
        params.put( "loginId", "88lm" );
        params.put( "password", "Asdf1234" );
        params.put( "fromIp", "18.176.18.122" );
        params.put( "sign", DigestUtils.md5Hex(
                id + sn + params.get( "loginId" ).toString() + "8153503006031672EF300005E5EF6AEF" ) );

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put( "id", id );
        requestMap.put( "method", method );
        requestMap.put( "jsonrpc", "2.0" );
        requestMap.put( "params", params );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        String resultStr = restTemplate.postForObject(
                "http://zd.mkecy.com/game-bg" + "/" + method, httpEntity, String.class );
        System.out.println( resultStr );
    }
}
