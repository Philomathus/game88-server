package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GameTypeWith;
import tv.game88.game.api.service.GameTypeWithService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 游戏类型Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/game/typeWith" )
public class GameTypeWithController extends BaseController {
    @Resource
    private GameTypeWithService gameTypeWithService;

    /**
     * 查询游戏类型关联列表
     */
    @PreAuthorize( "@ss.hasPermi('game:typeWith:list')" )
    @GetMapping( "/list" )
    public RspBase<List<GameTypeWith>> list( Long typeId ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<GameTypeWith> list = gameTypeWithService.selectGameTypeWithList( typeId );
        return getRspBasePage( list, pageDomain );
    }

    @PreAuthorize( "@ss.hasPermi('game:info:list')" )
    @GetMapping( "/listNotType/{typeId}" )
    public RspBase<List<GameInfo>> listNotType( @PathVariable Long typeId, @RequestParam( required = false ) String name ) {
        return RspBase.ok( gameTypeWithService.selectListNotType( typeId, name ) );
    }

    @PreAuthorize( "@ss.hasPermi('game:typeWith:insert')" )
    @Log( title = "游戏信息ID类型关联", businessType = BusinessType.INSERT )
    @PutMapping
    public RspBase<?> insertTypeWith( @RequestBody GameTypeWith gameTypeWith ) {
        return gameTypeWithService.updateTypeWith( gameTypeWith );
    }

    /**
     * 游戏信息ID类型关联
     */
    @PreAuthorize( "@ss.hasPermi('game:typeWith:edit')" )
    @Log( title = "游戏信息ID类型关联", businessType = BusinessType.UPDATE )
    @PostMapping( "/{typeId}" )
    public RspBase<?> insertTypeWith( @PathVariable Long typeId, @RequestBody List<Long> gameInfoIds ) {
        return gameTypeWithService.insertTypeWith( typeId, gameInfoIds );
    }

    /**
     * 游戏信息ID类型关联
     */
    @PreAuthorize( "@ss.hasPermi('game:typeWith:delete')" )
    @Log( title = "游戏信息ID类型剔除关联", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{typeId}" )
    public RspBase<?> deleteTypeWith( @PathVariable Long typeId, @RequestBody List<Long> gameInfoIds ) {
        return gameTypeWithService.deleteTypeWith( typeId, gameInfoIds );
    }
}