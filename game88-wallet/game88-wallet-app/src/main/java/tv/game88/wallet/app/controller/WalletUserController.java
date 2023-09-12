package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;
import java.util.List;

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

    @Operation( summary = "获取简单会员信息" )
    @PostMapping( "/api/getUserSimpleInfo" )
    public RspBase<RspUserSimpleInfo> getUserSimpleInfo() {
        return walletUserService.getUserSimpleInfo( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "设置资金密码" )
    @PostMapping( "/api/fundPassSet" )
    public RspBase<?> fundPassSet( @Validated @RequestBody ReqFundPass reqFundPass ) {
        return walletUserService.fundPassSet( MemberSecurityUtils.getUserId(), reqFundPass );
    }

    @Operation( summary = "个人账变" )
    @PostMapping( "/api/getFundDetails" )
    public RspBase<List<RspLogFund>> getFundDetails( @Validated @RequestBody ReqLogFund reqLogFund ) {
        startPage( reqLogFund );
        List<RspLogFund> fundDetails = walletUserService.getFundDetails( MemberSecurityUtils.getUserId(), reqLogFund );
        return getRspBasePage( fundDetails, reqLogFund );
    }

    @Operation( summary = "个人转账" )
    @PostMapping( "/api/personalTransfer" )
    public RspBase<?> personalTransfer( @Validated @RequestBody ReqPersonalTransfer reqPersonalTransfer ) {
        return walletUserService.personalTransfer( MemberSecurityUtils.getUserId(), reqPersonalTransfer );
    }
}
