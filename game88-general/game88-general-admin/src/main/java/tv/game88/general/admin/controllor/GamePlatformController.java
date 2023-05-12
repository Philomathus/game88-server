package tv.game88.general.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.game.dto.RspGameCategory;
import tv.game88.core.game.type.EnumGameCategory;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.service.GamePlatformService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 游戏平台Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/game/platform" )
public class GamePlatformController extends BaseController {
    @Resource
    private GamePlatformService gamePlatformService;

    /**
     * 查询游戏平台列表
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:list')" )
    @GetMapping( "/list" )
    public RspBase<List<GamePlatform>> list( GamePlatform gamePlatform ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GamePlatform> list = gamePlatformService.selectGamePlatformList( gamePlatform );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询游戏类别列表
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:list')" )
    @GetMapping( "/gameCategoryAll" )
    public RspBase<List<RspGameCategory>> gameCategoryAll() {
        return RspBase.ok( EnumGameCategory.getGameCategorys() );
    }

    /**
     * 获取游戏平台详细信息
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<GamePlatform> getInfo( @PathVariable( "id" ) Long id ) {
        GamePlatform gamePlatform = gamePlatformService.getById( id );
        if ( gamePlatform != null ) {
            String a = "**********";
            if ( StringUtils.isNotBlank( gamePlatform.getDes() ) ) {
                gamePlatform.setDes( a );
            }
            if ( StringUtils.isNotBlank( gamePlatform.getMd5() ) ) {
                gamePlatform.setMd5( a );
            }
        }
        return RspBase.ok( gamePlatform );
    }

    /**
     * 新增游戏平台
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:add')" )
    @Log( title = "游戏平台", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody GamePlatform gamePlatform ) {
        gamePlatform.setCreateBy( SecurityUtils.getUsername() );
        gamePlatform.setCreateTime( LocalDateTime.now() );
        gamePlatform.setEffect( false );
        gamePlatform.setMaintain( false );
        if ( StringUtils.isNotBlank( gamePlatform.getDes() ) ) {
            gamePlatform.setDes( AESCoder.encrypt( gamePlatform.getDes() ) );
        }
        if ( StringUtils.isNotBlank( gamePlatform.getMd5() ) ) {
            gamePlatform.setMd5( AESCoder.encrypt( gamePlatform.getMd5() ) );
        }
        return toResult( gamePlatformService.save( gamePlatform ) );
    }

    /**
     * 修改游戏平台
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:edit')" )
    @Log( title = "游戏平台", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody GamePlatform gamePlatform ) {
        gamePlatform.setUpdateBy( SecurityUtils.getUsername() );
        gamePlatform.setUpdateTime( LocalDateTime.now() );
        gamePlatform.setEffect( null );
        gamePlatform.setMaintain( null );

        GamePlatform gamePlatformOld = gamePlatformService.getById( gamePlatform.getId() );
        String       a               = "***";
        if ( StringUtils.isNotBlank( gamePlatform.getDes() ) ) {
            if ( gamePlatform.getDes().contains( a ) ) {
                gamePlatform.setDes( gamePlatformOld.getDes() );
            } else {
                gamePlatform.setDes( AESCoder.encrypt( gamePlatform.getDes() ) );
            }
        }
        if ( StringUtils.isNotBlank( gamePlatform.getMd5() ) ) {
            if ( gamePlatform.getMd5().contains( a ) ) {
                gamePlatform.setMd5( gamePlatformOld.getMd5() );
            } else {
                gamePlatform.setMd5( AESCoder.encrypt( gamePlatform.getMd5() ) );
            }
        }
        boolean isSave = gamePlatformService.updateById( gamePlatform );
        return toResult( isSave );
    }

    /**
     * 删除游戏平台
     */
    @PreAuthorize( "@ss.hasPermi('game:platform:remove')" )
    @Log( title = "游戏平台", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isSave = gamePlatformService.removeByIds( Arrays.asList( ids ) );
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:platform:effect')" )
    @Log( title = "游戏平台激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeEffect/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        GamePlatform update = new GamePlatform();
        update.setId( id );
        update.setEffect( effect );
        boolean isSave = gamePlatformService.updateById( update );
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:platform:effect')" )
    @Log( title = "游戏平台维护状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeMaintain/{id}/{maintain}" )
    public RspBase<?> changeMaintain( @PathVariable Long id, @PathVariable Boolean maintain ) {
        GamePlatform update = new GamePlatform();
        update.setId( id );
        update.setMaintain( maintain );
        boolean isSave = gamePlatformService.updateById( update );
        return toResult( isSave );
    }
}