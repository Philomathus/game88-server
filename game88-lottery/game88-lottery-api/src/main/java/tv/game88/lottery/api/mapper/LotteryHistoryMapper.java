package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;
import tv.game88.lottery.api.dto.HistoryResult;
import tv.game88.lottery.api.entity.LotteryHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 彩票开奖历史Mapper接口
 *
 * @author mengJun
 */
public interface LotteryHistoryMapper extends BaseMapper<LotteryHistory> {
    List<String> selectIssueWaite( @Param( "time" ) LocalDateTime time, @Param( "lotteryId" ) Integer lotteryId );

    Integer updateAlreadyPrize( @Param( "id" ) String historyId, @Param( "status" ) Integer status, @Param( "ctl" ) Integer ctl
            , @Param( "totalBet" ) Long totalBet, @Param( "code" ) String code, @Param( "totalPrize" ) BigDecimal totalPrize,
                                @Param( "killRate" ) BigDecimal killRate, @Param( "analyse" ) String analyse );

    List<HistoryResult> selectHistoryResult( @Param( "lotteryId" ) Integer lotteryId, @Param( "status" ) Integer status );

    @Select( { "{call pro_killrate_com(#{lotteryId,mode=IN},#{betTotal,mode=IN},#{killRate,mode=IN},#{betCountinfo,mode=IN},"
            + "#{result,mode=OUT,jdbcType=VARCHAR})}" } )
    @Options( statementType = StatementType.CALLABLE )
    String countLotteryResult( @Param( "lotteryId" ) Integer lotteryId, @Param( "betTotal" ) Long betTotal,
                               @Param( "killRate" ) BigDecimal killRate, @Param( "betCountinfo" ) String betCountinfo, @Param(
                                       "result" ) Map<String, String> result );

    List<HistoryResult> selectResultWaite( @Param( "lotteryAgent" ) String lotteryAgent,
                                           @Param( "lotteryId" ) Integer lotteryId );
}