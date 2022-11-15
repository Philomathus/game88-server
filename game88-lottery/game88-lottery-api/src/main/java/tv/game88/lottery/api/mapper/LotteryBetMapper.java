package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.lottery.api.entity.LotteryBet;

import java.util.List;

/**
 * 彩票会员下注详情Mapper接口
 *
 * @author mengJun
 */
public interface LotteryBetMapper extends BaseMapper<LotteryBet> {

    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情集合
     */
    public List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet );
}