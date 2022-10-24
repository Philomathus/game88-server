package tv.game88.core.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.vo.LoginUser;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * token验证处理
 *
 * @author MengJun
 */
@Log4j2
@Component
public class TokenService {

    // 令牌自定义标识
    @Value( "${token.header}" )
    private String header;
    // 令牌有效期（默认1小时）
    @Value( "${token.expireTime:1}" )
    private int    expireTime;

    @Resource
    private RedisUtils redisUtil;

    private static Key KEY_SECRET;

    @Value( "${token.secret}" )
    private void setKeySecret( String secret ) {
        TokenService.KEY_SECRET = Keys.hmacShaKeyFor( secret.getBytes( StandardCharsets.UTF_8 ) );
    }

    public static void main( String[] args ) throws Exception {
        //SecretKey secretKey = Keys.secretKeyFor( SignatureAlgorithm.HS512);
        //System.out.println(Base64Utils.encodeToString( secretKey.getEncoded() ));

        //KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        //System.out.println("公钥:" + Base64Utils.encodeToString( keyPair.getPublic().getEncoded() ));
        //System.out.println("私钥:" + Base64Utils.encodeToString( keyPair.getPrivate().getEncoded() ));
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser( HttpServletRequest request ) {
        // 获取请求携带的令牌
        String token = getToken( request );
        if ( StringUtils.isNotBlank( token ) ) {
            Claims claims = parseToken( token );
            if ( claims == null ) {
                return null;
            }
            // 解析对应的权限以及用户信息
            String userKey = ( String ) claims.get( AdminConstants.USER_KEY );
            if ( StringUtils.isBlank( userKey ) ) {
                return null;
            }
            Map<Object, Object> loginUserMap = redisUtil.hGetAll( AdminConstants.SYS_LOGIN_TOKEN + userKey );
            if ( !CollectionUtils.isEmpty( loginUserMap ) ) {
                return JsonUtil.map2Object( loginUserMap, LoginUser.class );
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser( LoginUser loginUser ) {
        if ( StringUtils.isNotNull( loginUser ) && StringUtils.isNotBlank( loginUser.getToken() ) ) {
            Duration duration = Duration.ofHours( expireTime );
            redisUtil.strSet( AdminConstants.SYS_LOGIN_USER + loginUser.getUser().getUserId(), loginUser.getToken(), duration );
            redisUtil.hMSet( AdminConstants.SYS_LOGIN_TOKEN + loginUser.getToken(), JsonUtil.object2Map( loginUser ) );
            redisUtil.expire( AdminConstants.SYS_LOGIN_TOKEN + loginUser.getToken(), duration );
        }
    }

    /**
     * 创建令牌
     *
     * @param loginUser 用户信息
     *
     * @return 令牌
     */
    public String createToken( LoginUser loginUser ) {
        String token = RandomStringUtils.randomAlphabetic( 2 ) + IdWorker.get32UUID();
        loginUser.setToken( token );
        setUserAgent( loginUser );
        setLoginUser( loginUser );

        Map<String, Object> claims = new HashMap<>();
        claims.put( AdminConstants.USER_KEY, token );
        return createToken( claims );
    }

    /**
     * 刷新令牌有效期
     */
    public void refreshToken( LoginUser loginUser ) {
        String userKey = redisUtil.strGet( AdminConstants.SYS_LOGIN_USER + loginUser.getUser().getUserId() );
        if ( StringUtils.isNotBlank( userKey ) ) {
            Duration duration = Duration.ofMinutes( expireTime );
            redisUtil.expire( AdminConstants.SYS_LOGIN_USER + loginUser.getUser().getUserId(), duration );
            redisUtil.expire( AdminConstants.SYS_LOGIN_TOKEN + userKey, duration );
        }
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent( LoginUser loginUser ) {
        UserAgent userAgent = UserAgent.parseUserAgentString( ServletUtil.getHttpServletRequest().getHeader( "User-Agent" ) );
        loginUser.setBrowser( userAgent.getBrowser().getName() );
        loginUser.setOs( userAgent.getOperatingSystem().getName() );
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     *
     * @return 令牌
     */
    private String createToken( Map<String, Object> claims ) {
        return Jwts.builder().setClaims( claims ).signWith( KEY_SECRET ).compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     *
     * @return 数据声明
     */
    private Claims parseToken( String token ) {
        try {
            return Jwts.parserBuilder().setSigningKey( KEY_SECRET ).build().parseClaimsJws( token ).getBody();
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }

    /**
     * 获取请求token
     *
     * @return token
     */
    private String getToken( HttpServletRequest request ) {
        String token = request.getHeader( header );
        if ( StringUtils.isNotBlank( token ) && token.startsWith( AdminConstants.TOKEN_PREFIX ) ) {
            token = token.replace( AdminConstants.TOKEN_PREFIX, "" );
        }
        return token;
    }

    public void delToken( Long userId ) {
        String token = redisUtil.strGet( AdminConstants.SYS_LOGIN_USER + userId );
        if ( StringUtils.isNotBlank( token ) ) {
            redisUtil.unlink( AdminConstants.SYS_LOGIN_TOKEN + token );
            redisUtil.unlink( AdminConstants.SYS_LOGIN_USER + userId );
        }
    }
}
