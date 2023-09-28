package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.service.WalletRecordService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.vo.PlatformUser;
import tv.game88.wallet.app.manager.MemberTokenManager;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@Tag( name = "支付接口" )
@Hidden
@Log4j2
public class PayController extends BaseController {
    @Resource
    private WalletRecordService walletRecordService;
    @Resource
    private WalletUserService   walletUserService;
    @Resource
    private MemberTokenManager  memberTokenManager;

    @Operation( summary = "支付下单接口" )
    @PostMapping( "/common/depositOrder" )
    @ResponseBody
    @Anonymous
    public RspBase<RspWalletRecordPay> payOrder( @RequestBody @Validated ReqDepositOrder reqDepositOrder ) throws Exception {
        return walletRecordService.payOrder( reqDepositOrder );
    }

    @Operation( summary = "代付下单接口" )
    @PostMapping( "/common/withdrawOrder" )
    @ResponseBody
    @Anonymous
    public RspBase<RspWalletRecord> withdrawOrder( @RequestBody @Validated ReqWithdrawOrder reqWithdrawOrder ) {
        return walletRecordService.withdrawOrder( reqWithdrawOrder );
    }

    @Operation( summary = "支付(代付)订单查询" )
    @PostMapping( "/common/orderQuery" )
    @ResponseBody
    @Anonymous
    public RspBase<RspWalletRecord> orderQuery( @RequestBody @Validated ReqOrderQuery reqOrderQuery ) {
        return walletRecordService.orderQuery( reqOrderQuery );
    }

    @Operation( summary = "用户进入支付页面" )
    @GetMapping( "/common/toDepositOrder" )
    @Anonymous
    public ModelAndView toDepositOrder( @RequestParam( value = "s" ) String s, @RequestParam( value = "t" ) long t ) throws Exception {
        return walletRecordService.toDepositOrder( s, t );
    }

    @Operation( summary = "内嵌登录接口" )
    @PostMapping( "/common/embeddedLogin" )
    @ResponseBody
    @Anonymous
    public RspBase<?> embeddedLogin( @RequestBody @Validated ReqEmbeddedLogin reqEmbeddedLogin ) {
        RspBase<?> rspMemberRspBase = walletUserService.embeddedLogin( reqEmbeddedLogin );
        Object     object           = rspMemberRspBase.getData();
        if ( object != null ) {
            Map<String, Object> resultMap = ( Map<String, Object> ) object;

            String token = memberTokenManager.setRspMemberToken( ( PlatformUser ) resultMap.get( "userIfo" ), null );
            resultMap.put( "token", token );
        }
        return rspMemberRspBase;
    }

    @Operation( summary = "用户请求支付" )
    @PostMapping( "/common/payDepositOrder" )
    @ResponseBody
    @Anonymous
    public RspBase<?> payDepositOrder( @RequestBody @Validated ReqPayDepositOrder reqPayDepositOrder ) throws Exception {
        return walletRecordService.payDepositOrder( reqPayDepositOrder );
    }
}
