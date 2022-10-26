package tv.game88.admin.system.controller;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tv.game88.admin.system.entity.req.UserResetPwdReq;
import tv.game88.common.base.BaseController;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.GoogleAuthUtil;
import tv.game88.common.utils.RSACoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.KeyConstants;
import tv.game88.core.admin.constant.UserConstants;
import tv.game88.core.admin.entity.SysRole;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.mapper.SysUserMapper;
import tv.game88.core.admin.service.ISysRoleService;
import tv.game88.core.admin.service.ISysUserService;
import tv.game88.core.admin.service.impl.TokenService;
import tv.game88.core.admin.utils.SecurityUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/system/user" )
public class SysUserController extends BaseController {
    @Resource
    private ISysUserService userService;
    @Resource
    private ISysRoleService roleService;
    @Resource
    private TokenService    tokenService;
    @Resource
    private SysUserMapper   sysUserMapper;
    @Value( "${spring.profiles.active}" )
    private String          profile;

    /**
     * 获取用户列表
     */
    @PreAuthorize( "@ss.hasPermi('system:user:list')" )
    @GetMapping( "/list" )
    public RspBase<List<SysUser>> list( SysUser user ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<SysUser> list = userService.selectUserList( user );
        return getRspBasePage( list, pageDomain );
    }

    @Log( title = "用户管理", businessType = BusinessType.EXPORT )
    @PreAuthorize( "@ss.hasPermi('system:user:export')" )
    @GetMapping( "/export" )
    public void export( SysUser user, HttpServletResponse response ) {
        List<SysUser> list = userService.selectUserList( user );
        ExportExcelUtil.exportExcel( list, "用户信息", "用户信息表", SysUser.class, response );
    }

    /**
     * 根据用户编号获取详细信息
     */
    @PreAuthorize( "@ss.hasPermi('system:user:query')" )
    @GetMapping( value = { "/", "/{userId}" } )
    public Map<String, Object> getInfo( @PathVariable( value = "userId", required = false ) Long userId ) {
        ImmutableMap.Builder<String, Object> resultMapBuilder = ImmutableMap.builder();
        resultMapBuilder.put( "code", HttpStatus.SUCCESS );
        List<SysRole> roles = roleService.selectRoleAll();
        resultMapBuilder.put( "roles", SysUser.isAdmin( userId ) ? roles : roles.stream().filter( r -> !r.isAdmin() )
                                                                                .collect( Collectors.toList() ) );
        if ( StringUtils.isNotNull( userId ) ) {
            resultMapBuilder.put( "data", userService.selectUserById( userId ) );
            resultMapBuilder.put( "roleIds", roleService.selectRoleListByUserId( userId ) );
        }
        return resultMapBuilder.build();
    }

    /**
     * 新增用户
     */
    @PreAuthorize( "@ss.hasPermi('system:user:add')" )
    @Log( title = "用户管理", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @Validated @RequestBody SysUser user ) {
        if ( UserConstants.NOT_UNIQUE.equals( userService.checkUserNameUnique( user.getUserName() ) ) ) {
            return RspBase.businessError( "新增用户'" + user.getUserName() + "'失败，登录账号已存在" );
        }
        user.setCreateBy( SecurityUtils.getUsername() );
        user.setPassword( SecurityUtils.encryptPassword( user.getPassword() ) );
        return toResult( userService.insertUser( user ) );
    }

    /**
     * 修改用户
     */
    @PreAuthorize( "@ss.hasPermi('system:user:edit')" )
    @Log( title = "用户管理", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @Validated @RequestBody SysUser user ) {
        userService.checkUserAllowed( user );
        user.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( userService.updateUser( user ) );
    }

