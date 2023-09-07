package tv.game88.wallet.app.controller;

import io.jsonwebtoken.lang.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.wallet.api.dto.ReqDepositOrder;
import tv.game88.wallet.api.dto.ReqOrderQuery;
import tv.game88.wallet.api.dto.ReqWithdrawOrder;
import tv.game88.wallet.api.dto.RspPayResult;
import tv.game88.wallet.api.service.WalletRecordService;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "支付接口" )
@Log4j2
public class PayController extends BaseController {
    @Resource
    private WalletRecordService walletRecordService;

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
    @Anonymous
    public RspPayResult payOrder( @RequestBody @Validated ReqDepositOrder reqDepositOrder ) {
        return walletRecordService.payOrder( reqDepositOrder );
    }

    @Operation( summary = "下发下单接口" )
    @PostMapping( "/common/withdrawOrder" )
    @Anonymous
    public RspPayResult withdrawOrder( @RequestBody @Validated ReqWithdrawOrder reqWithdrawOrder ) {
        return walletRecordService.withdrawOrder( reqWithdrawOrder );
    }

    @Operation( summary = "支付（下发）订单查询" )
    @PostMapping( "/common/orderQuery" )
    @Anonymous
    public RspPayResult orderQuery( @RequestBody @Validated ReqOrderQuery reqOrderQuery ) {
        return walletRecordService.orderQuery( reqOrderQuery );
    }
}
