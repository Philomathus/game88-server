package tv.game88.wallet.app.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.wallet.api.dto.RspMember;
import tv.game88.wallet.api.vo.PlatformUser;
import tv.game88.wallet.app.vo.MemberLoginUser;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * token验证处理
 *
 * @author MengJun
 */
@Log4j2
@Component
public class MemberTokenManager {
    // 令牌有效期（默认1天）
    @Value( "${token.expireTime:1}" )
    private int expireTime;

    @Resource
    private RedisUtils redisUtil;

    /**
     * 设置会员token
     *
     * @param rspMember 会员信息
     * @param ip        登录IP
     */
    public void setRspMemberToken( RspMember rspMember, String ip ) {
        if ( rspMember != null ) {
            PlatformUser platformUser = new PlatformUser();
            BeanUtils.copyProperties( rspMember, platformUser );
            MemberLoginUser loginUser = new MemberLoginUser( platformUser );
            loginUser.setLoginTime( LocalDateTime.now() );
            loginUser.setLoginIp( ip );
            String token = this.createToken( loginUser );
            rspMember.setToken( token );
        }
    }

    /**
     * 设置会员token
     *
     * @param platformUser 会员信息
     * @param ip           登录IP
     */
    public String setRspMemberToken( PlatformUser platformUser, String ip ) {
        if ( platformUser != null ) {
            /*String token = this.refreshLoginUserCache( platformUser.getId() );
            if ( StringUtils.isNotBlank( token ) ) {
                return token;
            }*/
            MemberLoginUser loginUser = new MemberLoginUser( platformUser );
            loginUser.setLoginTime( LocalDateTime.now() );
            loginUser.setLoginIp( ip );
            return this.createToken( loginUser );
        }
        return null;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public MemberLoginUser getLoginUser( HttpServletRequest request ) {
        // 获取请求携带的令牌
        String token = getToken( request );
        if ( StringUtils.isNotBlank( token ) ) {
            Map<Object, Object> loginUserMap = redisUtil.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
            if ( !CollectionUtils.isEmpty( loginUserMap ) ) {
                return JsonUtil.map2Object( loginUserMap, MemberLoginUser.class );
            }
        }
        return null;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public MemberLoginUser getLoginUser( String token ) {
        Map<Object, Object> loginUserMap = redisUtil.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
        if ( !CollectionUtils.isEmpty( loginUserMap ) ) {
            return JsonUtil.map2Object( loginUserMap, MemberLoginUser.class );
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUserCache( MemberLoginUser loginUser, String token ) {
        if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNotBlank( token ) ) {
            this.delToken( loginUser.getUserId() );
            Duration duration = Duration.ofDays( expireTime );
            redisUtil.strSet( Constants.MEMBER_LOGIN_USER + loginUser.getUserId(), token, duration );
            redisUtil.hMSet( Constants.MEMBER_LOGIN_TOKEN + token, JsonUtil.object2MapStr( loginUser ) );
            redisUtil.expire( Constants.MEMBER_LOGIN_TOKEN + token, duration );
        }
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     *
     * @return 令牌
     */
    private String createToken( MemberLoginUser loginUser ) {
        String token = RandomStringUtils.randomAlphabetic( 3 ) + IdWorker.get32UUID();
        setLoginUserCache( loginUser, token );
        return token;
    }

    /**
     * 刷新令牌有效期
     */
    public String refreshLoginUserCache( String userId ) {
        String token = redisUtil.strGet( Constants.MEMBER_LOGIN_USER + userId );
        if ( StringUtils.isNotBlank( token ) ) {
            Duration duration = Duration.ofDays( expireTime );
            redisUtil.expire( Constants.MEMBER_LOGIN_USER + userId, duration );
            redisUtil.expire( Constants.MEMBER_LOGIN_TOKEN + token, duration );
        }
        return token;
    }

    /**
     * 获取请求token
     *
     * @return token
     */
    private String getToken( HttpServletRequest request ) {
        return request.getHeader( "token" );
    }

    public void delToken( String memberId ) {
        String token = redisUtil.strGet( Constants.MEMBER_LOGIN_USER + memberId );
        if ( StringUtils.isNotBlank( token ) ) {
            redisUtil.unlink( Constants.MEMBER_LOGIN_TOKEN + token, Constants.MEMBER_LOGIN_USER + memberId );
        }
    }
}
