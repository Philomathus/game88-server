package tv.game88.admin.system.controller;

import org.apache.commons.io.IOUtils;
import tv.game88.admin.system.entity.GenTable;
import tv.game88.admin.system.entity.GenTableColumn;
import tv.game88.admin.system.service.IGenTableColumnService;
import tv.game88.admin.system.service.IGenTableService;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.Convert;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.common.base.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping( "/tool/gen" )
public class SysGenController extends BaseController {
    @Resource
    private IGenTableService       genTableService;
    @Resource
    private IGenTableColumnService genTableColumnService;

    /**
     * 查询代码生成列表
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:list')" )
    @GetMapping( "/list" )
    public RspBase<List<GenTable>> genList( GenTable genTable ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GenTable> list = genTableService.selectGenTableList( genTable );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 修改代码生成业务
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:query')" )
    @GetMapping( value = "/{talbleId}" )
    public RspBase<?> getInfo( @PathVariable Long talbleId ) {
        GenTable             table  = genTableService.selectGenTableById( talbleId );
        List<GenTable>       tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list   = genTableColumnService.selectGenTableColumnListByTableId( talbleId );
        Map<String, Object>  map    = new HashMap<>();
        map.put( "info", table );
        map.put( "rows", list );
        map.put( "tables", tables );
        return RspBase.ok( map );
    }

    /**
     * 查询数据库列表
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:list')" )
    @GetMapping( "/db/list" )
    public RspBase<List<GenTable>> dataList( GenTable genTable ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GenTable> list = genTableService.selectDbTableList( genTable );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询数据表字段列表
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:list')" )
    @GetMapping( value = "/column/{talbleId}" )
    public RspBase<List<GenTableColumn>> columnList( Long tableId ) {
        List<GenTableColumn> list     = genTableColumnService.selectGenTableColumnListByTableId( tableId );
        RspBase<List<GenTableColumn>> rspBase = RspBase.ok( list );
        rspBase.setTotal( ( long ) list.size() );
        return rspBase;
    }

    /**
     * 导入表结构（保存）
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:list')" )
    @Log( title = "代码生成", businessType = BusinessType.IMPORT )
    @PostMapping( "/importTable" )
    public RspBase<?> importTableSave( String tables ) {
        String[] tableNames = Convert.toStrArray( tables );
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames( tableNames );
        genTableService.importGenTable( tableList );
        return RspBase.ok();
    }

    /**
     * 修改保存代码生成业务
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:edit')" )
    @Log( title = "代码生成", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> editSave( @Validated @RequestBody GenTable genTable ) {
        genTableService.validateEdit( genTable );
        genTableService.updateGenTable( genTable );
        return RspBase.ok();
    }

    /**
     * 删除代码生成
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:remove')" )
    @Log( title = "代码生成", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{tableIds}" )
    public RspBase<?> remove( @PathVariable Long[] tableIds ) {
        genTableService.deleteGenTableByIds( tableIds );
        return RspBase.ok();
    }

    /**
     * 预览代码
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:preview')" )
    @GetMapping( "/preview/{tableId}" )
    public RspBase<?> preview( @PathVariable( "tableId" ) Long tableId ) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode( tableId );
        return RspBase.ok( dataMap );
    }

    /**
     * 生成代码（下载方式）
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:code')" )
    @Log( title = "代码生成", businessType = BusinessType.GENCODE )
    @GetMapping( "/download/{tableName}" )
    public void download( HttpServletResponse response, @PathVariable( "tableName" ) String tableName ) throws IOException {
        byte[] data = genTableService.downloadCode( tableName );
        genCode( response, data );
    }

    /**
     * 生成代码（自定义路径）
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:code')" )
    @Log( title = "代码生成", businessType = BusinessType.GENCODE )
    @GetMapping( "/genCode/{tableName}" )
    public RspBase<?> genCode( @PathVariable( "tableName" ) String tableName ) {
        genTableService.generatorCode( tableName );
        return RspBase.ok();
    }

    /**
     * 同步数据库
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:edit')" )
    @Log( title = "代码生成", businessType = BusinessType.UPDATE )
    @GetMapping( "/synchDb/{tableName}" )
    public RspBase<?> synchDb( @PathVariable( "tableName" ) String tableName ) {
        genTableService.synchDb( tableName );
        return RspBase.ok();
    }

    /**
     * 批量生成代码
     */
    @PreAuthorize( "@ss.hasPermi('tool:gen:code')" )
    @Log( title = "代码生成", businessType = BusinessType.GENCODE )
    @GetMapping( "/batchGenCode" )
    public void batchGenCode( HttpServletResponse response, String tables ) throws IOException {
        String[] tableNames = Convert.toStrArray( tables );
        byte[]   data       = genTableService.downloadCode( tableNames );
        genCode( response, data );
    }

    /**
     * 生成zip文件
     */
    private void genCode( HttpServletResponse response, byte[] data ) throws IOException {
        response.reset();
        response.addHeader( "Access-Control-Allow-Origin", "*" );
        response.addHeader( "Access-Control-Expose-Headers", "Content-Disposition" );
        response.setHeader( "Content-Disposition", "attachment; filename=\"common.zip\"" );
        response.addHeader( "Content-Length", "" + data.length );
        response.setContentType( "application/octet-stream; charset=UTF-8" );
        IOUtils.write( data, response.getOutputStream() );
    }
}
