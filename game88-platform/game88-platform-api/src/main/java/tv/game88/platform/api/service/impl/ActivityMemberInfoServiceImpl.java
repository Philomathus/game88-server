package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ActivityMemberInfo;
import tv.game88.platform.api.mapper.ActivityMemberInfoMapper;
import tv.game88.platform.api.service.ActivityMemberInfoService;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 会员推广管理Service业务层处理
 *
 * @author 77tv
 * @date 2021-03-19
 */
@Service
public class ActivityMemberInfoServiceImpl implements ActivityMemberInfoService {
    @Resource
    private ActivityMemberInfoMapper activityMemberInfoMapper;

    /**
     * 查询会员推广管理
     *
     * @param id 会员推广管理ID
     *
     * @return 会员推广管理
     */
    @Override
    public ActivityMemberInfo selectActivityMemberInfoById( String id ) {
        return activityMemberInfoMapper.selectActivityMemberInfoById( id );
    }

    /**
     * 查询会员推广管理列表
     *
     * @param activityMemberInfo 会员推广管理
     *
     * @return 会员推广管理
     */
    @Override
    public List<ActivityMemberInfo> selectActivityMemberInfoList( ActivityMemberInfo activityMemberInfo ) {
        return activityMemberInfoMapper.selectActivityMemberInfoList( activityMemberInfo );
    }

    @Override
    public List<Map> selectIpList( ActivityMemberInfo activityMemberInfo ) {
        return activityMemberInfoMapper.selectIpList( activityMemberInfo );
    }
}
