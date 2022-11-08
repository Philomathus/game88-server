package tv.game88.platform.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.cache.ActivityCacheUtil;
import tv.game88.platform.api.dto.RspActivityInfo;
import tv.game88.platform.api.dto.RspActivityType;
import tv.game88.platform.api.dto.RspQuestInfo;
import tv.game88.platform.api.dto.RspQuestType;
import tv.game88.platform.api.service.ActivityService;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private ActivityCacheUtil activityCacheUtil;

    @Override
    public List<RspActivityType> getActivityTypes() {
        return null;
    }

    @Override
    public List<RspActivityInfo> getActivityInfos( Long typeId, String memberId ) {
        return null;
    }

    @Override
    public List<RspQuestType> getActivityQuestTypes( String memberId ) {
        return null;
    }

    @Override
    public List<RspQuestInfo> getActivityQuestInfos( Long typeId, String memberId ) {
        return null;
    }

    @Override
    public RspBase<?> receiveQuestReward( Long id, String memberId ) {
        return null;
    }
}
