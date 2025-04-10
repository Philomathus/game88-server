package tv.game88.admin.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.admin.system.service.ISysDictDataService;
import tv.game88.admin.system.service.ISysDictTypeService;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.entity.SysDictData;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/dict/data" )
public class SysDictDataController extends BaseController {
    @Resource
    private ISysDictDataService dictDataService;
    @Resource
    private ISysDictTypeService dictTypeService;

    @PreAuthorize( "@ss.hasPermi('system:dict:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysDictData>> list( SysDictData dictData ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysDictData> list = dictDataService.selectDictDataList( dictData );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "字典数据", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('system:dict:export')" )
    @GetMapping( "/export" )
    public RspBase<List<SysDictData>> export( SysDictData dictData, HttpServletResponse response ) {
        return RspBase.ok(  dictDataService.selectDictDataList( dictData ) );
//        ExportExcelUtil.exportBigExcel( list, "字典数据", "字典数据表", SysDictData.class, response );
    }

    /**
     * 查询字典数据详细
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:query')" )
    @GetMapping( value = "/{dictCode}" )
    public RspBase<?> getInfo( @PathVariable Long dictCode ) {
        return RspBase.ok( dictDataService.selectDictDataById( dictCode ) );
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping( value = "/type/{dictType}" )
    public RspBase<?> dictType( @PathVariable String dictType ) {
        List<SysDictData> data = dictTypeService.selectDictDataByType( dictType );
        if ( StringUtils.isNull( data ) ) {
            data = new ArrayList<>();
        }
        return RspBase.ok( data );
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:add')" )
    @Log( title = "字典数据", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody SysDictData dict ) {
        dict.setCreateBy( SecurityUtils.getUsername() );
        return toResult( dictDataService.insertDictData( dict ) );
    }

    /**
     * 修改保存字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:edit')" )
    @Log( title = "字典数据", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @Validated @RequestBody SysDictData dict ) {
        dict.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( dictDataService.updateDictData( dict ) );
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:remove')" )
    @Log( title = "字典类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{dictCodes}" )
    public RspBase<?> remove( @PathVariable Long[] dictCodes ) {
        return toResult( dictDataService.deleteDictDataByIds( dictCodes ) );
    }
}
