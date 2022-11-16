package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.lottery.api.entity.LotteryRule;
import tv.game88.lottery.api.mapper.LotteryRuleMapper;
import tv.game88.lottery.api.service.LotteryRuleService;

import java.util.List;

/**
 * 彩票规则说明Service业务层处理
 *
 * @author mengJun
 */
@Service
public class LotteryRuleServiceImpl extends ServiceImpl<LotteryRuleMapper, LotteryRule> implements LotteryRuleService {
    /**
     * 查询彩票规则说明列表
     *
     * @param lotteryRule 彩票规则说明
     *
     * @return 彩票规则说明
     */
    @Override
    public List<LotteryRule> selectLotteryRuleList( LotteryRule lotteryRule ) {
        return this.baseMapper.selectLotteryRuleList( lotteryRule );
    }
}