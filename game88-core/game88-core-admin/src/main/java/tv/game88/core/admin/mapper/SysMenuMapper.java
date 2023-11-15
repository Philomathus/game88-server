package tv.game88.core.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.admin.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单表 数据层
 *
 * @author MengJun
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
	/**
	 * 查询系统菜单列表
	 *
	 * @param menu 菜单信息
	 * @return 菜单列表
	 */
	public List<SysMenu> selectMenuList( SysMenu menu );

	/**
	 * 根据用户ID查询权限
	 *
	 * @param userId 用户ID
	 * @return 权限列表
	 */
	public List<String> selectMenuPermsByUserId( Long userId );

	/**
	 * 根据用户ID查询菜单
	 *
	 * @return 菜单列表
	 */
	public List<SysMenu> selectMenuTreeAll();

	/**
	 * 根据用户ID查询菜单
	 *
	 * @param userId 用户ID
	 * @return 菜单列表
	 */
	public List<SysMenu> selectMenuTreeByUserId( Long userId );

	/**
	 * 根据角色ID查询菜单树信息
	 *
	 * @param roleId            角色ID
	 * @param menuCheckStrictly 菜单树选择项是否关联显示
	 * @return 选中菜单列表
	 */
	public List<Integer> selectMenuListByRoleId( @Param( "roleId" ) Long roleId,
												 @Param( "menuCheckStrictly" ) boolean menuCheckStrictly );

	/**
	 * 校验菜单名称是否唯一
	 *
	 * @param menuName 菜单名称
	 * @param parentId 父菜单ID
	 * @return 结果
	 */
	public SysMenu checkMenuNameUnique( @Param( "menuName" ) String menuName, @Param( "parentId" ) Long parentId );
}