    /**
     * 删除用户
     */
    @PreAuthorize( "@ss.hasPermi('system:user:remove')" )
    @Log( title = "用户管理", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{userIds}" )
    public RspBase<?> remove( @PathVariable Long[] userIds ) {
        return toResult( userService.deleteUserByIds( userIds ) );
    }

    /**
     * 重置密码
     */
    @PreAuthorize( "@ss.hasPermi('system:user:resetPwd')" )
    @Log( title = "用户重置密码", businessType = BusinessType.UPDATE )
    @PutMapping( "/resetPwd" )
    public RspBase<?> resetPwd( @Validated @RequestBody UserResetPwdReq userResetPwdReq ) throws Exception {
        if ( 1L == userResetPwdReq.getUserId() ) {
            throw new BusinessException( "不允许操作超级管理员角色" );
        }
        SecurityUtils.verifyMFACode( userResetPwdReq.getOtpCode() );
        SysUser user = new SysUser( userResetPwdReq.getUserId() );
        user.setPassword( SecurityUtils.encryptPassword( userResetPwdReq.getPassword() ) );
        user.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( userService.resetPwd( user ) );
    }

    /**
     * 状态修改
     */
    @PreAuthorize( "@ss.hasPermi('system:user:edit')" )
    @Log( title = "用户状态修改", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus" )
    public RspBase<?> changeStatus( @RequestBody SysUser user ) {
        userService.checkUserAllowed( user );
        user.setUpdateBy( SecurityUtils.getUsername() );
        return toResult( userService.updateUserStatus( user ) );
    }

    /**
     * 获取MFA验证码二维码
     */
    @GetMapping( "getOtpSecretQrcode" )
    public RspBase<Map<String, String>> getOtpSecretQrcode( String name ) {
        String              secretKey    = GoogleAuthUtil.createSecretKey();
        String              qrBarcodeUrl = GoogleAuthUtil.getQRBarcodeURL( name, this.profile + "管理后台", secretKey );
        Map<String, String> resultMap    = new HashMap<>();
        resultMap.put( "secretKey", secretKey );
        resultMap.put( "qrBarcodeBase", GoogleAuthUtil.tranUrlToBase64String( qrBarcodeUrl ) );
        return RspBase.ok( resultMap );
    }

    /**
     * 重置用户MFA秘钥
     */
    @PreAuthorize( "@ss.hasPermi('system:user:resetOtp')" )
    @DeleteMapping( "resetUserOtpSecret" )
    @Log( title = "重置用户MFA秘钥", businessType = BusinessType.DELETE )
    public RspBase<?> resetUserOtpSecret( Long userId, int otpAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( otpAuthCode );
        SysUser sysUser = new SysUser( userId );
        sysUser.setOtpSecret( null );
        sysUserMapper.updateOtpSecret( sysUser );
        tokenService.delToken( userId );
        return RspBase.ok();
    }

    /**
     * 绑定MFA密钥
     */
    @PreAuthorize( "@ss.hasPermi('system:user:resetOtp')" )
    @PostMapping( "boundOtpSecret" )
    public RspBase<?> boundOtpSecret( @RequestBody Map<String, Object> requestMap ) throws Exception {
        int    otpAuthCode = Integer.parseInt( requestMap.getOrDefault( "otpAuthCode", 0 ).toString() );
        String otpAuthKey  = requestMap.getOrDefault( "otpAuthKey", "" ).toString();
        String otpAuthName = requestMap.getOrDefault( "otpAuthName", "" ).toString();
        if ( GoogleAuthUtil.verifyCode( otpAuthKey, otpAuthCode ) ) {
            SysUser sysUser = userService.selectOtpSecretByUserName( otpAuthName );
            if ( sysUser == null ) {
                return RspBase.businessError( "获取用户账户异常" );
            }
            //当用户是重置OTP密钥
            if ( StringUtils.isNotBlank( sysUser.getOtpSecret() ) ) {
                return RspBase.businessError( "该账户已绑定谷MFA验证器，请勿重复绑定" );
            }
            sysUser.setOtpSecret( RSACoder.encryptByPublicKey( otpAuthKey, KeyConstants.GOOGLE_AUTH_PUBLIC_KEY ) );
            sysUserMapper.updateOtpSecret( sysUser );
            return RspBase.ok();
        }
        return RspBase.businessError( "MFA验证码不正确，请检查" );
    }
}
