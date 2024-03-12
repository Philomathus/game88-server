package tv.game88.platform.api.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ReportPlamGameschilds;
import tv.game88.platform.api.mapper.ReportPlamGameschildsMapper;
import tv.game88.platform.api.service.ReportPlamGameschildsService;

import java.util.List;

/**
 * 游戏投注报表子表Service业务层处理
 *
 * @author 77tv
 * @date 2021-02-20
 */
@Service
public class ReportPlamGameschildsServiceImpl implements ReportPlamGameschildsService {
    @Resource
    private ReportPlamGameschildsMapper reportPlamGameschildsMapper;

    /**
     * 查询游戏投注报表子表列表
     *
     * @param reportPlamGameschilds 游戏投注报表子表
     * @return 游戏投注报表子表
     */
    @Override
    public List<ReportPlamGameschilds> selectReportPlamGameschildsList( ReportPlamGameschilds reportPlamGameschilds ) {
        return reportPlamGameschildsMapper.selectReportPlamGameschildsList( reportPlamGameschilds );
    }

    @Override
    public String getPlatformId( ReportPlamGameschilds reportPlamGamesChilds ) {
        return reportPlamGameschildsMapper.getPlatformIdByGameUuid( reportPlamGamesChilds.getGameUuid() );
    }

    @Override
    public List<ReportPlamGameschilds> selectByBettorsCount( ReportPlamGameschilds reportPlamGamesChilds ) {
        return reportPlamGameschildsMapper.selectByBettorsCounts( reportPlamGamesChilds );
    }
}