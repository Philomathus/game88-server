package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.pay.api.dto.RspMemberWithdrawLogInfo;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.mapper.MemberWithdrawDetailMapper;
import tv.game88.pay.api.service.MemberWithdrawDetailService;

import java.math.BigDecimal;

@Service
public class MemberWithdrawDetailServiceImpl extends ServiceImpl<MemberWithdrawDetailMapper, MemberWithdrawDetail> implements MemberWithdrawDetailService {
    @Override
    public RspMemberWithdrawLogInfo getRspWithdrawInfo( MemberInfo memberInfo ) {
        //未打码金额 = 需求打码 - 累计有效打码
        BigDecimal noClean = memberInfo.getCodeWill().subtract( memberInfo.getCodeNow() );
        if ( noClean.compareTo( BigDecimal.ZERO ) < 0 ) {
            noClean = BigDecimal.ZERO;
        }
        BigDecimal nowTotal = memberInfo.getAccountNow();
        //可提现金额 = 账户余额 - 未打码金额
        BigDecimal canWithdrawMoney = nowTotal.subtract( noClean );
        if ( canWithdrawMoney.compareTo( BigDecimal.ZERO ) < 0 ) {
            canWithdrawMoney = BigDecimal.ZERO;
        }
        RspMemberWithdrawLogInfo rsp = new RspMemberWithdrawLogInfo();
        rsp.setNeedBeat( noClean );
        rsp.setCanWithdrawMoney( canWithdrawMoney );
        rsp.setAccountNow( nowTotal );
        return rsp;
    }
}

