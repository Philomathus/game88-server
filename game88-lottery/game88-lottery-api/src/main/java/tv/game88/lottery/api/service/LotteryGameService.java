package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.entity.LotteryGame;

import java.util.List;

/**
 * 下注Service接口
 *
 * @author rajesh
 * @date 2022-12021
 */
public interface LotteryGameService extends IService<LotteryGame> {

    /**
     * 查询下注列表 list all data
     */
    List<LotteryGame> selectLotteryGameAll( LotteryGame lotteryGame );
}
