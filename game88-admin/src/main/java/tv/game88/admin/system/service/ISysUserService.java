package tv.game88.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.admin.entity.SysUser;

import java.util.List;

/**
 * 用户 业务层
 *
 * @author MengJun
 */
public interface ISysUserService extends IService<SysUser> {
	/**
	 * 根据条件分页查询用户列表
	 *
	 * @param user 用户信息
	 * @return 用户信息集合信息
	 */
	public List<SysUser> selectUserList( SysUser user );

	/**
	 * 通过用户ID查询用户
	 *
	 * @param userId 用户ID
	 * @return 用户对象信息
	 */
	public SysUser selectUserById( Long userId );

	/**
	 * 通过用户名查询用户google验证秘钥
	 *
	 * @param userName 用户名
	 * @return 用户google验证秘钥
	 */
	public SysUser selectOtpSecretByUserName( String userName );

	/**
	 * 根据用户ID查询用户所属角色组
	 *
	 * @param userName 用户名
	 * @return 结果
	 */
	public String selectUserRoleGroup( String userName );

	/**
	 * 校验用户名称是否唯一
	 *
	 * @param userName 用户名称
	 * @return 结果
	 */
	public String checkUserNameUnique( String userName );

	/**
	 * 校验用户是否允许操作
	 *
	 * @param user 用户信息
	 */
	public void checkUserAllowed( SysUser user );

	/**
	 * 新增用户信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public int insertUser( SysUser user );

	/**
	 * 修改用户信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public int updateUser( SysUser user );
	/**
	 * 修改用户状态
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public int updateUserStatus( SysUser user );

	/**
	 * 修改用户基本信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public int updateUserProfile( SysUser user );

	/**
	 * 修改用户登陆时间和IP
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public boolean updateUserLoginTime( SysUser user );

	/**
	 * 重置用户密码
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	public int resetPwd( SysUser user );

	/**
	 * 重置用户密码
	 *
	 * @param userName 用户名
	 * @param password 密码
	 * @return 结果
	 */
	public int resetUserPwd( String userName, String password );


	/**
	 * 批量删除用户信息
	 *
	 * @param userIds 需要删除的用户ID
	 * @return 结果
	 */
	public int deleteUserByIds( Long[] userIds );

}
