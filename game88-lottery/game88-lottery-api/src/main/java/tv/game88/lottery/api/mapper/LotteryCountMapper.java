package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.lottery.api.dto.BetCount;
import tv.game88.lottery.api.entity.LotteryCount;

import java.util.List;

/**
 * 彩票会员下注行为Mapper接口
 *
 * @author mengJun
 */
public interface LotteryCountMapper extends BaseMapper<LotteryCount> {
    List<BetCount> selectCountTotal( @Param( "lotteryId" ) Integer lotteryId, @Param( "issue" ) String issue, @Param( "memberId"
    ) String memberId );

    List<BetCount> countBet( @Param( "issue" ) String issue, @Param( "lotteryId" ) Integer lotteryId );
}