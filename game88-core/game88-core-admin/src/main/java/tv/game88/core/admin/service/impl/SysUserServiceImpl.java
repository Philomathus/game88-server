package tv.game88.core.admin.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.annotation.DataScope;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.entity.SysUserRole;
import tv.game88.core.admin.mapper.SysRoleMapper;
import tv.game88.core.admin.mapper.SysUserMapper;
import tv.game88.core.admin.mapper.SysUserRoleMapper;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.service.ISysUserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户 业务层处理
 *
 * @author MengJun
 */
@Log4j2
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Resource
    private SysUserMapper     userMapper;
    @Resource
    private SysRoleMapper     roleMapper;
    @Resource
    private SysUserRoleMapper   userRoleMapper;
    @Resource
    private SysUserTokenService sysUserTokenService;

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     *
     * @return 用户信息集合信息
     */
    @Override
    @DataScope( userAlias = "u" )
    public List<SysUser> selectUserList( SysUser user ) {
        return userMapper.selectUserList( user );
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     *
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName( String userName ) {
        return userMapper.selectUserByUserName( userName );
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     *
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserById( Long userId ) {
        return userMapper.selectUserById( userId );
    }

    @Override
    public SysUser selectOtpSecretByUserName( String userName ) {
        return userMapper.selectOtpSecretByUserName( userName );
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     *
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup( String userName ) {
        List<SysRole> list   = roleMapper.selectRolesByUserName( userName );
        StringBuilder idsStr = new StringBuilder();
        for ( SysRole role : list ) {
            idsStr.append( role.getRoleName() ).append( "," );
        }
        if ( StringUtils.isNotBlank( idsStr.toString() ) ) {
            return idsStr.substring( 0, idsStr.length() - 1 );
        }
        return idsStr.toString();
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param userName 用户名称
     *
     * @return 结果
     */
    @Override
    public String checkUserNameUnique( String userName ) {
        int count = userMapper.checkUserNameUnique( userName );
        if ( count > 0 ) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


    /**
     * 校验用户是否允许操作
     *
     * @param user 用户信息
     */
    @Override
    public void checkUserAllowed( SysUser user ) {
        if ( StringUtils.isNotNull( user.getUserId() ) && user.isAdmin() ) {
            throw new BusinessException( "不允许操作超级管理员角色" );
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     *
     * @return 结果
     */
    @Override
    @Transactional( rollbackFor = Exception.class )
    public int insertUser( SysUser user ) {
        // 新增用户信息
        int rows = userMapper.insertUser( user );
        // 新增用户与角色管理
        insertUserRole( user );
        return rows;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     *
     * @return 结果
     */
    @Override
    @Transactional( rollbackFor = Exception.class )
    public int updateUser( SysUser user ) {
        Long userId = user.getUserId();
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId( userId );
        // 新增用户与角色管理
        insertUserRole( user );
        int i = userMapper.updateUser( user );
        sysUserTokenService.delToken( userId );

        return i;
    }

    /**
     * 修改用户状态
     *
     * @param user 用户信息
     *
     * @return 结果
     */
    @Override
    public int updateUserStatus( SysUser user ) {
        return userMapper.updateUser( user );
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     *
     * @return 结果
     */
    @Override
    public int updateUserProfile( SysUser user ) {
        return userMapper.updateUser( user );
    }

    /**
     * 修改用户头像
     *
     * @param userName 用户名
     * @param avatar   头像地址
     *
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar( String userName, String avatar ) {
        return userMapper.updateUserAvatar( userName, avatar ) > 0;
    }

    @Override
    public boolean updateUserLoginTime( SysUser user ) {
        return userMapper.updateUserLoginTime( user ) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param user 用户信息
     *
     * @return 结果
     */
    @Override
    public int resetPwd( SysUser user ) {
        return userMapper.updateUser( user );
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     *
     * @return 结果
     */
    @Override
    public int resetUserPwd( String userName, String password ) {
        return userMapper.resetUserPwd( userName, password );
    }

    /**
     * 新增用户角色信息
     *
     * @param user 用户对象
     */
    public void insertUserRole( SysUser user ) {
        Long[] roles = user.getRoleIds();
        if ( StringUtils.isNotNull( roles ) ) {
            // 新增用户与角色管理
            List<SysUserRole> list = new ArrayList<SysUserRole>();
            for ( Long roleId : roles ) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId( user.getUserId() );
                ur.setRoleId( roleId );
                list.add( ur );
            }
            if ( list.size() > 0 ) {
                userRoleMapper.batchUserRole( list );
            }
        }
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     *
     * @return 结果
     */
    @Override
    @Transactional( rollbackFor = Exception.class )
    public int deleteUserById( Long userId ) {
        // 删除用户与角色关联
        userRoleMapper.deleteUserRoleByUserId( userId );
        return userMapper.deleteUserById( userId );
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     *
     * @return 结果
     */
    @Override
    @Transactional( rollbackFor = Exception.class )
    public int deleteUserByIds( Long[] userIds ) {
        for ( Long userId : userIds ) {
            checkUserAllowed( new SysUser( userId ) );
        }
        // 删除用户与角色关联
        userRoleMapper.deleteUserRole( userIds );
        return userMapper.deleteUserByIds( userIds );
    }

}
