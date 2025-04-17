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
import tv.game88.platform.api.cache.MessageCacheUtil;
import tv.game88.platform.api.entity.MessageHomeNotice;
import tv.game88.platform.api.service.MessageHomeNoticeService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 首页公告Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/message/messageHomeNotice" )
public class MessageHomeNoticeController extends BaseController {
    @Resource
    private MessageHomeNoticeService messageHomeNoticeService;
    @Resource
    private MessageCacheUtil         messageCacheUtil;

    /**
     * 查询首页公告列表
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MessageHomeNotice>> list( MessageHomeNotice messageHomeNotice ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MessageHomeNotice> list = messageHomeNoticeService.selectMessageHomeNoticeList( messageHomeNotice );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出首页公告列表
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:export')" )
    @Log( title = "首页公告", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( MessageHomeNotice messageHomeNotice ) {
        List<MessageHomeNotice> list = messageHomeNoticeService.selectMessageHomeNoticeList( messageHomeNotice );
        return RspBase.ok(list);
    }

    /**
     * 获取首页公告详细信息
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MessageHomeNotice> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( messageHomeNoticeService.getById( id ) );
    }

    /**
     * 新增首页公告
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:add')" )
    @Log( title = "首页公告", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MessageHomeNotice messageHomeNotice ) {
        messageHomeNotice.setCreateBy( SecurityUtils.getUsername() );
        messageHomeNotice.setCreateTime( LocalDateTime.now() );
        messageHomeNotice.setEffect( false );
        return toResult( messageHomeNoticeService.save( messageHomeNotice ) );
    }

    /**
     * 修改首页公告
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:edit')" )
    @Log( title = "首页公告", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MessageHomeNotice messageHomeNotice ) {
        messageHomeNotice.setEffect( null );
        boolean isUpdate = messageHomeNoticeService.updateById( messageHomeNotice );
        if ( isUpdate ) {
            messageCacheUtil.clear( MessageCacheUtil.HOME_NOTICE );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除首页公告
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:remove')" )
    @Log( title = "首页公告", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = messageHomeNoticeService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            messageCacheUtil.clear( MessageCacheUtil.HOME_NOTICE );
        }
        return toResult( isRemove );
    }

    /**
     * 修改首页公告激活状态
     */
    @PreAuthorize( "@ss.hasPermi('message:homeNotice:effect')" )
    @Log( title = "首页公告激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        MessageHomeNotice update = new MessageHomeNotice();
        update.setId( id );
        update.setEffect( effect );
        boolean isUpdate = messageHomeNoticeService.updateById( update );
        if ( isUpdate ) {
            messageCacheUtil.clear( MessageCacheUtil.HOME_NOTICE );
        }
        return toResult( isUpdate );
    }
}