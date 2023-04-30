package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.MemberQuest;

import java.util.List;

public interface MemberQuestMapper extends BaseMapper<MemberQuest> {

    /**
     * 查询会员任务
     *
     * @param id 会员任务ID
     * @return 会员任务
     */
    MemberQuest selectMemberQuestById(String id);

    /**
     * 查询会员任务列表
     *
     * @param memberQuest 会员任务
     * @return 会员任务集合
     */
    List<MemberQuest> selectMemberQuestList( MemberQuest memberQuest );

    /**
     * 重置每日任务
     */
    void resetDayTaskStatus();

    /**
     * 新增会员任务
     *
     * @param memberQuest 会员任务
     * @return 结果
     */
    int insertMemberQuest(MemberQuest memberQuest);

    /**
     * 修改会员任务
     *
     * @param memberQuest 会员任务
     * @return 结果
     */
    int updateMemberQuest(MemberQuest memberQuest);

    /**
     * 删除会员任务
     *
     * @param id 会员任务ID
     * @return 结果
     */
    int deleteMemberQuestById(String id);

    /**
     * 批量删除会员任务
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMemberQuestByIds(String[] ids );
}
