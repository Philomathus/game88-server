package tv.game88.core.admin.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.annotation.RateLimiter;
import tv.game88.core.admin.enums.LimitType;
import tv.game88.core.admin.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流处理
 *
 * @author ruoyi
 */
@Log4j2
@Aspect
@Component
public class RateLimiterAspect {
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate( RedisTemplate<Object, Object> redisTemplate ) {
        this.redisTemplate = redisTemplate;
    }

    @Before( "@annotation(rateLimiter)" )
    public void doBefore( JoinPoint point, RateLimiter rateLimiter ) throws Throwable {
        String key   = rateLimiter.key();
        int    time  = rateLimiter.time();
        int    count = rateLimiter.count();

        String       combineKey = getCombineKey( rateLimiter, point );
        List<Object> keys       = Collections.singletonList( combineKey );
        try {
            Long number = redisTemplate.execute( limitScript(), keys, count, time );
            if (StringUtils.isNull( number ) || number.intValue() > count) {
                throw new ServiceException( "访问过于频繁，请稍候再试" );
            }
            log.info( "限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), key );
        } catch ( ServiceException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new RuntimeException( "服务器限流异常，请稍候再试" );
        }
    }

    public String getCombineKey( RateLimiter rateLimiter, JoinPoint point ) {
        StringBuilder stringBuilder = new StringBuilder( rateLimiter.key() );
        if (rateLimiter.limitType() == LimitType.IP) {
            stringBuilder.append( ServletUtil.getIp() ).append( "-" );
        }
        MethodSignature signature   = ( MethodSignature ) point.getSignature();
        Method          method      = signature.getMethod();
        Class<?>        targetClass = method.getDeclaringClass();
        stringBuilder.append( targetClass.getName() ).append( "-" ).append( method.getName() );
        return stringBuilder.toString();
    }

    private static RedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText( limitScriptText() );
        redisScript.setResultType( Long.class );
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private static String limitScriptText() {
        return """
                 local key = KEYS[1]
                 local count = tonumber(ARGV[1])
                 local time = tonumber(ARGV[2])
                 local current = redis.call('get', key);
                 if current and tonumber(current) > count then
                     return tonumber(current);
                 end
                 current = redis.call('incr', key)
                 if tonumber(current) == 1 then
                     redis.call('expire', key, time)
                 end
                 return tonumber(current);
                """;
    }
}
