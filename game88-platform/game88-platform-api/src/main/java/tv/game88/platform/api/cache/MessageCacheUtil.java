package tv.game88.platform.api.cache;


import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.platform.api.dto.RspMessageCommonProblem;
import tv.game88.platform.api.dto.RspMessageHomeNotice;
import tv.game88.platform.api.dto.RspMessageOnSite;
import tv.game88.platform.api.mapper.MessageCommonProblemMapper;
import tv.game88.platform.api.mapper.MessageHomeNoticeMapper;
import tv.game88.platform.api.mapper.MessageOnSiteMapper;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏缓存
 */
@Component
public class MessageCacheUtil {

    public static final String HOME_NOTICE    = Constants.MESSAGE_PREX + "homeNotices";
    public static final String COMMON_PROBLEM = Constants.MESSAGE_PREX + "commonProblem";
    public static final String ON_SITE        = Constants.MESSAGE_PREX + "onSite";

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private MessageCommonProblemMapper messageCommonProblemMapper;
    @Resource
    private MessageHomeNoticeMapper    messageHomeNoticeMapper;
    @Resource
    private MessageOnSiteMapper        messageOnSiteMapper;

    public List<RspMessageHomeNotice> getMessageHomeNotices() {
        Boolean exists = redisUtils.exists( HOME_NOTICE );
        if ( exists == null || !exists ) {
            List<RspMessageHomeNotice> result = new QueryChainWrapper<>( messageHomeNoticeMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "sort" )
                    .list()
                    .stream()
                    .map( hn -> {
                        RspMessageHomeNotice rsp = new RspMessageHomeNotice();
                        BeanUtils.copyProperties( hn, rsp );
                        return rsp;
                    } )
                    .collect( Collectors.toList() );
            if ( result.size() > 0 ) {
                if ( redisUtils.lock( HOME_NOTICE, 3 ) ) {
                    redisUtils.lRightPushAll( HOME_NOTICE, result
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtils.unLock( HOME_NOTICE );
                }
            }
            return result;
        }
        List<String> list = redisUtils.lRange( HOME_NOTICE, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, RspMessageHomeNotice.class ) ).collect( Collectors.toList() );
    }

    public List<RspMessageCommonProblem> getMessageCommonProblems() {
        Boolean exists = redisUtils.exists( COMMON_PROBLEM );
        if ( exists == null || !exists ) {
            List<RspMessageCommonProblem> result = new QueryChainWrapper<>( messageCommonProblemMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "sort" )
                    .list()
                    .stream()
                    .map( hn -> {
                        RspMessageCommonProblem rsp = new RspMessageCommonProblem();
                        BeanUtils.copyProperties( hn, rsp );
                        return rsp;
                    } )
                    .collect( Collectors.toList() );
            if ( result.size() > 0 ) {
                if ( redisUtils.lock( COMMON_PROBLEM, 3 ) ) {
                    redisUtils.lRightPushAll( COMMON_PROBLEM, result
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtils.unLock( COMMON_PROBLEM );
                }
            }
            return result;
        }
        List<String> list = redisUtils.lRange( COMMON_PROBLEM, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, RspMessageCommonProblem.class ) ).collect( Collectors.toList() );
    }

    public List<RspMessageOnSite> getMessageOnSites() {
        Boolean exists = redisUtils.exists( ON_SITE );
        if ( exists == null || !exists ) {
            List<RspMessageOnSite> result = new QueryChainWrapper<>( messageOnSiteMapper )
                    .ge( "create_time", LocalDateTime.now().minusMonths( 1 ) )
                    .orderByDesc( "create_time" )
                    .list()
                    .stream()
                    .map( hn -> {
                        RspMessageOnSite rsp = new RspMessageOnSite();
                        BeanUtils.copyProperties( hn, rsp );
                        return rsp;
                    } )
                    .collect( Collectors.toList() );
            if ( result.size() > 0 ) {
                if ( redisUtils.lock( ON_SITE, 3 ) ) {
                    redisUtils.lRightPushAll( ON_SITE, result
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtils.expire( ON_SITE, Duration.ofDays( 30 ) );
                    redisUtils.unLock( ON_SITE );
                }
            }
            return result;
        }
        List<String> list = redisUtils.lRange( ON_SITE, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, RspMessageOnSite.class ) ).collect( Collectors.toList() );
    }

    public void clear( String key ) {
        redisUtils.unlink( key );
    }
}
