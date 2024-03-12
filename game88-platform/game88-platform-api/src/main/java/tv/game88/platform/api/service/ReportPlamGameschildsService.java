package tv.game88.platform.api.service;

import tv.game88.platform.api.entity.ReportPlamGameschilds;

import java.util.List;

/**
 * 游戏投注报表子表Service接口
 *
 * @author 77tv
 * @date 2021-02-20
 */
public interface ReportPlamGameschildsService {
	/**
	 * 查询游戏投注报表子表列表
	 *
	 * @param reportPlamGameschilds 游戏投注报表子表
	 * @return 游戏投注报表子表集合
	 */
	List<ReportPlamGameschilds> selectReportPlamGameschildsList(ReportPlamGameschilds reportPlamGameschilds);
	String getPlatformId( ReportPlamGameschilds reportPlamGameschilds );
	List<ReportPlamGameschilds> selectByBettorsCount( ReportPlamGameschilds reportPlamGameschilds );
}
