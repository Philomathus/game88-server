package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.MemberQuest;

import java.util.List;

public interface MemberQuestMapper extends BaseMapper<MemberQuest> {
    List<MemberQuest> selectMemberQuestList( MemberQuest memberQuest );

    /**
     * 重置每日任务
     */
    void resetDayTaskStatus();
}
