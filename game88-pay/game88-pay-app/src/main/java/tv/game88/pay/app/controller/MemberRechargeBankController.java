package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.pay.api.dto.*;
import tv.game88.pay.api.service.MemberRechargeBankService;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@Tag( name = "银行卡绑定与申请入款相关接口" )
@Log4j2
public class MemberRechargeBankController extends BaseController {
    @Resource
    private MemberRechargeBankService memberRechargeBankService;

    @Operation( summary = "获取充值银行卡列表" )
    @PostMapping( "/rechargeBankList" )
    public RspBase<List<RspPayRechargeBank>> rechargeBankList() {
        PlatformUser             platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        List<RspPayRechargeBank> data;
        if ( platformUser.getStatus() == 6 ) {
            data = new ArrayList<>();
        } else {
            data = memberRechargeBankService.selectList( platformUser.getId(), platformUser.getVip() );
        }
        return RspBase.ok( data );
    }

    @Operation( summary = "获取绑定的银行卡" )
    @PostMapping( "/getBindCardList" )
    public RspBase<RspWithdrawBank> getBindCardList() {
        String memberId = MemberSecurityUtils.getUserId();
        return memberRechargeBankService.getBindCardList( memberId );
    }

    @Operation( summary = "设置默认提现卡" )
    @PostMapping( "/setBindCardDv" )
    public RspBase<?> setBindCardDv( @RequestBody ReqMemberCardCancel reqMemberCard ) {
        String memberId = MemberSecurityUtils.getUserId();
        return toResult( memberRechargeBankService.setBindCardDv( memberId, reqMemberCard.getCardId() ) );
    }

    @Operation( summary = "绑定提现卡" )
    @PostMapping( "/setBindCard" )
    public RspBase<?> setBindCard( @Validated @RequestBody ReqMemberCard reqMemberCard ) {
        String memberId = MemberSecurityUtils.getUserId();
        return memberRechargeBankService.setBindCard( memberId, reqMemberCard );
    }

    @Operation( summary = "银行卡充值" )
    @PostMapping( "/bankRecharge" )
    public RspBase<?> bankCardRecharge( @Validated @RequestBody ReqMemberCardRecharge req ) {
        log.info( "the parameters are : {} ", req );
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        return memberRechargeBankService.bankCardRecharge( platformUser, req );
    }
}
