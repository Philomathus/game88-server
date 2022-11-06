package tv.game88.admin.system.controller;

import com.google.common.collect.ImmutableMap;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.service.ISysUserService;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/user/profile" )
public class SysProfileController extends BaseController {
    @Resource
    private ISysUserService     userService;
    @Resource
    private SysUserTokenService sysUserTokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public Map<String, Object> profile() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser   user      = loginUser.getUser();
        user.setPassword( null );
        return ImmutableMap.of( "code", HttpStatus.SUCCESS, "data", user, "roleGroup",
                userService.selectUserRoleGroup( loginUser.getUsername() ) );
    }

    /**
     * 修改用户
     */
    @Log( title = "个人信息", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> updateProfile( @RequestBody SysUser user ) {
        if ( userService.updateUserProfile( user ) > 0 ) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            // 更新缓存用户信息
            loginUser.getUser().setNickName( user.getNickName() );
            loginUser.getUser().setSex( user.getSex() );
            sysUserTokenService.setLoginUser( loginUser );
            return RspBase.ok();
        }
        return RspBase.businessError( "修改个人信息异常，请联系管理员" );
    }

    /**
     * 重置密码
     */
    @Log( title = "个人信息", businessType = BusinessType.UPDATE )
    @PutMapping( "/updatePwd" )
    public RspBase<?> updatePwd( @RequestBody String data ) throws Exception {
        if ( StringUtils.isNotBlank( data ) && data.startsWith( "\"" ) ) {
            data = data.substring( 1, data.length() - 1 );
        }
        String decryptStr = RSACoder.decryptByPrivateKey( data, KeyConstants.LOGIN_PRIVATE_KEY );

        Map<String, String> requestMap = JsonUtil.json2Map( decryptStr );

        String oldPassword = requestMap.get( "oldPwd" );
        String newPassword = requestMap.get( "newPwd" );

        LoginUser loginUser = SecurityUtils.getLoginUser();
        String    userName  = loginUser.getUsername();
        String    password  = loginUser.getPassword();
        if ( !SecurityUtils.matchesPassword( oldPassword, password ) ) {
            return RspBase.businessError( "修改密码失败，旧密码错误" );
        }
        if ( SecurityUtils.matchesPassword( newPassword, password ) ) {
            return RspBase.businessError( "新密码不能与旧密码相同" );
        }
        if ( userService.resetUserPwd( userName, SecurityUtils.encryptPassword( newPassword ) ) > 0 ) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword( SecurityUtils.encryptPassword( newPassword ) );
            sysUserTokenService.setLoginUser( loginUser );
            return RspBase.ok();
        }
        return RspBase.businessError( "修改密码异常，请联系管理员" );
    }
}
