package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.pay.api.dto.ReqPayChannel;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.dto.RspPayChannel;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping( "/pay" )
@Tag( name = "支付充值相关接口" )
@Log4j2
public class PayController {
    @Resource
    private PayService     payService;

    @Operation( summary = "获取充值类型列表" )
    @PostMapping( "/typeList" )
    public RspBase<List<PayType>> securityFindTypeList( @RequestHeader( "dv" ) String deviceType ) {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        log.info( "获取充值类型列表 - memberId:{}", platformUser.getId() );
        return RspBase.ok( payService.findPayTypeList( platformUser, deviceType ) );
    }

    @Operation( summary = "获取充值通道列表" )
    @PostMapping( "/channelList" )
    public RspBase<List<RspPayChannel>> securityFindChannelList( @RequestBody ReqPayChannel reqPayChannel ) {
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        log.info( "获取充值通道列表 - memberId:{}", platformUser.getId() );
        if ( platformUser.getStatus() == 2 ) {
            return RspBase.ok( payService.findPayChannelList( reqPayChannel.getTypeId(), platformUser ) );
        } else {
            return RspBase.ok( payService.findPayChannel( reqPayChannel.getTypeId(), platformUser ) );
        }
    }

    @Operation( summary = "支付充值请求" )
    @PostMapping( "/recharge" )
    public RspBase<?> securityPayRechargeNew( @RequestBody ReqPayRecharge reqPayRecharge ) throws Exception {
        log.info( "充值请求对象:{}", JsonUtil.object2Json( reqPayRecharge ) );
        PlatformUser platformUser = MemberSecurityUtils.getLoginUser().getPlatformUser();
        log.info( "充值请求,userId:{},realIp:{}", platformUser.getId(), reqPayRecharge.getRealIp() );
        return payService.payRecharge( reqPayRecharge, platformUser );
    }

    @GetMapping( value = "/redirect/{orderNo}", produces = MediaType.TEXT_HTML_VALUE )
    @ResponseBody
    public ResponseEntity<String> payRedirect( @PathVariable String orderNo ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add( "client_header_buffer_size", "512k" );
        headers.add( "large_client_header_buffers", "4 512k" );
        return new ResponseEntity<>( payService.payRedirect( orderNo ), headers, HttpStatus.OK );
    }
}
