package tv.game88.core.member.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.member.cache.ConfigRecommendCacheUtils;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.entity.MemberRecommend;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.mapper.MemberRecommendMapper;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MemberRecommendManager {
    @Resource
    private MemberInfoMapper          memberInfoMapper;
    @Resource
    private ConfigRecommendCacheUtils configRecommendCacheUtils;
    @Resource
    private MemberRecommendMapper     memberRecommendMapper;

    public void recommendProcess( MemberInfo memberInfo, BigDecimal rechargeMoney ) {
        if ( StringUtils.isNotBlank( memberInfo.getInviterCode() ) ) {
            Map<Integer, ConfigRecommend> billMap = configRecommendCacheUtils.getBillMap();

            MemberInfo rd1 = memberInfoMapper.findRecommendByInviterCode( memberInfo.getInviterCode() );
            MemberInfo rd2 = null;
            if ( rd1 != null ) {//一级分佣
                saveRecommend( rechargeMoney, billMap, 1, memberInfo, rd1 );
                if ( StringUtils.isNotBlank( rd1.getInviterCode() ) ) {
                    rd2 = memberInfoMapper.findRecommendByInviterCode( rd1.getInviterCode() );
                }
            }
            if ( rd2 != null ) {//二级分佣
                saveRecommend( rechargeMoney, billMap, 2, memberInfo, rd2 );
            }
        }
    }

    private void saveRecommend( BigDecimal rechargeMoney, Map<Integer, ConfigRecommend> billMap, int key, MemberInfo memberInfo
            , MemberInfo rd ) {
        MemberRecommend memberRecommend = new MemberRecommend();
        BigDecimal      commission      = rechargeMoney.multiply( billMap.get( key ).getBill() );
        memberRecommend.setId( IdWorker.get32UUID() );
        memberRecommend.setCreateTime( LocalDateTime.now() );
        memberRecommend.setMemberId( memberInfo.getId() );
        memberRecommend.setCommission( commission );
        memberRecommend.setStatus( 0 );
        memberRecommend.setCode( memberInfo.getId() );
        memberRecommend.setOrderMoney( rechargeMoney );
        memberRecommend.setLevel( key );
        memberRecommend.setInviterId( rd.getId() );
        memberRecommendMapper.insert( memberRecommend );
    }
}