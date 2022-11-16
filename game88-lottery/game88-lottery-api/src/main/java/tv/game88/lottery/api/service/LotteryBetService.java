package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.entity.LotteryBet;

import java.util.List;

/**
 * 彩票会员下注详情Service接口
 *
 * @author mengJun
 */
public interface LotteryBetService extends IService<LotteryBet> {
    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情集合
     */
    public List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet );
}