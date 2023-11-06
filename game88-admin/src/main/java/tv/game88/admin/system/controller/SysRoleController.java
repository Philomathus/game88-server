package tv.game88.admin.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.service.ISysRoleService;
import tv.game88.core.admin.utils.SecurityUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 角色信息
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/role" )
public class SysRoleController extends BaseController {
    @Resource
    private ISysRoleService roleService;

    @PreAuthorize( "@ss.hasPermi('system:role:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysRole>> list( SysRole role ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysRole> list = roleService.selectRoleList( role );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "角色管理", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('system:role:export')" )
    @GetMapping( "/export" )
    public void export( SysRole role, HttpServletResponse response ) {
        List<SysRole> list = roleService.selectRoleList( role );
        ExportExcelUtil.exportExcel( list, "角色信息", "角色信息表", SysRole.class, response );
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize( "@ss.hasPermi('system:role:query')" )
    @GetMapping( value = "/{roleId}" )
    public RspBase<SysRole> getInfo( @PathVariable Long roleId ) {
        return RspBase.ok( roleService.selectRoleById( roleId ) );
    }

    /**
     * 新增角色
     */
    @PreAuthorize( "@ss.hasPermi('system:role:add')" )
    @Log( title = "角色管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody SysRole role ) {
        if ( UserConstants.NOT_UNIQUE.equals( roleService.checkRoleNameUnique( role ) ) ) {
            return RspBase.businessError( "新增角色'" + role.getRoleName() + "'失败，角色名称已存在" );
        } else if ( UserConstants.NOT_UNIQUE.equals( roleService.checkRoleKeyUnique( role ) ) ) {
            return RspBase.businessError( "新增角色'" + role.getRoleName() + "'失败，角色权限已存在" );
        }
        role.setCreateBy( SecurityUtils.getUsername() );
        return toResult( roleService.insertRole( role ) );

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize( "@ss.hasPermi('system:role:edit')" )
    @Log( title = "角色管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @Validated @RequestBody SysRole role ) {
        roleService.checkRoleAllowed( role );
        if ( UserConstants.NOT_UNIQUE.equals( roleService.checkRoleNameUnique( role ) ) ) {
            return RspBase.businessError( "修改角色'" + role.getRoleName() + "'失败，角色名称已存在" );
        } else if ( UserConstants.NOT_UNIQUE.equals( roleService.checkRoleKeyUnique( role ) ) ) {
            return RspBase.businessError( "修改角色'" + role.getRoleName() + "'失败，角色权限已存在" );
        }
        role.setUpdateBy( SecurityUtils.getUsername() );

        if ( roleService.updateRole( role ) > 0 ) {
            // TODO 测试阶段先禁用 roleService.userRoleList( role.getRoleId() );//踢蹬，所有角色下的用户踢蹬
            return RspBase.ok();
        }
        return RspBase.businessError( "修改角色'" + role.getRoleName() + "'失败，请联系管理员" );
    }

    /**
     * 修改保存数据权限
     */
    @PreAuthorize( "@ss.hasPermi('system:role:edit')" )
    @Log( title = "角色管理", businessType = BusinessType.UPDATE )
    @PutMapping( "/dataScope" )
    public RspBase<?> dataScope( @RequestBody SysRole role ) {
        roleService.checkRoleAllowed( role );
        return toResult( roleService.updateRole( role ) );
    }

    /**
     * 状态修改
     */
    @PreAuthorize( "@ss.hasPermi('system:role:edit')" )
    @Log( title = "角色管理", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody SysRole role ) {
        roleService.checkRoleAllowed( role );
        role.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( roleService.updateRoleStatus( role ) );
    }

    /**
     * 删除角色
     */
    @PreAuthorize( "@ss.hasPermi('system:role:remove')" )
    @Log( title = "角色管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{roleIds}" )
    public RspBase<?> remove( @PathVariable Long[] roleIds ) {
        return toResult( roleService.deleteRoleByIds( roleIds ) );
    }

    /**
     * 获取角色选择框列表
     */
    @PreAuthorize( "@ss.hasPermi('system:role:query')" )
    @GetMapping( "/optionselect" )
    public RspBase<List<SysRole>> optionselect() {
        return RspBase.ok( roleService.selectRoleAll() );
    }
}
