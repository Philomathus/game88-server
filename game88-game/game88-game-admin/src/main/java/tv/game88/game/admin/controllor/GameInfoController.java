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
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.service.GameInfoService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/game/info" )
public class GameInfoController extends BaseController {
    @Resource
    private GameInfoService gameInfoService;
    @Resource
    private GameCacheUtils  gameCacheUtils;

    /**
     * 查询游戏信息列表
     */
    @PreAuthorize( "@ss.hasPermi('game:info:list')" )
    @GetMapping( "/list" )
    public RspBase<List<GameInfo>> list( GameInfo gameInfo ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GameInfo> list = gameInfoService.selectGameInfoList( gameInfo );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询游戏信息列表
     */
    @PreAuthorize( "@ss.hasPermi('game:info:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<RspGame>> listAll( @RequestParam( required = false ) Long platformId ) {
        QueryWrapper<GameInfo> select = new QueryWrapper<GameInfo>().select( "id", "name", "icon" );
        if ( platformId != null ) {
            select.eq( "platform_id", platformId );
        }
        List<RspGame> list = gameInfoService.list( select ).stream().map( p -> {
            RspGame rspGame = new RspGame();
            BeanUtils.copyProperties( p, rspGame );
            return rspGame;
        } ).collect( Collectors.toList() );
        return RspBase.ok( list );
    }

    /**
     * 获取游戏信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('game:info:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<GameInfo> getInfo( @PathVariable( "id" ) Long id ) {
        return RspBase.ok( gameInfoService.getById( id ) );
    }

    /**
     * 新增游戏信息
     */
    @PreAuthorize( "@ss.hasPermi('game:info:add')" )
    @Log( title = "游戏信息", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody GameInfo gameInfo ) {
        gameInfo.setCreateTime( LocalDateTime.now() );
        gameInfo.setEffect( false );
        gameInfo.setRecommend( false );
        gameInfo.setMaintain( false );
        return toResult( gameInfoService.save( gameInfo ) );
    }

    /**
     * 修改游戏信息
     */
    @PreAuthorize( "@ss.hasPermi('game:info:edit')" )
    @Log( title = "游戏信息", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody GameInfo gameInfo ) {
        gameInfo.setEffect( null );
        gameInfo.setRecommend( null );
        gameInfo.setMaintain( null );
        boolean isSave = gameInfoService.updateById( gameInfo );
        if ( isSave ) {
            gameCacheUtils.clearByInfoId( gameInfo.getId() );
        }
        return toResult( isSave );
    }

    /**
     * 删除游戏信息
     */
    @PreAuthorize( "@ss.hasPermi('game:info:remove')" )
    @Log( title = "游戏信息", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable Long[] ids ) {
        boolean isSave = gameInfoService.removeByIds( Arrays.asList( ids ) );
        if ( isSave ) {
            for ( Long id : ids ) {
                gameCacheUtils.clearByInfoId( id );
            }
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:info:effect')" )
    @Log( title = "游戏信息激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeEffect/{id}/{effect}" )
    public RspBase<?> changeEffect( @PathVariable Long id, @PathVariable Boolean effect ) {
        GameInfo update = new GameInfo();
        update.setId( id );
        update.setEffect( effect );
        boolean isSave = gameInfoService.updateById( update );
        if ( isSave ) {
            gameCacheUtils.clearByInfoId( id );
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:info:effect')" )
    @Log( title = "游戏信息维护状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeMaintain/{id}/{maintain}" )
    public RspBase<?> changeMaintain( @PathVariable Long id, @PathVariable Boolean maintain ) {
        GameInfo update = new GameInfo();
        update.setId( id );
        update.setMaintain( maintain );
        boolean isSave = gameInfoService.updateById( update );
        if ( isSave ) {
            gameCacheUtils.clearByInfoId( id );
        }
        return toResult( isSave );
    }

    @PreAuthorize( "@ss.hasPermi('game:info:effect')" )
    @Log( title = "游戏信息推荐状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeRecommend/{id}/{recommend}" )
    public RspBase<?> changeRecommend( @PathVariable Long id, @PathVariable Boolean recommend ) {
        GameInfo update = new GameInfo();
        update.setId( id );
        update.setRecommend( recommend );
        boolean isSave = gameInfoService.updateById( update );
        if ( isSave ) {
            gameCacheUtils.clearByInfoId( id );
        }
        return toResult( isSave );
    }
}