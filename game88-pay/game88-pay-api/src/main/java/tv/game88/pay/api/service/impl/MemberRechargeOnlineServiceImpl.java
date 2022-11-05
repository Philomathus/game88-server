package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqMemberRechargeOnline;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.service.MemberRechargeOnlineService;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class MemberRechargeOnlineServiceImpl extends ServiceImpl<MemberRechargeOnlineMapper, MemberRechargeOnline> implements MemberRechargeOnlineService {
    @Resource
    private PayService payService;

    @Override
    public List<MemberRechargeOnline> selectMemberRechargeOnlineList( ReqMemberRechargeOnline req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.selectMemberRechargeOnlineList( req );
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

        payService.updatePayJourStatus( memberRechargeOnline );
        return RspBase.ok( "人工补单成功" );
    }
}

