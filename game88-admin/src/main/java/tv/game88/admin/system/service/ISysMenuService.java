package tv.game88.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.admin.entity.SysMenu;
import tv.game88.core.admin.vo.RouterVo;
import tv.game88.core.admin.vo.TreeSelect;

import java.util.List;

/**
 * 菜单 业务层
 *
 * @author MengJun
 */
public interface ISysMenuService extends IService<SysMenu> {
    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     *
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList( Long userId );

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menu   菜单信息
     * @param userId 用户ID
     *
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList( SysMenu menu, Long userId );

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     *
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeByUserId( Long userId );

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     *
     * @return 选中菜单列表
     */
    public List<Integer> selectMenuListByRoleId( Long roleId );

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     *
     * @return 路由列表
     */
    public List<RouterVo> buildMenus( List<SysMenu> menus );

    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     *
     * @return 树结构列表
     */
    public List<SysMenu> buildMenuTree( List<SysMenu> menus );

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     *
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildMenuTreeSelect( List<SysMenu> menus );

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     *
     * @return 菜单信息
     */
    public SysMenu selectMenuById( Long menuId );

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     *
     * @return 结果 true 存在 false 不存在
     */
    public boolean hasChildByMenuId( Long menuId );

    /**
     * 查询菜单是否存在角色
     *
     * @param menuId 菜单ID
     *
     * @return 结果 true 存在 false 不存在
     */
    public boolean checkMenuExistRole( Long menuId );

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     *
     * @return 结果
     */
    public String checkMenuNameUnique( SysMenu menu );
}
