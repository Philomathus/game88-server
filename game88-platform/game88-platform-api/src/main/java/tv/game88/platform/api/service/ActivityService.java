package tv.game88.platform.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.dto.RspActivityInfo;
import tv.game88.platform.api.dto.RspActivityType;
import tv.game88.platform.api.dto.RspQuestInfo;
import tv.game88.platform.api.dto.RspQuestType;

import java.util.List;

public interface ActivityService {
    List<RspActivityType> getActivityTypes();

    List<RspActivityInfo> getActivityInfos( Long typeId, String memberId );

    List<RspQuestType> getActivityQuestTypes( String memberId );

    List<RspQuestInfo> getActivityQuestInfos( Long typeId, String memberId );

    RspBase<?> receiveQuestReward( Long id, String memberId );
}
