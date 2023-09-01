package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.dto.RspPlamGamesMonth;
import tv.game88.general.api.entity.ReportPlamGames;

import java.util.List;

public interface ReportPlamGamesMapper extends BaseMapper<ReportPlamGames> {

	public List<ReportPlamGames> selectReportPlamGamesList( ReportPlamGames reportPlamGames);

	String calldataProrepPlamcom(@Param( "timedateta" ) String timedateta, @Param( "agentPlatform" ) String agentPlatform);

    ReportPlamGames countBetData(ReportPlamGames reportPlamGames);

    List<RspPlamGamesMonth> selectReportPlamGamesListMonth( ReportPlamGames reportPlamGames);

}