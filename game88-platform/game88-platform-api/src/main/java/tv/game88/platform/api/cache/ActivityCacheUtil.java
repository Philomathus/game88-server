package tv.game88.platform.api.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.platform.api.entity.ActivityInfo;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.platform.api.entity.ActivityQuestType;
import tv.game88.platform.api.entity.ActivityType;
import tv.game88.platform.api.mapper.ActivityInfoMapper;
import tv.game88.core.quest.mapper.ActivityQuestInfoMapper;
import tv.game88.platform.api.mapper.ActivityQuestTypeMapper;
import tv.game88.platform.api.mapper.ActivityTypeMapper;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActivityCacheUtil {
    // 活动类型
    public static final String ACTIVITY_TYPE_KEY       = Constants.ACTIVITY_PREX + "activityType";
    public static final String ACTIVITY_INFO_KEY       = Constants.ACTIVITY_PREX + "activityInfo";
    //任务
    public static final String ACTIVITY_QUEST_INFO_KEY = Constants.ACTIVITY_PREX + "activityQuestInfo";
    public static final String ACTIVITY_QUEST_TYPE_KEY = Constants.ACTIVITY_PREX + "activityQuestType";

    @Resource
    private RedisUtils redisUtil;

    @Resource
    private ActivityInfoMapper      activityInfoMapper;
    @Resource
    private ActivityTypeMapper      activityTypeMapper;
    @Resource
    private ActivityQuestInfoMapper activityQuestInfoMapper;
    @Resource
    private ActivityQuestTypeMapper activityQuestTypeMapper;

    /**
     * 查询活动信息列表
     */
    public List<ActivityInfo> getActiveInfos() {
        //判断是否有缓存
        Boolean exists = redisUtil.exists( ACTIVITY_INFO_KEY );
        if ( exists == null || !exists ) {
            List<ActivityInfo> activityInfos = new QueryChainWrapper<>( activityInfoMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "sort" )
                    .list();
            if ( !activityInfos.isEmpty() ) {
                if ( redisUtil.lock( ACTIVITY_INFO_KEY, 3 ) ) {
                    redisUtil.lRightPushAll( ACTIVITY_INFO_KEY, activityInfos
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtil.unLock( ACTIVITY_INFO_KEY );
                }
            }
            return activityInfos;
        }
        List<String> list = redisUtil.lRange( ACTIVITY_INFO_KEY, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, ActivityInfo.class ) ).collect( Collectors.toList() );
    }

    /**
     * 查询活动类型列表
     */
    public List<ActivityType> getActiveTypes() {
        //判断是否有缓存
        Boolean exists = redisUtil.exists( ACTIVITY_TYPE_KEY );
        if ( exists == null || !exists ) {
            List<ActivityType> activityInfos = new QueryChainWrapper<>( activityTypeMapper ).orderByAsc( "sort" ).list();
            if ( !activityInfos.isEmpty() ) {
                if ( redisUtil.lock( ACTIVITY_TYPE_KEY, 3 ) ) {
                    redisUtil.lRightPushAll( ACTIVITY_TYPE_KEY, activityInfos
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtil.unLock( ACTIVITY_TYPE_KEY );
                }
            }
            return activityInfos;
        }
        List<String> list = redisUtil.lRange( ACTIVITY_TYPE_KEY, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, ActivityType.class ) ).collect( Collectors.toList() );
    }

    /**
     * 查询任务信息列表
     */
    public List<ActivityQuestInfo> getQuestInfos() {
        //判断是否有缓存
        Boolean exists = redisUtil.exists( ACTIVITY_QUEST_INFO_KEY );
        if ( exists == null || !exists ) {
            List<ActivityQuestInfo> activityQuestInfos = new QueryChainWrapper<>( activityQuestInfoMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "sort" )
                    .list();
            if ( !activityQuestInfos.isEmpty() ) {
                if ( redisUtil.lock( ACTIVITY_QUEST_INFO_KEY, 3 ) ) {
                    redisUtil.lRightPushAll( ACTIVITY_QUEST_INFO_KEY, activityQuestInfos
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtil.unLock( ACTIVITY_QUEST_INFO_KEY );
                }
            }
            return activityQuestInfos;
        }
        List<String> list = redisUtil.lRange( ACTIVITY_QUEST_INFO_KEY, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, ActivityQuestInfo.class ) ).collect( Collectors.toList() );
    }

    /**
     * 查询任务类型列表
     */
    public List<ActivityQuestType> getQuestTypes() {
        //判断是否有缓存
        Boolean exists = redisUtil.exists( ACTIVITY_QUEST_TYPE_KEY );
        if ( exists == null || !exists ) {
            List<ActivityQuestType> activityQuestTypes = new QueryChainWrapper<>( activityQuestTypeMapper )
                    .orderByAsc( "sort" )
                    .list();
            if ( !activityQuestTypes.isEmpty() ) {
                if ( redisUtil.lock( ACTIVITY_QUEST_TYPE_KEY, 3 ) ) {
                    redisUtil.lRightPushAll( ACTIVITY_QUEST_TYPE_KEY, activityQuestTypes
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtil.unLock( ACTIVITY_QUEST_TYPE_KEY );
                }
            }
            return activityQuestTypes;
        }
        List<String> list = redisUtil.lRange( ACTIVITY_QUEST_TYPE_KEY, 0, -1 );
        return list.stream().map( a -> JsonUtil.json2Object( a, ActivityQuestType.class ) ).collect( Collectors.toList() );
    }

    /**
     * 活动信息
     */
    public void addActivityInfo( ActivityInfo activityInfo ) {
        Boolean exists = redisUtil.exists( ACTIVITY_INFO_KEY );
        if ( exists == null || !exists ) {
            List<ActivityInfo> activityInfos = new QueryChainWrapper<>( activityInfoMapper ).list();
            if ( !activityInfos.isEmpty() ) {
                if ( redisUtil.lock( ACTIVITY_INFO_KEY, 3 ) ) {
                    redisUtil.lRightPushAll( ACTIVITY_INFO_KEY, activityInfos
                            .stream()
                            .map( JsonUtil::object2Json )
                            .collect( Collectors.toList() ) );
                    redisUtil.unLock( ACTIVITY_INFO_KEY );
                }
            }
        } else {
            redisUtil.lRightPushAll( ACTIVITY_INFO_KEY, JsonUtil.object2Json( activityInfo ) );
        }
    }

    /**
     * 清空緩存
     */
    public void delActiveCache( String key ) {
        redisUtil.unlink( key );
    }


}
