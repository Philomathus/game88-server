package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.dto.RspPayRechargeUsdt;
import tv.game88.pay.api.service.MemberRechargeUsdtService;
import tv.game88.pay.api.service.PayRechargeUsdtService;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "USDT充值相关接口" )
@Log4j2
public class MemberRechargeUsdtController {
    @Resource
    private MemberRechargeUsdtService memberRechargeUsdtService;
    @Resource
    private PayRechargeUsdtService    payRechargeUsdtService;

    @Operation( summary = "获取充值USDT列表" )
    @PostMapping( "/rechargeUsdtList" )
    public RspBase<List<RspPayRechargeUsdt>> rechargeUsdtList() {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        return RspBase.ok( payRechargeUsdtService.selectList( platformUser.getId(), platformUser.getVip() ) );
    }

    @Operation( summary = "USDT充值" )
    @PostMapping( "/usdtRecharge" )
    public RspBase<?> usdtRecharge( @Validated @RequestBody ReqMemberRechargeUsdt req ) {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        return memberRechargeUsdtService.usdtRecharge( platformUser, req );
    }
}
