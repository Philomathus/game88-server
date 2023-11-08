package tv.game88.platform.api.mapper;

import tv.game88.platform.api.entity.ActivityMemberInfo;

import java.util.List;
import java.util.Map;

/**
 * 会员推广管理Mapper接口
 *
 * @author 77tv
 * @date 2021-03-19
 */
public interface ActivityMemberInfoMapper {
    /**
     * 查询会员推广管理
     *
     * @param id 会员推广管理ID
     *
     * @return 会员推广管理
     */
    ActivityMemberInfo selectActivityMemberInfoById( String id );

    /**
     * 查询会员推广管理列表
     *
     * @param activityMemberInfo 会员推广管理
     *
     * @return 会员推广管理集合
     */
    List<ActivityMemberInfo> selectActivityMemberInfoList( ActivityMemberInfo activityMemberInfo );

    List<Map> selectIpList( ActivityMemberInfo activityMemberInfo );
}
