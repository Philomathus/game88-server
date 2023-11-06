package tv.game88.core.admin.security.service;

import com.google.common.collect.Sets;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.service.ISysMenuService;
import tv.game88.core.admin.service.ISysRoleService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author MengJun
 */
@Component
public class SysPermissionService {
    @Resource
    private ISysRoleService roleService;

    @Resource
    private ISysMenuService menuService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     *
     * @return 角色权限信息
     */
    public Set<String> getRolePermission( SysUser user ) {
        Set<String> roles = new HashSet<>();
        // 管理员拥有所有权限
        if ( user.isAdmin() ) {
            roles.add( "admin" );
        } else {
            roles.addAll( roleService.selectRolePermissionByUserId( user.getUserId() ) );
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param user 用户信息
     *
     * @return 菜单权限信息
     */
    public void getMenuPermission( SysUser user ) {
        // 管理员拥有所有权限
        if ( user.isAdmin() ) {
            user.setPermissions( Sets.newHashSet( "*:*:*" ) );
        } else {
            user.setPermissions( menuService.selectMenuPermsByUserId( user.getUserId() ) );
        }
    }
}
