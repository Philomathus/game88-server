package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.platform.api.cache.MessageCacheUtil;
import tv.game88.platform.api.entity.MessageOnSite;
import tv.game88.platform.api.service.MemberInfoService;
import tv.game88.platform.api.service.MessageOnSiteService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 站内信Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/message/messageOnSite" )
public class MessageOnSiteController extends BaseController {
    @Resource
    private MessageOnSiteService messageOnSiteService;
    @Resource
    private MessageCacheUtil     messageCacheUtil;

    @Resource
    private MemberInfoService memberInfoService;

    /**
     * 查询站内信列表
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MessageOnSite>> list( MessageOnSite messageOnSite ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MessageOnSite> list = messageOnSiteService.selectMessageOnSiteList( messageOnSite );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出站内信列表
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:export')" )
    @Log( title = "站内信", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( MessageOnSite messageOnSite, HttpServletResponse response ) {
        List<MessageOnSite> list = messageOnSiteService.selectMessageOnSiteList( messageOnSite );
        ExportExcelUtil.exportExcel( list, "站内信", "站内信表", MessageOnSite.class, response );
    }

    /**
     * 获取站内信详细信息
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MessageOnSite> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( messageOnSiteService.getById( id ) );
    }

    /**
     * 新增站内信
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:add')" )
    @Log( title = "站内信", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MessageOnSite messageOnSite ) {
        messageOnSite.setCreateBy( SecurityUtils.getUsername() );
        messageOnSite.setCreateTime( LocalDateTime.now() );
        boolean isSave = messageOnSiteService.save( messageOnSite );
        if ( isSave ) {
            messageCacheUtil.clear( MessageCacheUtil.ON_SITE );
        }
        return toResult( isSave );
    }

    /**
     * 修改站内信
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:edit')" )
    @Log( title = "站内信", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MessageOnSite messageOnSite ) {
        boolean isUpdate = messageOnSiteService.updateById( messageOnSite );
        if ( isUpdate ) {
            messageCacheUtil.clear( MessageCacheUtil.ON_SITE );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除站内信
     */
    @PreAuthorize( "@ss.hasPermi('message:onSite:remove')" )
    @Log( title = "站内信", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = messageOnSiteService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            messageCacheUtil.clear( MessageCacheUtil.ON_SITE );
        }
        return toResult( isRemove );
    }

    @PreAuthorize( "@ss.hasPermi('message:onSite:add')" )
    @Log( title = "会员站内信息", businessType = BusinessType.INSERT )
    @PostMapping("/sendUserMessage")
    public RspBase<?> sendUserMessage( @RequestBody MessageOnSite messageOnSite ) {
        MemberInfo memberInfo = memberInfoService.getById(messageOnSite.getReceiverUserId());
        if ( Objects.isNull(memberInfo)) {
            return RspBase.businessError("发送失败,会员id错误");
        }
        messageOnSite.setCreateBy( SecurityUtils.getUsername() );
        messageOnSite.setCreateTime(LocalDateTime.now());
        int result = messageOnSiteService.insertMessageOnSite( messageOnSite );
        if(result > 0) {
            messageCacheUtil.clear( MessageCacheUtil.ON_SITE );
        }
        return RspBase.ok( result );
    }
}
