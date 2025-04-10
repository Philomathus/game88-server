package tv.game88.core.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.service.ConfigBankListService;
import tv.game88.core.config.cache.ConfigBankListCache;
import tv.game88.core.config.entity.ConfigBankList;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 银行字典列表Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/configBankList" )
public class ConfigBankListController extends BaseController {
    @Resource
    private ConfigBankListService configBankListService;
    @Resource
    private ConfigBankListCache   configBankListCache;

    /**
     * 查询银行字典列表列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigBankList>> list( ConfigBankList configBankList ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigBankList> list = configBankListService.selectConfigBankListList( configBankList );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询银行字典列表列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<ConfigBankList>> listAll() {
        return RspBase.ok( configBankListService.list() );
    }

    /**
     * 导出银行字典列表列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:export')" )
    @Log( title = "银行字典列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<ConfigBankList>> export(ConfigBankList configBankList, HttpServletResponse response ) {
        List<ConfigBankList> list = configBankListService.selectConfigBankListList( configBankList );
//        ExportExcelUtil.exportBigExcel( list, "银行字典列表", "银行字典列表", ConfigBankList.class, response );
        return RspBase.ok( list );
    }

    /**
     * 获取银行字典列表详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<?> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( configBankListService.getById( id ) );
    }

    /**
     * 银行字典状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:effect')" )
    @Log( title = "银行字典状态激活", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable( "id" ) Long id, @PathVariable( "effect" ) boolean effect ) {
        ConfigBankList update = new ConfigBankList();
        update.setId( id );
        update.setEffect( effect );
        boolean success = configBankListService.updateById( update );
        if ( success ) {
            configBankListCache.clear();
        }
        return RspBase.ok( success );
    }

    /**
     * 新增银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:add')" )
    @Log( title = "银行字典列表", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigBankList configBankList ) {
        configBankList.setEffect( false );
        return RspBase.ok( configBankListService.save( configBankList ) );
    }

    /**
     * 修改银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:edit')" )
    @Log( title = "银行字典列表", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigBankList configBankList ) {
        boolean data = configBankListService.updateById( configBankList );
        if ( data ) {
            configBankListCache.clear();
        }
        return RspBase.ok( data );
    }

    /**
     * 删除银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:remove')" )
    @Log( title = "银行字典列表", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean data = configBankListService.removeByIds( Arrays.asList( ids ) );
        if ( data ) {
            configBankListCache.clear();
        }
        return RspBase.ok( data );
    }
}
