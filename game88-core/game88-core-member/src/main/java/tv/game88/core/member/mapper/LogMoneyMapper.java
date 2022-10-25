package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.member.dto.ReqLogMoney;
import tv.game88.core.member.dto.RspLogMoney;
import tv.game88.core.member.entity.LogMoney;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 资金日志Mapper接口
 *
 * @author 77lm
 * @date 2021-10-09
 */
public interface LogMoneyMapper extends BaseMapper<LogMoney> {

    /**
     * 查询资金日志列表
     *
     * @param logMoney 资金日志
     *
     * @return 资金日志集合
     */
    List<LogMoney> selectLogMoneyList( LogMoney logMoney );

    List<LogMoney> selectLogMoneyFirstList( LogMoney logMoney );

    List<LogMoney> selectLogMoneySingleList( LogMoney logMoney );

    List<LogMoney> findMark( @Param( "userId" ) String userId, @Param( "mark" ) String mark, @Param( "money" ) BigDecimal money
            , @Param( "pay" ) BigDecimal pay, @Param( "dbNodes" ) String dbNodes );

    /**
     * 插入数据
     */
    int insert( @Param( "req" ) LogMoney record, @Param( "dbNodes" ) String dbNodes );

    /**
     * 插入数据
     */
    int insertDefault( @Param( "req" ) LogMoney record );

    List<RspLogMoney> findLogMoneyList( @Param( "userId" ) String userId, @Param( "dbNodes" ) String dbNodes,
                                        @Param( "req" ) ReqLogMoney req, @Param( "beginDay" ) String beginDay,
                                        @Param( "endDay" ) String endDay );

    LogMoney selectById( @Param( "id" ) String id, @Param( "dbNodes" ) String dbNodes );

    int updateByIdSelective( @Param( "req" ) LogMoney record, @Param( "dbNodes" ) String dbNodes );

    Map totalCount( LogMoney logMoney );

    Map listCount( LogMoney logMoney );

    Integer findExist( @Param( "dbNodes" ) String dbNodes, @Param( "keyId" ) String id );
}
