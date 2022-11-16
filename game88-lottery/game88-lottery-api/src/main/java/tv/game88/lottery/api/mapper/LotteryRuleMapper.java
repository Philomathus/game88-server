package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.lottery.api.entity.LotteryRule;

import java.util.List;

/**
 * 彩票规则说明Mapper接口
 *
 * @author mengJun
 */
public interface LotteryRuleMapper extends BaseMapper<LotteryRule> {

    /**
     * 查询彩票规则说明列表
     *
     * @param lotteryRule 彩票规则说明
     *
     * @return 彩票规则说明集合
     */
    public List<LotteryRule> selectLotteryRuleList( LotteryRule lotteryRule );
}