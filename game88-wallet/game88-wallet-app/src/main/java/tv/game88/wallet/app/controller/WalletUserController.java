package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.security.annotation.Anonymous;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.constants.ReqConstant;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.WalletUserFundEnum;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import jakarta.annotation.Resource;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RestController
@Tag( name = "钱包用户信息接口" )
@Log4j2
public class WalletUserController extends BaseController {
    @Resource
    private WalletUserService walletUserService;

    @Operation( summary = "获取账户余额" )
    @PostMapping( "/api/getAmount" )
    public RspBase<RspAmount> getAmount() {
        return walletUserService.getAmount( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "获取会员信息" )
    @PostMapping( "/api/getUserInfo" )
    public RspBase<RspMember> getUserInfo() {
        return walletUserService.getUserInfo( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "设置资金密码" )
    @PostMapping( "/api/fundPassSet" )
    public RspBase<?> fundPassSet( @Validated @RequestBody ReqFundPass reqFundPass ) {
        return walletUserService.fundPassSet( MemberSecurityUtils.getUserId(), reqFundPass );
    }

    @Operation( summary = "重置基金密码" )
    @PostMapping( "/api/fundPassReset" )
    public RspBase<?> funPassReset( @Validated @RequestBody ReqConstant.ReqResetFundPasswd reqResetFundPasswd ) {
        return walletUserService.resetFunPassword( MemberSecurityUtils.getUserId(), reqResetFundPasswd );

    }

    @Operation( summary = "个人账变" )
    @PostMapping( "/api/getFundDetails" )
    public RspBase<List<RspLogFund>> getFundDetails( @Validated @RequestBody ReqLogFund reqLogFund ) {
        startPage( reqLogFund );
        List<RspLogFund> fundDetails = walletUserService.getFundDetails( MemberSecurityUtils.getUserId(), reqLogFund );
        return getRspBasePage( fundDetails, reqLogFund );
    }

    @Operation( summary = "资金枚举类型" )
    @PostMapping( "/api/getFundEnumTypes" )
    @Anonymous
    public RspBase<List<RspFundEnumType>> getFundEnumTypes() {
        return RspBase.ok( WalletUserFundEnum.getFundEnumType() );
    }

    @Operation( summary = "个人转账" )
    @PostMapping( "/api/personalTransfer" )
    public RspBase<?> personalTransfer( @Validated @RequestBody ReqPersonalTransfer reqPersonalTransfer ) {
        return walletUserService.personalTransfer( MemberSecurityUtils.getUserId(), reqPersonalTransfer );
    }

    @Operation( summary = "上传验证身份信息" )
    @PostMapping( "/api/verifyIdCard" )
    public RspBase<?> verifyIdCard( @Validated @RequestBody ReqVerifyIdCard reqVerifyIdCard ) {
        return walletUserService.verifyIdCard( MemberSecurityUtils.getUserId(), reqVerifyIdCard );
    }

    @Operation( summary = "上传验证身份信息" )
    @PutMapping( "/api/updateIdCard" )
    public RspBase<?> updateIdCardPut( @RequestBody IdCardDto reqUpdateIdCard ) {

        WalletUser update = new WalletUser();
        update.setId( MemberSecurityUtils.getUserId() );

        if ( isNotBlank( reqUpdateIdCard.getIdCardNumber() ) ) {
            update.setIdNumber( reqUpdateIdCard.getIdCardNumber() );
        }
        if ( isNotBlank( reqUpdateIdCard.getRealName() ) ) {
            update.setRealName( reqUpdateIdCard.getRealName() );
        }
        if ( isNotBlank( reqUpdateIdCard.getIdFrontPic() ) ) {
            update.setIdFrontPic( reqUpdateIdCard.getIdFrontPic() );
        }
        if ( isNotBlank( reqUpdateIdCard.getIdBackPic() ) ) {
            update.setIdBackPic( reqUpdateIdCard.getIdBackPic() );
        }

        update.setIsVerified( 1 );

        boolean hasUpdated = walletUserService.updateById( update );

        return hasUpdated ? RspBase.ok() : RspBase.businessError( "申请身份认证异常，请稍后再试" );
    }

    @Operation( summary = "上传验证身份信息" )
    @PostMapping( "/api/idCard" )
    public RspBase<?> getIdCard( ) {
        return walletUserService.getIdCard( MemberSecurityUtils.getUserId() );
    }
}
