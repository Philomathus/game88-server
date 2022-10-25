package tv.game88.core.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.security.context.PermissionContextHolder;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;

import java.util.Set;

/**
 * 自定义权限实现，ss取自SpringSecurity首字母
 *
 * @author MengJun
 */
@Service( "ss" )
public class PermissionService {
	/**
	 * 所有权限标识
	 */
	private static final String ALL_PERMISSION       = "*:*:*";
	/**
	 * 管理员角色权限标识
	 */
	private static final String SUPER_ADMIN          = "admin";
	private static final String ROLE_DELIMETER       = ",";
	private static final String PERMISSION_DELIMETER = ",";

	/**
	 * 验证用户是否具备某权限
	 *
	 * @param permission 权限字符串
	 * @return 用户是否具备某权限
	 */
	public boolean hasPermi( String permission ) {
		if ( StringUtils.isBlank( permission ) ) {
			return false;
		}
		LoginUser loginUser = SecurityUtils.getLoginUser();
		if ( StringUtils.isNull( loginUser ) || CollectionUtils.isEmpty( loginUser.getUser().getPermissions() ) ) {
			return false;
		}
		PermissionContextHolder.setContext(permission);
		return hasPermissions( loginUser.getUser().getPermissions(), permission );
	}

	/**
	 * 验证用户是否不具备某权限，与 hasPermi逻辑相反
	 *
	 * @param permission 权限字符串
	 * @return 用户是否不具备某权限
	 */
	public boolean lacksPermi( String permission ) {
		return !hasPermi(permission);
	}

	/**
	 * 验证用户是否具有以下任意一个权限
	 *
	 * @param permissions 以 PERMISSION_NAMES_DELIMETER 为分隔符的权限列表
	 * @return 用户是否具有以下任意一个权限
	 */
	public boolean hasAnyPermi( String permissions ) {
		if ( StringUtils.isBlank( permissions ) ) {
			return false;
		}
		LoginUser loginUser = SecurityUtils.getLoginUser();
		if ( StringUtils.isNull( loginUser ) || CollectionUtils.isEmpty( loginUser.getUser().getPermissions() ) ) {
			return false;
		}
		PermissionContextHolder.setContext(permissions);
		Set<String> authorities = loginUser.getUser().getPermissions();
		for ( String permission : permissions.split( PERMISSION_DELIMETER ) ) {
			if ( permission != null && hasPermissions( authorities, permission ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断用户是否拥有某个角色
	 *
	 * @param role 角色字符串
	 * @return 用户是否具备某角色
	 */
	public boolean hasRole( String role ) {
		if ( StringUtils.isBlank( role ) ) {
			return false;
		}
		LoginUser loginUser = SecurityUtils.getLoginUser();
		if ( StringUtils.isNull( loginUser ) || CollectionUtils.isEmpty( loginUser.getUser().getRoles() ) ) {
			return false;
		}
		for ( SysRole sysRole : loginUser.getUser().getRoles() ) {
			String roleKey = sysRole.getRoleKey();
			if ( SUPER_ADMIN.equals( roleKey ) || roleKey.equals( StringUtils.trim( role ) ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 验证用户是否不具备某角色，与 isRole逻辑相反。
	 *
	 * @param role 角色名称
	 * @return 用户是否不具备某角色
	 */
	public boolean lacksRole( String role ) {
		return hasRole( role ) != true;
	}

	/**
	 * 验证用户是否具有以下任意一个角色
	 *
	 * @param roles 以 ROLE_NAMES_DELIMETER 为分隔符的角色列表
	 * @return 用户是否具有以下任意一个角色
	 */
	public boolean hasAnyRoles( String roles ) {
		if ( StringUtils.isBlank( roles ) ) {
			return false;
		}
		LoginUser loginUser = SecurityUtils.getLoginUser();
		if ( StringUtils.isNull( loginUser ) || CollectionUtils.isEmpty( loginUser.getUser().getRoles() ) ) {
			return false;
		}
		for ( String role : roles.split( ROLE_DELIMETER ) ) {
			if ( hasRole( role ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否包含权限
	 *
	 * @param permissions 权限列表
	 * @param permission  权限字符串
	 * @return 用户是否具备某权限
	 */
	private boolean hasPermissions( Set<String> permissions, String permission ) {
		return permissions.contains( ALL_PERMISSION ) || permissions.contains( StringUtils.trim( permission ) );
	}
}
