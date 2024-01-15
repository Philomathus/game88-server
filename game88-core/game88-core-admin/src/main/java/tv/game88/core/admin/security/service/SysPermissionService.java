package tv.game88.core.admin.security.service;

import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.mapper.SysMenuMapper;
import tv.game88.core.admin.mapper.SysRoleMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author MengJun
 */
@Component
public class SysPermissionService {
    @Resource
    private SysRoleMapper   sysRoleMapper;

    @Resource
    private SysMenuMapper   sysMenuMapper;

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
            List<SysRole> perms    = sysRoleMapper.selectRolePermissionByUserId( user.getUserId() );
            Set<String>   permsSet = new HashSet<>();
            for ( SysRole perm : perms ) {
                permsSet.addAll( Arrays.asList( perm.getRoleKey().trim().split( "," ) ) );
            }
            roles.addAll( permsSet );
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
            List<String> perms    = sysMenuMapper.selectMenuPermsByUserId( user.getUserId() );
            Set<String>  permsSet = new HashSet<>();
            for ( String perm : perms ) {
                if ( StringUtils.isNotBlank( perm ) ) {
                    permsSet.addAll( Arrays.asList( perm.trim().split( "," ) ) );
                }
            }
            user.setPermissions( permsSet );
        }
    }
}
