package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.member.entity.MemberMoney;

import java.math.BigDecimal;
import java.util.List;

/**
 * 派送彩金暂存表Service接口
 *
 * @author Rajesh
 * @date 2022-12-23
 */
public interface MemberMoneyService extends IService<MemberMoney> {

    /**
     * 查询派送彩金暂存表列表 list all data
     *
     * @param memberMoney 派送彩金暂存表
     * @return 派送彩金暂存表集合
     */
    List<MemberMoney> selectAllMemberMoneyList( MemberMoney memberMoney );

    /**
     *  行为类型统计 count money service
     */
    BigDecimal countMoney();

    /**
     * 查询派送彩金暂存表列表 remove all data service
     */
    Integer handleClean();
}
