package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.pay.api.dto.ReqPayChannel;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.dto.RspPayChannel;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.service.PayService;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "在线支付充值相关接口" )
@Log4j2
public class MemberRechargeOnlineController {
    @Resource
    private PayService payService;

    @Operation( summary = "获取充值类型列表" )
    @PostMapping( "/payTypeList" )
    public RspBase<List<PayType>> payTypeList( @RequestHeader( "dev" ) String deviceType ) {
        PlatformUser  platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        List<PayType> payTypeList  = payService.findPayTypeList( platformUser, deviceType );
        return RspBase.ok( payTypeList );
    }

    @Operation( summary = "获取充值通道列表" )
    @PostMapping( "/payChannelList" )
    public RspBase<List<RspPayChannel>> payChannelList( @RequestBody ReqPayChannel reqPayChannel ) {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        if ( platformUser.getStatus() == 2 ) {
            return RspBase.ok( payService.findPayChannelList( reqPayChannel.getTypeId(), platformUser ) );
        } else {
            return RspBase.ok( payService.findPayChannel( reqPayChannel.getTypeId(), platformUser ) );
        }
    }

    @Operation( summary = "支付充值请求" )
    @PostMapping( "/onlineRecharge" )
    public RspBase<?> onlineRecharge( @Validated @RequestBody ReqPayRecharge reqPayRecharge ) throws Exception {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        reqPayRecharge.setRealIp( ServletUtil.getIp() );
        return payService.payRecharge( reqPayRecharge, platformUser );
    }
}
