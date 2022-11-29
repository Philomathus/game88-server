package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.cache.ConfigVipCacheUtils;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.platform.api.service.MemberBcodeService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 会员打码数据Service业务层处理
 *
 * @author mengJun
 */
@Service
@Log4j2
public class MemberBcodeServiceImpl extends ServiceImpl<MemberBcodeMapper, MemberBcode> implements MemberBcodeService {
    @Resource
    private MemberInfoMapper    memberInfoMapper;
    @Resource
    private MemberMoneyManager  memberMoneyManager;
    @Resource
    private ConfigVipCacheUtils configVipCacheUtils;

    /**
     * 查询会员打码数据列表
     *
     * @param memberBcode 会员打码数据
     *
     * @return 会员打码数据
     */
    @Override
    public List<MemberBcode> selectMemberBcodeList( MemberBcode memberBcode ) {
        if ( memberBcode.getSelectDate() != null ) {
            memberBcode.setStartTime( memberBcode.getSelectDate()[ 0 ] + " 00:00:00" );
            memberBcode.setEndTime( memberBcode.getSelectDate()[ 1 ] + " 23:59:59" );
        }
        return this.baseMapper.selectMemberBcodeList( memberBcode );
    }

    /**
     * 统计
     */
    @Override
    public RspBase<MemberBcode> getTotalData( MemberBcode memberBcode ) {
        if ( memberBcode.getSelectDate() != null ) {
            memberBcode.setStartTime( memberBcode.getSelectDate()[ 0 ] + " 00:00:00" );
            memberBcode.setEndTime( memberBcode.getSelectDate()[ 1 ] + " 23:59:59" );
        }
        MemberBcode memberBcode1 = this.baseMapper.getTotalData( memberBcode );
        if ( Objects.isNull( memberBcode1 ) ) {
            MemberBcode memberBcode2 = new MemberBcode();
            memberBcode2.setCountCur( BigDecimal.ZERO );
            memberBcode2.setTotal( BigDecimal.ZERO );
            return RspBase.ok( memberBcode2 );
        }
        return RspBase.ok( memberBcode1 );
    }

    @Override
    public int updateMemberBcode( MemberBcode memberBcode ) {
        BigDecimal add = memberBcode.getCur();
        if ( add.compareTo( BigDecimal.ZERO ) < 0 ) {
            add = BigDecimal.ZERO;
        }
        MemberBcode db = this.baseMapper.selectById( memberBcode.getId() );
        if ( add.compareTo( db.getIncome() ) > 0 ) {
            add = db.getIncome();
            memberBcode.setCur( add );
        }
        if ( add.compareTo( db.getIncome() ) < 0 ) {
            memberBcode.setStatus( 0 );
        } else {
            memberBcode.setStatus( 1 );
        }
        int c = this.baseMapper.updateById( memberBcode );
        if ( c > 0 ) {
            BigDecimal addCode = add.subtract( db.getCur() );
            memberInfoMapper.updateBeatCode( db.getUserId(), addCode, addCode );

            List<ConfigVip> configVips = configVipCacheUtils
                    .getConfigVipMap()
                    .values()
                    .stream()
                    .sorted( Comparator.comparing( ConfigVip::getBcode ) )
                    .toList();
            memberMoneyManager.checkAndUpdateVip( db.getUserId(), configVips );
        }
        return c;
    }
}
