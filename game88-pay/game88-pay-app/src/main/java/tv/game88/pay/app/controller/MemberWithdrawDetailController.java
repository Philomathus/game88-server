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
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.pay.api.dto.*;
import tv.game88.pay.api.service.MemberWithdrawDetailService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "会员提现相关接口" )
@Log4j2
public class MemberWithdrawDetailController extends BaseController {
    @Resource
    private MemberWithdrawDetailService memberWithdrawDetailService;

    @Operation( summary = "获取提现信息" )
    @PostMapping( "/withdrawDetailInfo" )
    public RspBase<RspMemberWithdrawDetailInfo> withdrawDetailInfo() {
        return RspBase.ok( memberWithdrawDetailService.getRspWithdrawDetail( MemberSecurityUtils.getUserId() ) );
    }

    @Operation( summary = "查看是否开启提现密码", description = "返回值 ：true=输入密码，false设置密码" )
    @PostMapping( "/withdrawPassIsOpen" )
    public RspBase<?> memberWithdrawPassIsOpen() {
        return memberWithdrawDetailService.memberWithdrawPassIsOpen( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "设置提现密码" )
    @PostMapping( "/withdrawPassSet" )
    public RspBase<?> memberWithdrawPassIsOpen( @Validated @RequestBody ReqBoxPass boxPass ) {
        return memberWithdrawDetailService.memberWithdrawPassSet( MemberSecurityUtils.getUserId(), boxPass );
    }

    @Operation( summary = "人工提现申请" )
    @PostMapping( "/withdrawBank" )
    public RspBase<?> withdrawBank( @Validated @RequestBody ReqMemberCardWithdraw req ) {
        return memberWithdrawDetailService.withdrawBank( MemberSecurityUtils.getUserId(), req );
    }

    @Operation( summary = "提现充值详情" )
    @PostMapping( "/withdrawRechargeDetail" )
    public RspBase<List<RspWithdrawRechargeDetail>> withdrawDetail( @RequestBody ReqDetailType reqDetailType ) {
        startPage( reqDetailType );
        List<RspWithdrawRechargeDetail> resultList =
                memberWithdrawDetailService.withdrawRechargeDetail( MemberSecurityUtils.getUserId(), reqDetailType.getType() );
        return getRspBasePage( resultList, reqDetailType );
    }
}
