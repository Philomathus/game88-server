package tv.game88.game.admin.controllor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.RspGame;
import tv.game88.game.api.entity.GameType;
import tv.game88.game.api.service.GameTypeService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏类型Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/game/type" )
public class GameTypeController extends BaseController {
    @Resource
    private GameTypeService gameTypeService;
    @Resource
    private GameCacheUtils  gameCacheUtils;

    /**
     * 查询游戏类型列表
     */
    @PreAuthorize( "@ss.hasPermi('game:type:list')" )
    @GetMapping( "/list" )
    public RspBase<List<GameType>> list( GameType gameType ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GameType> list = gameTypeService.selectGameTypeList( gameType );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询游戏类型列表
     */
    @PreAuthorize( "@ss.hasPermi('game:type:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<RspGame>> listAll() {
        List<RspGame> list = gameTypeService.list( new QueryWrapper<GameType>().select( "id", "name", "icon" ) ).stream().map( p -> {
            RspGame rspGame = new RspGame();
            BeanUtils.copyProperties( p, rspGame );
            return rspGame;
        } ).collect( Collectors.toList() );
        return RspBase.ok( list );
    }

    /**
     * 获取游戏类型详细信息
     */
    @PreAuthorize( "@ss.hasPermi('game:type:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<GameType> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( gameTypeService.getById( id ) );
    }

    /**
     * 新增游戏类型
     */
    @PreAuthorize( "@ss.hasPermi('game:type:add')" )
    @Log( title = "游戏类型", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody GameType gameType ) {
        gameType.setEffect( false );
        return toResult( gameTypeService.save( gameType ) );
    }

    /**
     * 修改游戏类型
     */
    @PreAuthorize( "@ss.hasPermi('game:type:edit')" )
    @Log( title = "游戏类型", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody GameType gameType ) {
        gameType.setEffect( null );
        boolean isSave = gameTypeService.updateById( gameType );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_KEY );
        }
        return toResult( isSave );
    }

    /**
     * 删除游戏类型
     */
    @PreAuthorize( "@ss.hasPermi('game:type:remove')" )
    @Log( title = "游戏类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isSave = gameTypeService.removeByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_KEY );
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:type:effect')" )
    @Log( title = "游戏类型激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeEffect/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        GameType update = new GameType();
        update.setId( id );
        update.setEffect( effect );
        boolean isSave = gameTypeService.updateById( update );
        if ( isSave ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_KEY );
        }
        return toResult( isSave );
    }
}