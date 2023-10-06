package tv.game88.core.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.annotation.DataScope;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.entity.SysRoleMenu;
import tv.game88.core.admin.entity.SysUserRole;
import tv.game88.core.admin.mapper.SysRoleMapper;
import tv.game88.core.admin.mapper.SysRoleMenuMapper;
import tv.game88.core.admin.mapper.SysUserRoleMapper;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.service.ISysRoleService;

import javax.annotation.Resource;
import java.util.*;

/**
 * 角色 业务层处理
 *
 * @author MengJun
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService {
    @Resource
    private SysRoleMapper     roleMapper;
    @Resource
    private SysRoleMenuMapper roleMenuMapper;
    @Resource
    private SysUserRoleMapper   userRoleMapper;
    @Resource
    private SysUserTokenService sysUserTokenService;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    @DataScope( userAlias = "u" )
    public List<SysRole> selectRoleList( SysRole role ) {
        return roleMapper.selectRoleList( role );
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId( Long userId ) {
        List<SysRole> perms    = roleMapper.selectRolePermissionByUserId( userId );
        Set<String>   permsSet = new HashSet<>();
        for ( SysRole perm : perms ) {
            if ( Objects.nonNull( perm ) ) {
                permsSet.addAll( Arrays.asList( perm.getRoleKey().trim().split( "," ) ) );
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll() {
        return SpringUtils.getAopProxy( this ).selectRoleList( new SysRole() );
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Integer> selectRoleListByUserId( Long userId ) {
        return roleMapper.selectRoleListByUserId( userId );
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById( Long roleId ) {
        return roleMapper.selectRoleById( roleId );
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleNameUnique( SysRole role ) {
        long    roleId = StringUtils.isNull( role.getRoleId() ) ? -1L : role.getRoleId();
        SysRole info   = roleMapper.checkRoleNameUnique( role.getRoleName() );
        if ( StringUtils.isNotNull( info ) && info.getRoleId() != roleId ) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public String checkRoleKeyUnique( SysRole role ) {
        long    roleId = StringUtils.isNull( role.getRoleId() ) ? -1L : role.getRoleId();
        SysRole info   = roleMapper.checkRoleKeyUnique( role.getRoleKey() );
        if ( StringUtils.isNotNull( info ) && info.getRoleId() != roleId ) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed( SysRole role ) {
        if ( StringUtils.isNotNull( role.getRoleId() ) && role.isAdmin() ) {
            throw new BusinessException( "不允许操作超级管理员角色" );
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId( Long roleId ) {
        return userRoleMapper.countUserRoleByRoleId( roleId );
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole( SysRole role ) {
        // 新增角色信息
        roleMapper.insertRole( role );
        return insertRoleMenu( role );
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole( SysRole role ) {
        // 修改角色信息
        roleMapper.updateRole( role );
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId( role.getRoleId() );
        return insertRoleMenu( role );
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus( SysRole role ) {
        return roleMapper.updateRole( role );
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu( SysRole role ) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for ( Long menuId : role.getMenuIds() ) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId( role.getRoleId() );
            rm.setMenuId( menuId );
            list.add( rm );
        }
        if ( !list.isEmpty() ) {
            rows = roleMenuMapper.batchRoleMenu( list );
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleById( Long roleId ) {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId( roleId );
        return roleMapper.deleteRoleById( roleId );
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleByIds( Long[] roleIds ) {
        for ( Long roleId : roleIds ) {
            checkRoleAllowed( new SysRole( roleId ) );
            SysRole role = selectRoleById( roleId );
            if ( countUserRoleByRoleId( roleId ) > 0 ) {
                throw new BusinessException( String.format( "%1$s已分配,不能删除", role.getRoleName() ) );
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu( roleIds );
        return roleMapper.deleteRoleByIds( roleIds );
    }

    @Override
    public void userRoleList( Long roleId ) {
        List<SysUserRole> sysUserRolesList = userRoleMapper.userRoleList( roleId );
        for ( SysUserRole user : sysUserRolesList ) {
            Long userId1 = user.getUserId();
            sysUserTokenService.delToken( userId1 );
        }
    }
}
