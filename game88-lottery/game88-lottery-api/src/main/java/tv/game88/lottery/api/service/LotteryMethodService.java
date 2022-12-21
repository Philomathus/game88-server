package tv.game88.lottery.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.entity.LotteryMethod;

import java.util.List;

/**
 * 彩票种类Service接口
 *
 * @author mengJun
 * @date 2022-12-21
 */
public interface LotteryMethodService extends IService<LotteryMethod> {

    /**
     * 查询彩票种类列表
     *
     * @param lotteryMethod 彩票种类
     * @return 彩票种类集合
     */
    public List<LotteryMethod> selectLotteryMethodList( LotteryMethod lotteryMethod );
}
