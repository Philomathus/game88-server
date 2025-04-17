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
import tv.game88.platform.api.entity.MessageCommonProblem;
import tv.game88.platform.api.service.MessageCommonProblemService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 常用问题Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/message/commonProblem" )
public class MessageCommonProblemController extends BaseController {
    @Resource
    private MessageCommonProblemService messageCommonProblemService;
    @Resource
    private MessageCacheUtil            messageCacheUtil;

    /**
     * 查询常用问题列表
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MessageCommonProblem>> list( MessageCommonProblem messageCommonProblem ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MessageCommonProblem> list = messageCommonProblemService.selectMessageCommonProblemList( messageCommonProblem );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出常用问题列表
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:export')" )
    @Log( title = "常用问题", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<?>> export( MessageCommonProblem messageCommonProblem ) {
        List<MessageCommonProblem> list = messageCommonProblemService.selectMessageCommonProblemList( messageCommonProblem );
        return RspBase.ok(list);
    }

    /**
     * 获取常用问题详细信息
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MessageCommonProblem> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( messageCommonProblemService.getById( id ) );
    }

    /**
     * 新增常用问题
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:add')" )
    @Log( title = "常用问题", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MessageCommonProblem messageCommonProblem ) {
        messageCommonProblem.setCreateBy( SecurityUtils.getUsername() );
        messageCommonProblem.setCreateTime( LocalDateTime.now() );
        messageCommonProblem.setEffect( false );
        return toResult( messageCommonProblemService.save( messageCommonProblem ) );
    }

    /**
     * 修改常用问题
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:edit')" )
    @Log( title = "常用问题", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody MessageCommonProblem messageCommonProblem ) {
        messageCommonProblem.setEffect( null );
        boolean isUpdate = messageCommonProblemService.updateById( messageCommonProblem );
        if ( isUpdate ) {
            messageCacheUtil.clear( MessageCacheUtil.COMMON_PROBLEM );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除常用问题
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:remove')" )
    @Log( title = "常用问题", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isRemove = messageCommonProblemService.removeByIds( Arrays.asList( ids ) );
        if ( isRemove ) {
            messageCacheUtil.clear( MessageCacheUtil.COMMON_PROBLEM );
        }
        return toResult( isRemove );
    }

    /**
     * 修改常用问题激活状态
     */
    @PreAuthorize( "@ss.hasPermi('message:commonProblem:effect')" )
    @Log( title = "常用问题激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        MessageCommonProblem update = new MessageCommonProblem();
        update.setId( id );
        update.setEffect( effect );
        boolean isUpdate = messageCommonProblemService.updateById( update );
        if ( isUpdate ) {
            messageCacheUtil.clear( MessageCacheUtil.COMMON_PROBLEM );
        }
        return toResult( isUpdate );
    }
}