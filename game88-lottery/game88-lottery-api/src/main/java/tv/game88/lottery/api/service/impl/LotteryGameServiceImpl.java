package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.lottery.api.entity.LotteryGame;
import tv.game88.lottery.api.mapper.LotteryGameMapper;
import tv.game88.lottery.api.service.LotteryGameService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下注Service业务层处理
 *
 * @author mengJun
 * @date 2022-12-21
 */
@Service
public class LotteryGameServiceImpl extends ServiceImpl<LotteryGameMapper, LotteryGame> implements LotteryGameService {

    @Resource
    private LotteryGameMapper lotteryGameMapper;

    /**
     * 查询下注列表
     *
     * @param lotteryGame 下注
     * @return 下注
     */
    @Override
    public List<LotteryGame> selectLotteryGameAll( LotteryGame lotteryGame ) {
        return lotteryGameMapper.selectLotteryGameList( lotteryGame );
    }
}
