package tv.game88.game.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.game.api.service.GameDataService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.time.LocalDateTime;

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
        gameDataService.beatGameCodeAgent( begin, begin, end, null, EnumGameCategory.KAIXUAN );
    }

    @Test
    public void beatLotteryCode() {
        LocalDateTime endDay  = LocalDateTime.now();
        LocalDateTime starDay = endDay.minusMonths( 2 );
        String        begin   = LocalDateTimeUtils.format( starDay );
        String        end     = LocalDateTimeUtils.format( endDay );
        gameDataService.beatLotteryCode( begin, end );
    }
}
