package tv.game88.wallet.admin.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.wallet.api.entity.WalletUser;
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

    /**
     * 查询钱包用户列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletUser:list')" )
    @GetMapping( "/list" )
    public RspBase<List<WalletUser>> list( WalletUser walletUser ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletUser> list = walletUserService.selectWalletUserList( walletUser );
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
        ExportExcelUtil.exportExcel( list, "钱包用户", "钱包用户表", WalletUser.class, response );
    }

    /**
     * 获取钱包用户详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletUser:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<WalletUser> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( walletUserService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('admin:walletUser:changeStatus')" )
    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus/{memberId}" )
    public Object changeStatusBan( @PathVariable String memberId, Integer status, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setStatus( status );
        return toResult( walletUserService.updateById( update ) );
    }

    @PreAuthorize( "@ss.hasPermi('admin:walletUser:changeStatus')" )
    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeVerified/{memberId}" )
    public Object changeVerified( @PathVariable String memberId, Integer isVerified, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        WalletUser update = new WalletUser();
        update.setId( memberId );
        update.setIsVerified( isVerified );
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

}