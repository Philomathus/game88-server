package tv.game88.platform.api.mapper;

import tv.game88.platform.api.entity.ReportPlamGameschilds;

import java.util.List;

/**
 * 游戏投注报表子表Mapper接口
 *
 * @author 77tv
 * @date 2021-02-20
 */
public interface ReportPlamGameschildsMapper {
    /**
     * 查询游戏投注报表子表列表
     *
     * @param reportPlamGameschilds 游戏投注报表子表
     *
     * @return 游戏投注报表子表集合
     */
    List<ReportPlamGameschilds> selectReportPlamGameschildsList( ReportPlamGameschilds reportPlamGameschilds );

    String getPlatformIdByGameUuid( String gameUuid );

    List<ReportPlamGameschilds> selectByBettorsCounts( ReportPlamGameschilds reportPlamGamesChilds );
}
