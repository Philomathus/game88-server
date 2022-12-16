package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqMemberRechargeOnline;
import tv.game88.pay.api.dto.RspPayChannelName;
import tv.game88.pay.api.dto.RspRechargeOnline;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.mapper.PayChannelMapper;
import tv.game88.pay.api.mapper.PayPlatformMapper;
import tv.game88.pay.api.service.MemberRechargeOnlineService;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MemberRechargeOnlineServiceImpl extends ServiceImpl<MemberRechargeOnlineMapper, MemberRechargeOnline> implements MemberRechargeOnlineService {
    @Resource
    private PayService        payService;
    @Resource
    private PayChannelMapper  payChannelMapper;
    @Resource
    private PayPlatformMapper payPlatformMapper;

    @Override
    public List<MemberRechargeOnline> selectMemberRechargeOnlineList( ReqMemberRechargeOnline req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        List<MemberRechargeOnline> memberRechargeOnlines = this.baseMapper.selectMemberRechargeOnlineList( req );
        if ( !CollectionUtils.isEmpty( memberRechargeOnlines ) ) {
            List<RspPayChannelName> payChannelNames = payChannelMapper.selectPayChannelName( memberRechargeOnlines
                    .stream()
                    .map( MemberRechargeOnline::getChannelId )
                    .collect( Collectors.toSet() ) );
            List<PayPlatform> payPlatforms = payPlatformMapper.selectBatchIds( memberRechargeOnlines
                    .stream()
                    .map( MemberRechargeOnline::getPlatformId )
                    .collect( Collectors.toSet() ) );
            for ( MemberRechargeOnline memberRechargeOnline : memberRechargeOnlines ) {
                if ( BooleanUtils.isTrue( memberRechargeOnline.getPatchOrder() ) && memberRechargeOnline.getStatus() == 1 ) {
                    memberRechargeOnline.setStatus( 2 );
                }
                for ( RspPayChannelName payChannelName : payChannelNames ) {
                    if ( Objects.equals( memberRechargeOnline.getChannelId(), payChannelName.getId() ) ) {
                        memberRechargeOnline.setChannelName( payChannelName.getChannelName() );
                        memberRechargeOnline.setPlatformName( payChannelName.getPlatformName() );
                        memberRechargeOnline.setRate( payChannelName.getPayRate() );
                    }
                }
                for ( PayPlatform payPlatform : payPlatforms ) {
                    if ( Objects.equals( memberRechargeOnline.getPlatformId(), payPlatform.getId() ) ) {
                        memberRechargeOnline.setPlatformName( payPlatform.getName() );
                    }
                }
            }
        }
        return memberRechargeOnlines;
    }

    @Override
    public Map listCount( ReqMemberRechargeOnline req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.listCount( req );
    }

    @Override
    public RspBase<?> payPatchOrder( MemberRechargeOnline req ) {
        MemberRechargeOnline memberRechargeOnline = this.baseMapper.selectById( req.getOrderNo() );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            return RspBase.businessError( "订单状态有误，补单失败" );
        }
        memberRechargeOnline.setRealMoney( req.getRealMoney() );
        memberRechargeOnline.setPatchOrder( true );
        memberRechargeOnline.setRemark( req.getRemark() );

        payService.updatePayJourStatus( memberRechargeOnline, req.getRemark() );
        return RspBase.ok( "人工补单成功" );
    }

    @Override
    public MemberRechargeOnline selectMemberRechargeOnlineById( String id ) {
        return this.baseMapper.selectMemberRechargeOnlineById( id );
    }

    @Override
    public List<RspRechargeOnline> selectRspReportList( ReqMemberRechargeOnline req ) {
        if ( req.getUpdateTime() != null ) {
            req.setSelectStartDate( req.getUpdateTime() + " 00:00:00" );
            req.setSelectEndDate( req.getUpdateTime() + " 23:59:59" );
        }
        return this.baseMapper.selectRspReportList( req );
    }

    @Override
    public Map reportListCount( ReqMemberRechargeOnline req ) {
        if ( req.getUpdateTime() != null ) {
            req.setSelectStartDate( req.getUpdateTime() + " 00:00:00" );
            req.setSelectEndDate( req.getUpdateTime() + " 23:59:59" );
        }
        return this.baseMapper.reportListCount( req );
    }
}

