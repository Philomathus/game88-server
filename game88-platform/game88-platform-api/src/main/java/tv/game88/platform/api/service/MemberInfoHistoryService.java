package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.entity.MemberInfoHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public interface MemberInfoHistoryService extends IService<MemberInfoHistory> {

    List<MemberInfoHistory> memberInfoHistoryList( MemberInfoHistory memberInfoHistory);

    BigDecimal getHistoryRecharge( String memberId );

    List<MemberCard> selectMemberCardList( String memberId );

    RspBase<?> personalReport( String startTime, String endTime, String memberId );

    Map listCount( MemberInfoHistory memberInfoHistory );
}
