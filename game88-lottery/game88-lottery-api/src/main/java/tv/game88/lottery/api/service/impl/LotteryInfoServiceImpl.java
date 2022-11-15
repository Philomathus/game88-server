package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.lottery.api.entity.LotteryInfo;
import tv.game88.lottery.api.mapper.LotteryInfoMapper;
import tv.game88.lottery.api.service.LotteryInfoService;

import java.util.List;

/**
 * 彩票信息Service业务层处理
 *
 * @author mengJun
 */
@Service
public class LotteryInfoServiceImpl extends ServiceImpl<LotteryInfoMapper, LotteryInfo> implements LotteryInfoService {
    /**
     * 查询彩票信息列表
     *
     * @param lotteryInfo 彩票信息
     *
     * @return 彩票信息
     */
    @Override
    public List<LotteryInfo> selectLotteryInfoList( LotteryInfo lotteryInfo ) {
        return this.baseMapper.selectLotteryInfoList( lotteryInfo );
    }
}