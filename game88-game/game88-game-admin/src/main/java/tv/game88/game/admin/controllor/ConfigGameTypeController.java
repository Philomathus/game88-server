package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.entity.ConfigGametype;
import tv.game88.game.api.service.ConfigGametypeService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 游戏字典Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping( "/game/configGameType" )
public class ConfigGameTypeController extends BaseController {
    @Resource
    private ConfigGametypeService configGametypeService;

    /**
     * 查询游戏字典列表
     */
    @PreAuthorize( "@ss.hasPermi('game:configGametype:list')" )
    @GetMapping( "/list" )
    public RspBase<List<ConfigGametype>> list( ConfigGametype configGametype ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<ConfigGametype> list = configGametypeService.selectConfigGametypeList( configGametype );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 获取游戏字典详细信息
     */
    @PreAuthorize( "@ss.hasPermi('game:configGametype:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<ConfigGametype> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( configGametypeService.getById( id ) );
    }

    /**
     * 新增游戏字典
     */
    @PreAuthorize( "@ss.hasPermi('game:configGametype:add')" )
    @Log( title = "游戏配置新增", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody ConfigGametype configGametype ) {
        return toResult( configGametypeService.save( configGametype ) );
    }

    /**
     * 修改游戏字典
     */
    @PreAuthorize( "@ss.hasPermi('game:configGametype:edit')" )
    @Log( title = "游戏配置编辑", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody ConfigGametype configGametype ) {

        return toResult( configGametypeService.updateById( configGametype ) );
    }

    /**
     * 删除游戏字典
     */
    @PreAuthorize( "@ss.hasPermi('game:configGametype:remove')" )
    @Log( title = "游戏配置删除", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( configGametypeService.removeBatchByIds( Arrays.asList( ids ) ) );
    }
}