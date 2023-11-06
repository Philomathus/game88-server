package tv.game88.wallet.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.service.WalletMessageService;
import tv.game88.wallet.api.type.WalletMessageEnum;

import jakarta.annotation.Resource;
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
    @Resource
    private RedisUtils           redisUtils;

    /**
     * 查询系统站内信列表
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:list')" )
    @GetMapping( "/list" )
    public RspBase<List<WalletMessage>> list( WalletMessage walletMessage ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        walletMessage.setType( WalletMessageEnum.system );
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
     * 新增系统站内信
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:add')" )
    @Log( title = "站内信", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody WalletMessage walletMessage ) {
        walletMessage.setId( null );
        walletMessage.setCreateBy( SecurityUtils.getUsername() );
        walletMessage.setCreateTime( LocalDateTime.now() );
        walletMessage.setIsRead( null );
        walletMessage.setReceiverUserId( null );
        walletMessage.setType( WalletMessageEnum.system );
        return toResult( walletMessageService.save( walletMessage ) );
    }

    /**
     * 删除站内信
     */
    @PreAuthorize( "@ss.hasPermi('admin:walletMessage:remove')" )
    @Log( title = "站内信", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isSave = walletMessageService.removeByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            for ( Long id : ids ) {
                redisUtils.unlink( ConstantsWallet.MESSAGE_SYSTEM_IS_READ + id );
            }
        }
        return toResult( isSave );
    }
}