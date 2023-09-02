package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.general.api.entity.ReportPlamGameschilds;
import tv.game88.general.api.mapper.ReportPlamGameschildsMapper;
import tv.game88.general.api.service.IReportPlamGameschildsService;

import java.util.List;

/**
 * 游戏投注报表子表Service业务层处理
 *
 * @author 77tv
 * @date 2021-02-20
 */
@Service
public class ReportPlamGameschildsServiceImpl extends ServiceImpl<ReportPlamGameschildsMapper, ReportPlamGameschilds> implements IReportPlamGameschildsService {

    /**
     * 查询游戏投注报表子表列表
     *
     * @param reportPlamGameschilds 游戏投注报表子表
     *
     * @return 游戏投注报表子表
     */
    @Override
    public List<ReportPlamGameschilds> selectReportPlamGameschildsList( ReportPlamGameschilds reportPlamGameschilds ) {

        DynamicDataSourceContextHolder.push( "slave_" + reportPlamGameschilds.getAgentPlatform() );

        List<ReportPlamGameschilds> allList = this.baseMapper.selectReportPlamGameschildsList( reportPlamGameschilds );

        DynamicDataSourceContextHolder.poll();
        return allList;
    }
}
