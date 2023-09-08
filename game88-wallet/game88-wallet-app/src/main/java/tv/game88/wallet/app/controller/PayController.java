package tv.game88.wallet.app.controller;

import io.jsonwebtoken.lang.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.service.WalletRecordService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.vo.PlatformUser;
import tv.game88.wallet.app.manager.MemberTokenManager;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@Tag( name = "支付接口" )
@Log4j2
public class PayController extends BaseController {
    @Resource
    private WalletRecordService walletRecordService;
    @Resource
    private WalletUserService   walletUserService;
    @Resource
    private MemberTokenManager  memberTokenManager;

    @ResponseStatus( HttpStatus.BAD_REQUEST )
    @ResponseBody
    @ExceptionHandler( MethodArgumentNotValidException.class )
    public RspPayResult methodArgumentNotValidException( MethodArgumentNotValidException ex ) {
        BindingResult    result      = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        RspPayResult     error       = new RspPayResult( HttpStatus.BAD_REQUEST.value(), "字段验证错误" );
        for ( FieldError fieldError : fieldErrors ) {
            error.getError()
                 .add( Maps.of( "field", fieldError.getField() ).and( "message", fieldError.getDefaultMessage() ).build() );
        }
        return error;
    }

    @Operation( summary = "支付下单接口" )
    @PostMapping( "/common/depositOrder" )
    @ResponseBody
    @Anonymous
    public RspPayResult payOrder( @RequestBody @Validated ReqDepositOrder reqDepositOrder ) throws Exception {
        return walletRecordService.payOrder( reqDepositOrder );
    }

    @Operation( summary = "下发下单接口" )
    @PostMapping( "/common/withdrawOrder" )
    @ResponseBody
    @Anonymous
    public RspPayResult withdrawOrder( @RequestBody @Validated ReqWithdrawOrder reqWithdrawOrder ) {
        return walletRecordService.withdrawOrder( reqWithdrawOrder );
    }

    @Operation( summary = "支付（下发）订单查询" )
    @PostMapping( "/common/orderQuery" )
    @ResponseBody
    @Anonymous
    public RspPayResult orderQuery( @RequestBody @Validated ReqOrderQuery reqOrderQuery ) {
        return walletRecordService.orderQuery( reqOrderQuery );
    }

    @Operation( summary = "用户进入支付页面" )
    @GetMapping( "/common/toDepositOrder" )
    @Anonymous
    public ModelAndView toDepositOrder( @RequestParam( value = "s" ) String sign, @RequestParam( value = "t" ) long time ) throws Exception {
        String              data      = AESCoder.decryptByKey( sign, AESCoder.secretKey + time );
        Map<String, Object> resultMap = JsonUtil.json2Map( data );
        return walletRecordService.toDepositOrder( resultMap );
    }

    @Operation( summary = "内嵌登录接口" )
    @PostMapping( "/common/embeddedLogin" )
    @Anonymous
    public RspPayResult embeddedLogin( @RequestBody @Validated ReqEmbeddedLogin reqEmbeddedLogin ) {
        RspPayResult rspMemberRspBase = walletUserService.embeddedLogin( reqEmbeddedLogin );
        Object       object           = rspMemberRspBase.getResult();
        if ( object != null ) {
            Map<String, Object> resultMap = ( Map<String, Object> ) object;

            String token = memberTokenManager.setRspMemberToken( ( PlatformUser ) resultMap.get( "userIfo" ), null );
            resultMap.put( "token", token );
        }
        return rspMemberRspBase;
    }
}
