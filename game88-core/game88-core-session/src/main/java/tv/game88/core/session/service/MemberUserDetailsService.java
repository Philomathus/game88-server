package tv.game88.core.session.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.session.vo.MemberLoginUser;

import javax.annotation.Resource;

/**
 * 用户验证处理
 *
 * @author MengJun
 */
@Log4j2
@Service
public class MemberUserDetailsService implements UserDetailsService {
    @Resource
    private MemberInfoMapper memberInfoMapper;

    @Override
    public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
        PlatformUser platformUser = memberInfoMapper.selectPlatformUserByUserName( username );
        if ( StringUtils.isNull( platformUser ) ) {
            log.info( "登录用户：{} 不存在.", username );
            throw new UsernameNotFoundException( "登录用户：" + username + "不存在" );
        } else if ( platformUser.getStatus() == 0 ) {
            log.info( "登录用户：{} 已被停用.", username );
            throw new BusinessException( "对不起，您的账号：" + username + "已停用" );
        }
        return new MemberLoginUser( platformUser );
    }
}
