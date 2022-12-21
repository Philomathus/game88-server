package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.lottery.api.entity.LotteryMethod;

import java.util.List;

/**
 * 彩票下注分类Mapper接口
 *
 * @author mengJun
 * @date 2022-12-21
 */
public interface LotteryMethodMapper extends BaseMapper<LotteryMethod> {

    /**
     * 查询彩票种类列表
     *
     * @param lotteryMethod 彩票种类
     * @return 彩票种类集合
     */
    List<LotteryMethod> selectLotteryMethodList(LotteryMethod lotteryMethod);
}