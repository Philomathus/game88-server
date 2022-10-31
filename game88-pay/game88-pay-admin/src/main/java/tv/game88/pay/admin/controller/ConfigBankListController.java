package tv.game88.pay.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.pay.api.entity.ConfigBankList;
import tv.game88.pay.api.service.ConfigBankListService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
     * 导出银行字典列表列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:export')" )
    @Log( title = "银行字典列表", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ConfigBankList configBankList, HttpServletResponse response ) {
        List<ConfigBankList> list = configBankListService.selectConfigBankListList( configBankList );
        ExportExcelUtil.exportExcel( list, "银行字典列表", "银行字典列表", ConfigBankList.class, response );
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
     * 出款银行状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:edit')" )
    @Log( title = "出款银行状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable( "id" ) Long id, @PathVariable( "effect" ) boolean effect ) {
        ConfigBankList update = new ConfigBankList();
        update.setId( id );
        update.setEffect( effect );
        return RspBase.ok( configBankListService.updateById( update ) );
    }

    /**
     * 新增银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:add')" )
    @Log( title = "银行字典列表", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigBankList configBankList, @RequestParam( "file" ) MultipartFile file ) {
        configBankList.setEffect( false );
        return RspBase.ok( configBankListService.save( configBankList ) );
    }

    /**
     * 修改银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:edit')" )
    @Log( title = "银行字典列表", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigBankList configBankList, @RequestParam( "file" ) MultipartFile file ) {
        return RspBase.ok( configBankListService.updateById( configBankList ) );
    }

    /**
     * 删除银行字典列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:configBankList:remove')" )
    @Log( title = "银行字典列表", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        return RspBase.ok( configBankListService.removeByIds( Arrays.asList( ids ) ) );
    }
}
