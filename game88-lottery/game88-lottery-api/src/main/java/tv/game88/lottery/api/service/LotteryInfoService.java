package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.entity.LotteryInfo;

import java.util.List;

/**
 * 彩票信息Service接口
 *
 * @author mengJun
 */
public interface LotteryInfoService extends IService<LotteryInfo> {
    /**
     * 查询彩票信息列表
     *
     * @param lotteryInfo 彩票信息
     *
     * @return 彩票信息集合
     */
    public List<LotteryInfo> selectLotteryInfoList( LotteryInfo lotteryInfo );
}