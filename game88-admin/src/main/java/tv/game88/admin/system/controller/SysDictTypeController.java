package tv.game88.admin.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.admin.system.service.ISysDictTypeService;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysDictType;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 数据字典信息
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/dict/type" )
public class SysDictTypeController extends BaseController {
    @Resource
    private ISysDictTypeService dictTypeService;

    @PreAuthorize( "@ss.hasPermi('system:dict:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysDictType>> list( SysDictType dictType ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysDictType> list = dictTypeService.selectDictTypeList( dictType );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "字典类型", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('system:dict:export')" )
    @GetMapping( "/export" )
    public void export( SysDictType dictType, HttpServletResponse response ) {
        List<SysDictType> list = dictTypeService.selectDictTypeList( dictType );
        ExportExcelUtil.exportExcel( list, "字典信息", "字典信息表", SysDictType.class, response );
    }

    /**
     * 查询字典类型详细
     */
    //@PreAuthorize( "@ss.hasPermi('system:dict:query')" )
    @GetMapping( value = "/{dictId}" )
    public RspBase<?> getInfo( @PathVariable Long dictId ) {
        return RspBase.ok( dictTypeService.selectDictTypeById( dictId ) );
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:add')" )
    @Log( title = "字典类型", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody SysDictType dict ) {
        if ( UserConstants.NOT_UNIQUE.equals( dictTypeService.checkDictTypeUnique( dict ) ) ) {
            return RspBase.businessError( "新增字典" + dict.getDictName() + "失败，字典类型已存在" );
        }
        dict.setCreateBy( SecurityUtils.getUsername() );
        return toResult( dictTypeService.insertDictType( dict ) );
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:edit')" )
    @Log( title = "字典类型", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @Validated @RequestBody SysDictType dict ) {
        if ( UserConstants.NOT_UNIQUE.equals( dictTypeService.checkDictTypeUnique( dict ) ) ) {
            return RspBase.businessError( "修改字典" + dict.getDictName() + "失败，字典类型已存在" );
        }
        dict.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( dictTypeService.updateDictType( dict ) );
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:remove')" )
    @Log( title = "字典类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{dictIds}" )
    public RspBase<?> remove( @PathVariable Long[] dictIds ) {
        return toResult( dictTypeService.deleteDictTypeByIds( dictIds ) );
    }

    /**
     * 清空缓存
     */
    @PreAuthorize( "@ss.hasPermi('system:dict:remove')" )
    @Log( title = "字典类型", businessType = BusinessType.CLEAN )
    @DeleteMapping( "/clearCache" )
    public RspBase<?> clearCache() {
        dictTypeService.clearCache();
        return RspBase.ok();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping( "/optionselect" )
    public RspBase<?> optionselect() {
        List<SysDictType> dictTypes = dictTypeService.selectDictTypeAll();
        return RspBase.ok( dictTypes );
    }
}
