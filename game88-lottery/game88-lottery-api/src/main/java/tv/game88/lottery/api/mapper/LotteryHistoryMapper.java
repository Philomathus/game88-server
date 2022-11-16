package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.lottery.api.dto.HistoryResult;
import tv.game88.lottery.api.entity.LotteryHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 彩票开奖历史Mapper接口
 *
 * @author mengJun
 */
public interface LotteryHistoryMapper extends BaseMapper<LotteryHistory> {
    List<String> selectIssueWaite( @Param( "time" ) LocalDateTime time, @Param( "lotteryId" ) Integer lotteryId );

    Integer updateAlreadyPrize( @Param( "id" ) String historyId, @Param( "status" ) Integer status, @Param( "ctl" ) Integer ctl
            , @Param( "totalBet" ) Integer totalBet, @Param( "code" ) String code, @Param( "totalPrize" ) BigDecimal totalPrize
            , @Param( "killRate" ) BigDecimal killRate, @Param( "analyse" ) String analyse );

    List<HistoryResult> selectHistoryResult( @Param( "lotteryId" ) Integer lotteryId, @Param( "status" ) Integer status );
}