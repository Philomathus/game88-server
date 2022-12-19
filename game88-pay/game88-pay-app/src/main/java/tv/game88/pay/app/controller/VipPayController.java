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
import tv.game88.pay.api.dto.ReqVipPayDeposit;
import tv.game88.pay.api.dto.RspVipPayLogin;
import tv.game88.pay.api.service.VipPayService;

import javax.annotation.Resource;

@RestController
@Tag( name = "vipPay登录注册以及充值" )
@Log4j2
public class VipPayController extends BaseController {
    @Resource
    private VipPayService vipPayService;

    @Operation( summary = "vipPay登录注册" )
    @PostMapping( "/vipPayLogin" )
    public RspBase<RspVipPayLogin> vipPayLogin() {
        return vipPayService.vipPayLogin( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "vipPay扣款并充值平台" )
    @PostMapping( "/vipPayDeposit" )
    public RspBase<?> vipPayDeposit( @Validated @RequestBody ReqVipPayDeposit reqVipPayDeposit ) {
        return vipPayService.vipPayDeposit( reqVipPayDeposit, MemberSecurityUtils.getUserId() );
    }
}
