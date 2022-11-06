package tv.game88.core.session.security.service;

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
    public UserDetails loadUserByUsername( String userId ) throws UsernameNotFoundException {
        PlatformUser platformUser = memberInfoMapper.selectPlatformUserByUserId( userId );
        if ( StringUtils.isNull( platformUser ) ) {
            log.info( "登录用户：{} 不存在.", userId );
            throw new UsernameNotFoundException( "登录用户不存在" );
        } else if ( platformUser.getStatus() == 0 ) {
            log.info( "登录用户：{} 已被停用.", userId );
            throw new BusinessException( "对不起，您的账号已停用" );
        }
        return new MemberLoginUser( platformUser );
    }
}
