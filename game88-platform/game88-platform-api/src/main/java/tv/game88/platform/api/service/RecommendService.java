package tv.game88.platform.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.RspMemberRecommend;
import tv.game88.core.member.dto.RspMyRecommend;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.member.entity.MemberRecommend;
import tv.game88.platform.api.dto.RspDetailCommission;

import java.util.List;

public interface RecommendService {
    List<RspMemberRecommend> getRecommendDetailList( String code, String memberId );

    RspMyRecommend getRecommendDetail( String memberId );

    RspBase<?> receiveRecommendReward( String memberId );

    void receiveReward( String memberId, List<MemberRecommend> listRecommend );

    List<RspDetailCommission> getRecommendRewardDetailList( String memberId );

    List<ConfigRecommend> getRecommendDesc();
}
