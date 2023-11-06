package tv.game88.core.admin.security.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.entity.SysUser;
import tv.game88.core.admin.enums.UserStatus;
import tv.game88.core.admin.service.ISysUserService;
import tv.game88.core.admin.vo.LoginUser;

import jakarta.annotation.Resource;

/**
 * 用户验证处理
 *
 * @author MengJun
 */
@Log4j2
@Service
public class SysUserDetailsService implements UserDetailsService {
    @Resource
    private ISysUserService      userService;
    @Resource
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUserName( username );
        if ( StringUtils.isNull( user ) ) {
            log.info( "登录用户：{} 不存在.", username );
            throw new UsernameNotFoundException( "登录用户：" + username + "不存在" );
        } else if ( UserStatus.DELETED.getCode().equals( user.getDelFlag() ) ) {
            log.info( "登录用户：{} 已被删除.", username );
            throw new BusinessException( "对不起，您的账号：" + username + "已被删除" );
        } else if ( UserStatus.DISABLE.getCode().equals( user.getStatus() ) ) {
            log.info( "登录用户：{} 已被停用.", username );
            throw new BusinessException( "对不起，您的账号：" + username + "已停用" );
        }
        user.setOtpSecret( userService.selectOtpSecretByUserName( username ).getOtpSecret() );
        permissionService.getMenuPermission( user );
        return new LoginUser( user );
    }
}
