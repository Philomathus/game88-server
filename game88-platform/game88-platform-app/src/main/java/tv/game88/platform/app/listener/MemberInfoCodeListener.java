package tv.game88.platform.app.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.mapper.MemberInfoMapper;

import javax.annotation.Resource;
import java.util.Objects;

@Component
public class MemberInfoCodeListener implements ApplicationListener<ContextRefreshedEvent> {
    @Resource
    private RedisUtils       redisUtils;
    @Resource
    private MemberInfoMapper memberInfoMapper;

    @Override
    public void onApplicationEvent( ContextRefreshedEvent event ) {
        String code = memberInfoMapper.selectMaxMemberCode();
        if ( code.equals( "0" ) ) {
            return;
        }
        String maxCode      = code.substring( code.lastIndexOf( "_" ) + 1 );
        int    mysqlMaxCode = Integer.parseInt( maxCode );

        RedisAtomicLong entityIdCounter = new RedisAtomicLong( Constants.MEMBER_CODE,
                Objects.requireNonNull( redisUtils.getConnectionFactory() ) );
        long redisMaxCode = Constants.MEMBER_CODE_INIT + entityIdCounter.get();
        if ( redisMaxCode < mysqlMaxCode ) {
            entityIdCounter.set( mysqlMaxCode + 10 );
        }
    }
}
