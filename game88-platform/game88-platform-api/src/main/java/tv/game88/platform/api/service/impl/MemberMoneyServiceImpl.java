package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.core.member.entity.MemberMoney;
import tv.game88.core.member.mapper.MemberMoneyMapper;
import tv.game88.platform.api.service.MemberMoneyService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 派送彩金暂存表Service业务层处理
 *
 * @author Rajesh
 * @date 2022-12-23
 */
@Log4j2
@Service
public class MemberMoneyServiceImpl extends ServiceImpl<MemberMoneyMapper, MemberMoney> implements MemberMoneyService {

    @Resource
    private MemberMoneyMapper memberMoneyMapper;

    /**
     * 查询派送彩金暂存表列表
     *
     * @param memberMoney 派送彩金暂存表
     *
     * @return 派送彩金暂存表
     */
    @Override
    public List<MemberMoney> selectAllMemberMoneyList( MemberMoney memberMoney ) {
        return memberMoneyMapper.selectMemberMoneyList(memberMoney);
    }

    /**
     *  行为类型统计 count money service impl
     */
    @Override
    public BigDecimal countMoney() {
        return memberMoneyMapper.countMoney();
    }

    /**
     * 查询派送彩金暂存表列表 remove all data service impl
     */
    @Override
    public Integer handleClean() {
        return memberMoneyMapper.handleClean();
    }


}
