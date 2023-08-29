package tv.game88.game.api.service;

import tv.game88.game.api.entity.MemberGameData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GameDataService {
    void beatGameCodeAgent( String dTime, String start, String end, String account, Long id );

    void beatLotteryCode( String begin, String end );

    void doBeatCode( Map<String, BigDecimal> willCodeMap );

    void deQuestCheck( final List<MemberGameData> list );
}
