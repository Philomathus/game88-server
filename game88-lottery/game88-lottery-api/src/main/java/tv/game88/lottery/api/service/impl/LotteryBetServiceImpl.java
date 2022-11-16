package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.lottery.api.entity.LotteryBet;
import tv.game88.lottery.api.mapper.LotteryBetMapper;
import tv.game88.lottery.api.service.LotteryBetService;

import java.util.List;

/**
 * 彩票会员下注详情Service业务层处理
 *
 * @author mengJun
 */
@Service
public class LotteryBetServiceImpl extends ServiceImpl<LotteryBetMapper, LotteryBet> implements LotteryBetService {
    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情
     */
    @Override
    public List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet ) {
        return this.baseMapper.selectLotteryBetList( lotteryBet );
    }
}