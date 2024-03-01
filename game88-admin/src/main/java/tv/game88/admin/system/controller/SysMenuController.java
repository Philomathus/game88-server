package tv.game88.admin.system.controller;

import com.google.common.collect.ImmutableMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysMenu;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.admin.system.service.ISysMenuService;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;
import tv.game88.core.admin.vo.TreeSelect;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 菜单信息
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/menu" )
public class SysMenuController extends BaseController {
    @Resource
    private ISysMenuService menuService;

    /**
     * 获取菜单列表
     */
    @PreAuthorize( "@ss.hasPermi('system:menu:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysMenu>> list( SysMenu menu ) {
        List<SysMenu> menus = menuService.selectMenuList( menu, SecurityUtils.getUserId() );
        return RspBase.ok( menus );
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize( "@ss.hasPermi('system:menu:query')" )
    @GetMapping( value = "/{menuId}" )
    public RspBase<SysMenu> getInfo( @PathVariable Long menuId ) {
        return RspBase.ok( menuService.selectMenuById( menuId ) );
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping( "/treeselect" )
    public RspBase<List<TreeSelect>> treeselect( SysMenu menu ) {
        LoginUser     loginUser = SecurityUtils.getLoginUser();
        Long          userId    = loginUser.getUser().getUserId();
        List<SysMenu> menus     = menuService.selectMenuList( menu, userId );
        return RspBase.ok( menuService.buildMenuTreeSelect( menus ) );
    }

    /**
     * 加载对应角色菜单列表树
     */
    @GetMapping( value = "/roleMenuTreeselect/{roleId}" )
    public Map<String, Object> roleMenuTreeselect( @PathVariable( "roleId" ) Long roleId ) {
        LoginUser     loginUser = SecurityUtils.getLoginUser();
        List<SysMenu> menus     = menuService.selectMenuList( loginUser.getUser().getUserId() );
        return ImmutableMap.of( "code", HttpStatus.SUCCESS, "checkedKeys", menuService.selectMenuListByRoleId( roleId ), "menus"
                , menuService.buildMenuTreeSelect( menus ) );
    }

    /**
     * 新增菜单
     */
    @PreAuthorize( "@ss.hasPermi('system:menu:add')" )
    @Log( title = "菜单管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody SysMenu menu ) {
        if ( UserConstants.NOT_UNIQUE.equals( menuService.checkMenuNameUnique( menu ) ) ) {
            return RspBase.businessError( "新增菜单" + menu.getMenuName() + "失败，菜单名称已存在" );
        } else if ( UserConstants.YES_FRAME.equals( menu.getIsFrame() )
                && !StringUtils.startsWithAny( menu.getPath(), AdminConstants.HTTP, AdminConstants.HTTPS ) ) {
            return RspBase.businessError( "新增菜单" + menu.getMenuName() + "失败，地址必须以http(s)://开头" );
        }
        menu.setCreateBy( SecurityUtils.getUsername() );
        menu.setCreateTime( LocalDateTime.now() );
        return toResult( menuService.insertMenu( menu ) );
    }

    /**
     * 修改菜单
     */
    @PreAuthorize( "@ss.hasPermi('system:menu:edit')" )
    @Log( title = "菜单管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @Validated @RequestBody SysMenu menu ) {
        if ( UserConstants.NOT_UNIQUE.equals( menuService.checkMenuNameUnique( menu ) ) ) {
            return RspBase.businessError( "修改菜单" + menu.getMenuName() + "失败，菜单名称已存在" );
        } else if ( UserConstants.YES_FRAME.equals( menu.getIsFrame() )
                && !StringUtils.startsWithAny( menu.getPath(), AdminConstants.HTTP, AdminConstants.HTTPS ) ) {
            return RspBase.businessError( "修改菜单" + menu.getMenuName() + "失败，地址必须以http(s)://开头" );
        } else if ( menu.getMenuId().equals( menu.getParentId() ) ) {
            return RspBase.businessError( "修改菜单" + menu.getMenuName() + "失败，上级菜单不能选择自己" );
        }
        menu.setUpdateBy( SecurityUtils.getUsername() );
        menu.setUpdateTime( LocalDateTime.now() );
        return toResult( menuService.updateMenu( menu ) );
    }

    /**
     * 删除菜单
     */
    @PreAuthorize( "@ss.hasPermi('system:menu:remove')" )
    @Log( title = "菜单管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{menuId}" )
    public RspBase<?> remove( @PathVariable( "menuId" ) Long menuId ) {
        if ( menuService.hasChildByMenuId( menuId ) ) {
            return RspBase.businessError( "存在子菜单,不允许删除" );
        }
        if ( menuService.checkMenuExistRole( menuId ) ) {
            return RspBase.businessError( "菜单已分配,不允许删除" );
        }
        return toResult( menuService.removeById( menuId ) );
    }
}
