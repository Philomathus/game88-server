package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.cache.ConfigRecommendCacheUtils;
import tv.game88.core.member.dto.RspMemberRecommend;
import tv.game88.core.member.dto.RspMyRecommend;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.member.entity.MemberRecommend;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberRecommendMapper;
import tv.game88.platform.api.dto.RspDetailCommission;
import tv.game88.platform.api.entity.LogCommission;
import tv.game88.platform.api.mapper.LogCommissionMapper;
import tv.game88.platform.api.service.RecommendService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RecommendServiceImpl implements RecommendService {
    @Resource
    private MemberRecommendMapper     memberRecommendMapper;
    @Resource
    private LogCommissionMapper       logCommissionMapper;
    @Resource
    private MemberMoneyManager        memberMoneyManager;
    @Resource
    private ConfigEnvCacheUtil        configEnvCacheUtil;
    @Resource
    private ConfigRecommendCacheUtils configRecommendCacheUtils;

    @Override
    public List<RspMemberRecommend> getRecommendDetailList( String code, String memberId ) {
        return memberRecommendMapper.getRecommendDetailList( code, memberId );
    }

    @Override
    public RspMyRecommend getRecommendDetail( String memberId ) {
        RspMyRecommend rspMyRecommend = memberRecommendMapper.getMyRecommend( memberId );

        List<String> valueList = configEnvCacheUtil.getConf( Arrays.asList( "share_url", "share_background", "share_icon" ) );
        rspMyRecommend.setMemberCode( memberId );
        rspMyRecommend.setUrl( valueList.get( 0 ).concat( "channelCode=" ).concat( memberId ) );
        rspMyRecommend.setShareBackground( valueList.get( 1 ) );
        rspMyRecommend.setShareIcon( valueList.get( 2 ) );
        return rspMyRecommend;
    }

    @Override
    public RspBase<?> receiveRecommendReward( String memberId ) {
        List<MemberRecommend> listRecommend = new QueryChainWrapper<>( memberRecommendMapper )
                .eq( "inviter_id", memberId )
                .eq( "status", 0 )
                .select( "id", "commission" )
                .list();
        if ( CollectionUtils.isEmpty( listRecommend ) ) {
            return RspBase.businessError( "暂无可领取的佣金" );
        }
        SpringUtils.getBean( RecommendService.class ).receiveReward( memberId, listRecommend );
        return RspBase.ok( memberRecommendMapper.getMyRecommend( memberId ) );
    }

    @Transactional( rollbackFor = Exception.class )
    public void receiveReward( String memberId, List<MemberRecommend> listRecommend ) {
        BigDecimal sionSum = listRecommend
                .stream()
                .map( MemberRecommend::getCommission )
                .reduce( BigDecimal.ZERO, BigDecimal::add );
        int i = memberRecommendMapper.updateCommissionByBatch( listRecommend );
        //佣金记录
        LogCommission logCommission = new LogCommission();
        logCommission.setCommission( sionSum );
        logCommission.setCreateTime( LocalDateTime.now() );
        logCommission.setMemberId( memberId );
        int j = logCommissionMapper.insert( logCommission );
        if ( i <= 0 || j <= 0 ) {
            throw new BusinessException( "佣金领取失败" );
        }
        memberMoneyManager.addMemberMoney( memberId, sionSum, EnumMoney.COMMISSION, 1, memberId + "的推广佣金", null, null );
    }

    @Override
    public List<RspDetailCommission> getRecommendRewardDetailList( String memberId ) {
        return logCommissionMapper.findByMemberId( memberId );
    }

    @Override
    public List<ConfigRecommend> getRecommendDesc() {
        return configRecommendCacheUtils
                .getBillMap()
                .values()
                .stream()
                .sorted( Comparator.comparing( ConfigRecommend::getLevel ) )
                .collect( Collectors.toList() );
    }
}
