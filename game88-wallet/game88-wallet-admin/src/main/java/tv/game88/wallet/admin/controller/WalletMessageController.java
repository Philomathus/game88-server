package tv.game88.wallet.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.service.WalletMessageService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 站内信Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/admin/walletMessage" )
public class WalletMessageController extends BaseController {
    @Resource
    private WalletMessageService walletMessageService;

    /**
     * 查询站内信列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:list')" )
    @GetMapping( "/list" )
    public RspBase<List<WalletMessage>> list( WalletMessage walletMessage ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<WalletMessage> list = walletMessageService.selectWalletMessageList( walletMessage );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取站内信详细信息
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<WalletMessage> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( walletMessageService.getById( id ) );
    }

    /**
     * 新增站内信
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:add')" )
    @Log( title = "站内信", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody WalletMessage walletMessage ) {
        walletMessage.setCreateBy( SecurityUtils.getUsername() );
        walletMessage.setCreateTime( LocalDateTime.now() );
        return toResult( walletMessageService.save( walletMessage ) );
    }

    /**
     * 修改站内信
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:edit')" )
    @Log( title = "站内信", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody WalletMessage walletMessage ) {
        return toResult( walletMessageService.updateById( walletMessage ) );
    }

    /**
     * 删除站内信
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:remove')" )
    @Log( title = "站内信", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        return toResult( walletMessageService.removeByIds( Arrays.asList( ids ) ) );
    }
}