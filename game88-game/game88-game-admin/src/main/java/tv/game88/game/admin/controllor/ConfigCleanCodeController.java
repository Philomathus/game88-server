package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.entity.ConfigCleanCode;
import tv.game88.game.api.service.ConfigCleanCodeService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping( "/game/configCleanCode" )
public class ConfigCleanCodeController extends BaseController {

    @Resource
    private ConfigCleanCodeService configCleanCodeService;

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigCleanCode>> list(ConfigCleanCode configCleanCode ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigCleanCode> list = configCleanCodeService.selectConfigCleanCodeList( configCleanCode );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigCleanCode> getInfo( @PathVariable( "id" ) Integer id ) {
        return RspBase.ok( configCleanCodeService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:add')" )
    @Log( title = "洗码配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigCleanCode configCleanCode ) {
        configCleanCode.setEffect( false );
        return toResult( configCleanCodeService.save( configCleanCode ) );
    }

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:edit')" )
    @Log( title = "洗码配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigCleanCode configCleanCode ) {
        configCleanCode.setEffect( null );
        boolean isSave = configCleanCodeService.updateById( configCleanCode );
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:remove')" )
    @Log( title = "洗码配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Integer[] ids ) {
        boolean isSave = configCleanCodeService.removeByIds( Arrays.asList( ids ) );
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:effect')" )
    @Log( title = "洗码配置激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeEffect/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Integer id, @PathVariable Boolean effect ) {
        ConfigCleanCode update = new ConfigCleanCode();
        update.setId( id );
        update.setEffect( effect );
        boolean isSave = configCleanCodeService.updateById( update );
        return toResult( isSave );
    }

    @Log( title = "洗码配置", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('game:configCleanCode:export')" )
    @GetMapping( "/export" )
    public void export( ConfigCleanCode configCleanCode, HttpServletResponse response ) {
        List<ConfigCleanCode> list = configCleanCodeService.selectConfigCleanCodeList( configCleanCode );
        ExportExcelUtil.exportExcel( list, "洗码配置信息", "洗码配置信息表", ConfigCleanCode.class, response );
    }
}
