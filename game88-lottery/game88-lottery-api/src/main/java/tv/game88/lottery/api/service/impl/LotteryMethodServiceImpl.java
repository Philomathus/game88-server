package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.lottery.api.entity.LotteryMethod;
import tv.game88.lottery.api.mapper.LotteryMethodMapper;
import tv.game88.lottery.api.service.LotteryMethodService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 彩票种类Service业务层处理
 *
 * @author mengJun
 * @date 2022-12-21
 */
@Service
public class LotteryMethodServiceImpl extends ServiceImpl<LotteryMethodMapper,LotteryMethod> implements LotteryMethodService {

    @Resource
    private LotteryMethodMapper lotteryMethodMapper;

    /**
     * 查询彩票种类列表
     *
     * @param lotteryMethod 彩票种类
     * @return 彩票种类
     */
    @Override
    public List<LotteryMethod> selectLotteryMethodList( LotteryMethod lotteryMethod ) {
        return lotteryMethodMapper.selectLotteryMethodList( lotteryMethod );
    }
}
