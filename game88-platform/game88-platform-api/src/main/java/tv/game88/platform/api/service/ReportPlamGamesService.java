package tv.game88.platform.api.service;

import tv.game88.platform.api.dto.RspPlamGamesMonth;
import tv.game88.platform.api.entity.ReportPlamGames;

import java.text.ParseException;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ReportPlamGamesService {

	/**
	 * 查询【请填写功能名称】列表
	 *
	 * @param reportPlamGames 【请填写功能名称】
	 * @return 【请填写功能名称】集合
	 */
	List<ReportPlamGames> selectReportPlamGamesList( ReportPlamGames reportPlamGames );

	ReportPlamGames countBetData(ReportPlamGames reportPlamGames);

	List<ReportPlamGames> exportPlamGamesList(ReportPlamGames reportPlamGames);

    List<RspPlamGamesMonth> selectReportPlamGamesListMonth( ReportPlamGames reportPlamGames) throws ParseException;

	RspPlamGamesMonth countBet(ReportPlamGames reportPlamGames) throws ParseException;
}