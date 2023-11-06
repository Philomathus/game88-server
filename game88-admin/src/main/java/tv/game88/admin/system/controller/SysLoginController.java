package tv.game88.admin.system.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.admin.system.service.ISysLoginService;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.entity.SysMenu;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.security.service.SysPermissionService;
import tv.game88.core.admin.service.ISysMenuService;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginBody;
import tv.game88.core.admin.vo.LoginUser;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录验证
 *
 * @author MengJun
 */
@Log4j2
@RestController
public class SysLoginController {
    @Resource
    private ISysLoginService     sysLoginService;
    @Resource
    private ISysMenuService      menuService;
    @Resource
    private SysPermissionService permissionService;

    @Value( "${spring.profiles.active}" )
    private String profile;

    /**
     * 登录方法
     *
     * @param data 登录加密信息
     *
     * @return 结果
     */
    @PostMapping( value = "/login" )
    public RspBase<String> login( @RequestBody String data ) throws Exception {
        if ( StringUtils.isNotBlank( data ) && data.startsWith( "\"" ) ) {
            data = data.substring( 1, data.length() - 1 );
        }
        String    decryptStr = RSACoder.decryptByPrivateKey( data, KeyConstants.LOGIN_PRIVATE_KEY );
        LoginBody loginBody  = JsonUtil.json2Object( decryptStr, LoginBody.class );
        assert loginBody != null;
        // 生成令牌
        return sysLoginService.login( loginBody );
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping( "getInfo" )
    public RspBase<Map<String, Object>> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser   user      = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission( user );
        // 权限集合
        permissionService.getMenuPermission( user );
        user.setPassword( null );
        return RspBase.ok( ImmutableMap.of( "user", user, "roles", roles, "permissions", user.getPermissions() ) );
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping( "getRouters" )
    public RspBase<Map<String, Object>> getRouters() {
        LoginUser           loginUser = SecurityUtils.getLoginUser();
        SysUser             user      = loginUser.getUser();
        List<SysMenu>       menus     = menuService.selectMenuTreeByUserId( user.getUserId() );
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put( "menus", menuService.buildMenus( menus ) );
        resultMap.put( "vhostUrl", "general".equals( profile ) ? "" : ConfigDomainCacheUtil.me.getDomainOssValue() );
        return RspBase.ok( resultMap );
    }
}
