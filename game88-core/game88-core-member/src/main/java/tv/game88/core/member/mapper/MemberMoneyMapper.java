package tv.game88.core.member.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.member.entity.MemberMoney;

import java.math.BigDecimal;
import java.util.List;

/**
 * 派送彩金暂存表Mapper接口
 *
 * @author Rajesh
 * @date 2022-12-23
 */
public interface MemberMoneyMapper extends BaseMapper<MemberMoney> {

    /**
     * 查询派送彩金暂存表列表
     *
     * @param memberMoney 派送彩金暂存表
     *
     * @return 派送彩金暂存表集合
     */
    List<MemberMoney> selectMemberMoneyList( MemberMoney memberMoney );

    /**
     * 行为类型统计 count money mapper
     */
    BigDecimal countMoney();

    /**
     * 查询派送彩金暂存表列表 remove all data mapper
     */
    Integer handleClean();

    int insertBatch( List<MemberMoney> memberMoneyList );
}
