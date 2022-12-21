package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.lottery.api.entity.LotteryGame;

import java.util.List;

/**
 * 彩票下注配置Mapper接口
 *
 * @author mengJun
 */
public interface LotteryGameMapper extends BaseMapper<LotteryGame> {

    /**
     * 查询下注列表
     *
     * @param lotteryGame 下注
     * @return 下注集合
     */
    List<LotteryGame> selectLotteryGameList(LotteryGame lotteryGame);
}