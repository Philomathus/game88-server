package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfoHistory;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.core.member.mapper.MemberInfoHistoryMapper;
import tv.game88.platform.api.service.MemberInfoHistoryService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class MemberInfoHistoryServiceImpl extends ServiceImpl<MemberInfoHistoryMapper, MemberInfoHistory> implements MemberInfoHistoryService {

    @Resource
    private MemberCardMapper memberCardMapper;

    @Resource
    private ForkJoinPool forkJoinPool;


    @Override
    public List<MemberInfoHistory> memberInfoHistoryList( MemberInfoHistory memberInfoHistory ) {
        //有其他搜索条件的时候，忽略时间
        if ( StringUtils.isNotBlank( memberInfoHistory.getSearchValue() )
                || StringUtils.isNotBlank( memberInfoHistory.getLoginIp() )
                || StringUtils.isNotBlank( memberInfoHistory.getInviterCode() ) ) {
            memberInfoHistory.setSelectDate( null );
        }
        String[] selectDate = memberInfoHistory.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            memberInfoHistory.setSelectStartDate( selectDate[ 0 ] );
            memberInfoHistory.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.selectMemberInfoHistoryList( memberInfoHistory );
    }

    @Override
    public BigDecimal getHistoryRecharge( String memberId ) {
        BigDecimal money = this.baseMapper.selectMemberInfoHistoryRechargeById( memberId );
        return money == null ? BigDecimal.ZERO : money;
    }

    @Override
    public List<MemberCard> selectMemberCardList( String memberId ) {
        return memberCardMapper.selectMemberCard( memberId );
    }

    @Override
    public RspBase<?> personalReport( String startTime, String endTime, String memberId ) {
        List<Callable<Map<String, Object>>> forkJoinTasks = new ArrayList<>();

        // 线下充值 Offline recharge
        forkJoinTasks.add( () -> ImmutableMap.of( "personalRecharge", this.baseMapper.personalRecharge( startTime, endTime,
                memberId ) ) );
        // 线上充值 online recharge
        forkJoinTasks.add( () -> ImmutableMap.of( "personalOnlineRecharge", this.baseMapper.personalOnlineRecharge( startTime,
                endTime, memberId ) ) );
        //        // 线上充值2 online recharge 2
        //        forkJoinTasks.add( () -> ImmutableMap.of( "personalAgentRecharge", this.baseMapper.personalAgentRecharge(
        //        startTime,
        //                endTime, memberId ) ) );
        // 线上充值3 online recharge 3
        forkJoinTasks.add( () -> ImmutableMap.of( "personalUsdtRecharge", this.baseMapper.personalUsdtRecharge( startTime,
                endTime, memberId ) ) );
        // 提款 withdrawal
        //        forkJoinTasks.add( () -> ImmutableMap.of( "personalWithdrawRecharge",
        //                this.baseMapper.personalWithdrawRecharge( startTime, endTime, memberId ) ) );
        forkJoinTasks.add( () -> ImmutableMap.of( "totalAccount",
                this.baseMapper.totalAccount( startTime, endTime, memberId ) ) );

        List<Future<Map<String, Object>>> futureList = forkJoinPool.invokeAll( forkJoinTasks );
        Set<Map<String, Object>> resultSet = futureList.stream().map( t -> {
            try {
                return t.get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toSet() );
        resultSet.add( ImmutableMap.of( "memberId", memberId ) );

        Map<String, Object> resultMap = resultSet
                .stream()
                .map( Map::entrySet )
                .flatMap( Set::stream )
                .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );

        //        List<Map> mapList = this.baseMapper.personalGameData( startTime, endTime, memberId, memberId.substring(
        //                memberId.length() - 1 ) );

        //        resultMap.put( "bCodeList", mapList );

        return RspBase.ok( resultMap );
    }

    @Override
    public Map listCount( MemberInfoHistory memberInfoHistory ) {
        //有其他搜索条件的时候，忽略时间
        if ( StringUtils.isNotBlank( memberInfoHistory.getSearchValue() )
                || StringUtils.isNotBlank( memberInfoHistory.getLoginIp() )
                || StringUtils.isNotBlank( memberInfoHistory.getInviterCode() ) ) {
            memberInfoHistory.setSelectDate( null );
        }
        String[] selectDate = memberInfoHistory.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            memberInfoHistory.setSelectStartDate( selectDate[ 0 ] );
            memberInfoHistory.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.listCountMemberHistory( memberInfoHistory );
    }
}
