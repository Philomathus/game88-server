package tv.game88.core.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;
import tv.game88.core.lottery.dto.RspBetRecord;
import tv.game88.core.lottery.entity.LotteryBet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 彩票会员下注详情Mapper接口
 *
 * @author mengJun
 */
public interface LotteryBetMapper extends BaseMapper<LotteryBet> {

    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情集合
     */
    List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet );

    List<LotteryBet> selectLotteryBetSingleList( LotteryBet lotteryBet );

    List<LotteryBet> selectLotteryBetViewlList( LotteryBet lotteryBet );

    public List<LotteryBet> selectListByTime( @Param( "start" ) String start, @Param( "end" ) String end );

    List<RspBetRecord> getBetRecordList( @Param( "dbNodes" ) String dbNodes, @Param( "lotteryId" ) Integer lotteryId, @Param(
            "memberId" ) String memberId );

    int insertLotteryBet( @Param( "req" ) LotteryBet db, @Param( "dbNodes" ) String idLatest );

    List<LotteryBet> selectLotteryWaiteList( @Param( "issue" ) String issue, @Param( "lotteryId" ) Integer lotteryId );

    Integer selectLotteryWaiteCount( @Param( "issue" ) String issue, @Param( "lotteryId" ) Integer lotteryId );

    void updateStatusPrize( @Param( "id" ) String id, @Param( "dbNodes" ) String dbNodes, @Param( "prize" ) BigDecimal prize,
                            @Param( "code" ) String code, @Param( "updateTime" ) LocalDateTime updateTime,
                            @Param( "status" ) Integer status );

    @Select( { "{call procherck_quzhi_import(#{lottery_id,mode=IN},#{kj_result,mode=OUT,jdbcType=VARCHAR})}" } )
    @Options( statementType = StatementType.CALLABLE )
    String procherckQuzhiImport( @Param( "lottery_id" ) Integer lotteryId, @Param( "kj_result" ) String kj_result );
}