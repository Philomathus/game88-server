package tv.game88.platform.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.platform.api.dto.RspPlamGamesMonth;
import tv.game88.platform.api.entity.ReportPlamGames;

import java.util.List;

public interface ReportPlamGamesMapper {

    public List<ReportPlamGames> selectReportPlamGamesList( ReportPlamGames reportPlamGames );

    String calldataProrepPlamcom( @Param( "timedateta" ) String timedateta );

    ReportPlamGames countBetData( ReportPlamGames reportPlamGames );

    List<RspPlamGamesMonth> selectReportPlamGamesListMonth( ReportPlamGames reportPlamGames );

    RspPlamGamesMonth countBetMonth( ReportPlamGames reportPlamGames );
}