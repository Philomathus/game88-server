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
import tv.game88.game.api.entity.ConfigWashCode;
import tv.game88.game.api.service.ConfigWashCodeService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping( "/game/configWashCode" )
public class ConfigWashCodeController extends BaseController {

    @Resource
    private ConfigWashCodeService configWashCodeService;

    @Resource
    private GameCacheUtils gameCacheUtils;

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigWashCode>> list( ConfigWashCode configWashCode ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigWashCode> list = configWashCodeService.selectConfigWashCodeList( configWashCode );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigWashCode> getInfo( @PathVariable( "id" ) Integer id ) {
        return RspBase.ok( configWashCodeService.getById( id ) );
    }

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:add')" )
    @Log( title = "洗码配置", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigWashCode configWashCode ) {
        configWashCode.setEffect( false );
        boolean save = configWashCodeService.save( configWashCode );
        if ( save ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_WASH_CODE_CONFIG_LIST_KEY );
        }
        return toResult( save );
    }

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:edit')" )
    @Log( title = "洗码配置", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigWashCode configWashCode ) {
        configWashCode.setEffect( null );
        boolean isSave = configWashCodeService.updateById( configWashCode );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_WASH_CODE_CONFIG_LIST_KEY );
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:remove')" )
    @Log( title = "洗码配置", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Integer[] ids ) {
        boolean isSave = configWashCodeService.removeByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_WASH_CODE_CONFIG_LIST_KEY );
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:configWashCode:effect')" )
    @Log( title = "洗码配置激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeEffect/{id}/{effect}" )
    public RspBase<?> changeEffect( @PathVariable Integer id, @PathVariable Boolean effect ) {
        ConfigWashCode update = new ConfigWashCode();
        update.setId( id );
        update.setEffect( effect );
        boolean isSave = configWashCodeService.updateById( update );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_WASH_CODE_CONFIG_LIST_KEY );
        }
        return toResult( isSave );
    }

    @Log( title = "洗码配置", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('game:configWashCode:export')" )
    @GetMapping( "/export" )
    public void export( ConfigWashCode configWashCode, HttpServletResponse response ) {
        List<ConfigWashCode> list = configWashCodeService.selectConfigWashCodeList( configWashCode );
        ExportExcelUtil.exportExcel( list, "洗码配置信息", "洗码配置信息表", ConfigWashCode.class, response );
    }
}
