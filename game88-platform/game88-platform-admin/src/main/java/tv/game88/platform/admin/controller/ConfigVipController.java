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
import tv.game88.platform.api.cache.ConfigVipCacheUtils;
import tv.game88.platform.api.entity.ConfigVip;
import tv.game88.platform.api.service.ConfigVipService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 会员VIP配置Controller
 */
@RestController
@RequestMapping( "/config/vip" )
public class ConfigVipController extends BaseController {
    @Resource
    private ConfigVipService    configVipService;
    @Resource
    private ConfigVipCacheUtils configVipCacheUtils;

    /**
     * 查询会员VIP配置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigVip>> list( ConfigVip configVip ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigVip> list = configVipService.selectConfigVipList( configVip );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出会员VIP配置列表
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:export')" )
    @Log( title = "会员VIP配置", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ConfigVip configVip, HttpServletResponse response ) {
        List<ConfigVip> list = configVipService.selectConfigVipList( configVip );
        ExportExcelUtil.exportExcel( list, "会员VIP配置", "会员VIP配置表", ConfigVip.class, response );
    }

    /**
     * 获取会员VIP配置详细信息
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:edit')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigVip> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( configVipService.getById( id ) );
    }

    /**
     * 新增会员VIP配置
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:add')" )
    @Log( title = "会员VIP配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigVip configVip ) {
        boolean isSave = configVipService.save( configVip );
        if ( isSave ) {
            configVipCacheUtils.clear();
        }
        return toResult( isSave );
    }

    /**
     * 修改会员VIP配置
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:edit')" )
    @Log( title = "会员VIP配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigVip configVip ) {
        boolean isSave = configVipService.updateById( configVip );
        if ( isSave ) {
            configVipCacheUtils.clear();
        }
        return toResult( isSave );
    }

    /**
     * 删除会员VIP配置
     */
    @PreAuthorize( "@ss.hasPermi('config:vip:remove')" )
    @Log( title = "会员VIP配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        boolean isSave = configVipService.removeBatchByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            configVipCacheUtils.clear();
        }
        return toResult( isSave );
    }
}