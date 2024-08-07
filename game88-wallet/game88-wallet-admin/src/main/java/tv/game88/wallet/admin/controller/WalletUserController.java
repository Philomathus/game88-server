package tv.game88.wallet.admin.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.wallet.api.dto.ReqChangeAllStatus;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.service.WalletUserPayMethodService;
import tv.game88.wallet.api.service.WalletUserService;

import java.util.List;

/**
 * 钱包用户Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/admin/walletUser" )
public class WalletUserController extends BaseController {
    @Resource
    private WalletUserService walletUserService;

    @Resource
    private WalletUserPayMethodService walletUserPayMethodService;

    @Resource
    private RedisUtils              redisUtils;

    /**
     * 查询钱包用户列表
     */
   // @PreAuthorize( "@ss.hasPermi('admin:walletUser:list')" )
    @GetMapping( "/list" )
    public RspBase<List<WalletUser>> list( WalletUser walletUser ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        List<WalletUser> list;
        if( StringUtils.isNotBlank( walletUser.getBankAccount()  ) ){
            WalletUserPayMethod wallet = walletUserPayMethodService.getWalletUserPayMethod( walletUser.getBankAccount() );
            if ( wallet != null) {
                walletUser.setId(wallet.getUserId());
            }
            startPage( pageDomain );
            list = walletUserService.selectWalletUserList(walletUser);
        }else{
            startPage( pageDomain );
            list = walletUserService.selectWalletUserList(walletUser);
        }
        return getRspBasePage( list, pageDomain );

    }

    /**
     * 导出钱包用户列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletUser:export')" )
    @Log( title = "钱包用户", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( WalletUser walletUser, HttpServletResponse response ) {
        List<WalletUser> list = walletUserService.selectWalletUserList( walletUser );
        ExportExcelUtil.exportBigExcel( list, "钱包用户", "钱包用户表", WalletUser.class, response );
    }

    /**
     * 获取钱包用户详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletUser:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<WalletUser> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( walletUserService.getById( id ) );
    }

    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus/{memberId}" )
    public Object changeStatusBan( @PathVariable String memberId, Integer status, Integer googleAuthCode, String remarks) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setStatus( status );
        update.setRemarks( remarks );
        return toResult( walletUserService.updateById( update ) );
    }

    @PreAuthorize( "@ss.hasPermi('admin:walletUser:changeStatus')" )
    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeVerified/{memberId}" )
    public Object changeVerified( @PathVariable String memberId, Integer isVerified, Integer googleAuthCode, String remarks) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setIsVerified( isVerified );
        update.setRemarks( remarks );
        return toResult( walletUserService.updateById( update ) );
    }

    /**
     * 重置基金密码
     *
     * @param memberId
     * @param googleAuthCode
     */
    @PostMapping( "/resetFundPass/{memberId}" )
    public Object resetTxPass( @PathVariable String memberId, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setFundPassword( "" );
        update.setStatus( 1 );
        redisUtils.unlink( Constants.WALLET_PREX + "lock:fundPassword:" + memberId );
        return toResult( walletUserService.updateById( update ) );
    }


    @PostMapping( value = "/realNameUpdate/{memberId}" )
    @Log( title = "重置会员登录密码", businessType = BusinessType.UPDATE )
    public RspBase<?> reset( @PathVariable String memberId, String realName, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setRealName( realName );
        return toResult(walletUserService.updateById( update ) );
    }

    /**
     * 会员银行卡列表
     */
    @GetMapping( value = "/card-list" )
    public RspBase<List<WalletUserPayMethod>> findMemberCardList( String userId ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletUserPayMethod> list = walletUserPayMethodService.selectMemberCardList( userId );
        for ( WalletUserPayMethod memberCard : list ) {
            memberCard.setOldBankAccount( memberCard.getBankAccount() );
            memberCard.setOldBankId( memberCard.getBankId() );
            memberCard.setOldRealName( memberCard.getRealName() );
        }
        return getRspBasePage( list, pageDomain );
    }


    @Log( title = "修改用户银行卡信息", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeBank" )
    public RspBase<?> changeBank( @RequestBody WalletUserPayMethod memberCard ) {
        return walletUserPayMethodService.changeBank( memberCard );
    }

//    @PreAuthorize( "@ss.hasPermi('member:memberInfo:unbindCard')" )
    @Log( title = "解绑银行卡", businessType = BusinessType.UPDATE )
    @PutMapping( "/unbindCard" )
    public RspBase<?> unbindCard( @RequestBody WalletUserPayMethod memberCard ) {
        return walletUserPayMethodService.unbindCard( memberCard );
    }

    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeAllStatus" )
    public Object changeAllStatus(@RequestBody ReqChangeAllStatus reqChangeAllStatus) throws Exception {
        SecurityUtils.verifyMFACode( reqChangeAllStatus.getGoogleAuthCode() );

        return toResult(walletUserService.update(new LambdaUpdateWrapper<WalletUser>()
                .set(WalletUser::getStatus, reqChangeAllStatus.getStatus())
                .set(WalletUser::getRemarks, reqChangeAllStatus.getRemarks())
                .in(WalletUser::getId, reqChangeAllStatus.getMemberIds())
        ));

    }

    @Log( title = "修改用户验证状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeAllVerificationStatus" )
    public RspBase<?> changeAllVerificationStatus( @RequestBody ReqChangeAllStatus reqChangeAllVerificationStatus ) throws Exception {
        SecurityUtils.verifyMFACode( reqChangeAllVerificationStatus.getGoogleAuthCode() );

        return toResult(walletUserService.update(new LambdaUpdateWrapper<WalletUser>()
                .set(WalletUser::getIsVerified, reqChangeAllVerificationStatus.getStatus())
                .set(WalletUser::getRemarks, reqChangeAllVerificationStatus.getRemarks())
                .in(WalletUser::getId, reqChangeAllVerificationStatus.getMemberIds())
        ));
    }

}