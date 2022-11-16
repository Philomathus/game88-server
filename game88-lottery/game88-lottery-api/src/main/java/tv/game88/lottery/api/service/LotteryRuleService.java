package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.entity.LotteryRule;

import java.util.List;

/**
 * 彩票规则说明Service接口
 *
 * @author mengJun
 */
public interface LotteryRuleService extends IService<LotteryRule> {
    /**
     * 查询彩票规则说明列表
     *
     * @param lotteryRule 彩票规则说明
     *
     * @return 彩票规则说明集合
     */
    public List<LotteryRule> selectLotteryRuleList( LotteryRule lotteryRule );
}