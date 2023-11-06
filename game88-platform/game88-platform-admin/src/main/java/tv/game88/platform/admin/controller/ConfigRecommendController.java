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
import tv.game88.core.member.cache.ConfigRecommendCacheUtils;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.platform.api.service.ConfigRecommendService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 推广设置Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/config/recommend" )
public class ConfigRecommendController extends BaseController {
    @Resource
    private ConfigRecommendService    configRecommendService;
    @Resource
    private ConfigRecommendCacheUtils configRecommendCacheUtils;

    /**
     * 查询推广设置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigRecommend>> list( ConfigRecommend configRecommend ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigRecommend> list = configRecommendService.selectConfigRecommendList( configRecommend );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出推广设置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:export')" )
    @Log( title = "推广设置", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ConfigRecommend configRecommend, HttpServletResponse response ) {
        List<ConfigRecommend> list = configRecommendService.selectConfigRecommendList( configRecommend );
        ExportExcelUtil.exportExcel( list, "推广设置", "推广设置表", ConfigRecommend.class, response );
    }

    /**
     * 获取推广设置详细信息
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<?> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( configRecommendService.getById( id ) );
    }

    /**
     * 新增推广设置
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:add')" )
    @Log( title = "推广设置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigRecommend configRecommend ) {
        boolean isSave = configRecommendService.save( configRecommend );
        if ( isSave ) {
            configRecommendCacheUtils.clear();
        }
        return toResult( isSave );
    }

    /**
     * 修改推广设置
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:edit')" )
    @Log( title = "推广设置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigRecommend configRecommend ) {
        boolean isSave = configRecommendService.updateById( configRecommend );
        if ( isSave ) {
            configRecommendCacheUtils.clear();
        }
        return toResult( isSave );
    }

    /**
     * 删除推广设置
     */
    @PreAuthorize( "@ss.hasPermi('config:recommend:remove')" )
    @Log( title = "推广设置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{id}" )
    public RspBase<?> remove( @PathVariable String id ) {
        boolean isSave = configRecommendService.removeById( id );
        if ( isSave ) {
            configRecommendCacheUtils.clear();
        }
        return toResult( isSave );
    }
}
