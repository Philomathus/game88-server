package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.ReportPlamGameschilds;

import java.util.List;

/**
 * 游戏投注报表子表Service接口
 *
 * @author 77tv
 * @date 2021-02-20
 */
public interface IReportPlamGameschildsService extends IService<ReportPlamGameschilds> {

    /**
     * 查询游戏投注报表子表列表
     *
     * @param reportPlamGameschilds 游戏投注报表子表
     *
     * @return 游戏投注报表子表集合
     */
    public List<ReportPlamGameschilds> selectReportPlamGameschildsList( ReportPlamGameschilds reportPlamGameschilds );

}
