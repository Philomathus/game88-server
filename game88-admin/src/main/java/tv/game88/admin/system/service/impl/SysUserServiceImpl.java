package tv.game88.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import tv.game88.admin.system.service.ISysUserService;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户 业务层处理
 *
 * @author MengJun
 */
@Log4j2
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Resource
    private SysRoleMapper       roleMapper;
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
        return this.baseMapper.selectUserList( user );
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
        return this.baseMapper.selectUserById( userId );
    }

    @Override
    public SysUser selectOtpSecretByUserName( String userName ) {
        return this.baseMapper.selectOtpSecretByUserName( userName );
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
        int count = this.baseMapper.checkUserNameUnique( userName );
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
        user.setCreateTime( LocalDateTime.now() );
        // 新增用户信息
        int rows = this.baseMapper.insertUser( user );
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
        int i = this.baseMapper.updateUser( user );
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
        return this.baseMapper.updateUser( user );
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
        return this.baseMapper.updateUser( user );
    }

    @Override
    public boolean updateUserLoginTime( SysUser user ) {
        return this.baseMapper.updateUserLoginTime( user ) > 0;
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
        return this.baseMapper.updateUser( user );
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
        return this.baseMapper.resetUserPwd( userName, password );
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
            List<SysUserRole> list = new ArrayList<>();
            for ( Long roleId : roles ) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId( user.getUserId() );
                ur.setRoleId( roleId );
                list.add( ur );
            }
            if ( !list.isEmpty() ) {
                userRoleMapper.batchUserRole( list );
            }
        }
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
        return this.baseMapper.deleteUserByIds( userIds );
    }

    @Override
    public void updateOtpSecret( SysUser sysUser ) {
        this.baseMapper.updateOtpSecret( sysUser );
    }
}
