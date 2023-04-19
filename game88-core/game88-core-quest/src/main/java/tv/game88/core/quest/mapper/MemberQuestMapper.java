package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.MemberQuest;

import java.util.List;

public interface MemberQuestMapper extends BaseMapper<MemberQuest> {

    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    MemberQuest selectMemberQuestById(String id);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param memberQuest 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    List<MemberQuest> selectMemberQuestList( MemberQuest memberQuest );

    /**
     * 重置每日任务
     */
    void resetDayTaskStatus();

    /**
     * 新增【请填写功能名称】
     *
     * @param memberQuest 【请填写功能名称】
     * @return 结果
     */
    int insertMemberQuest(MemberQuest memberQuest);

    /**
     * 修改【请填写功能名称】
     *
     * @param memberQuest 【请填写功能名称】
     * @return 结果
     */
    int updateMemberQuest(MemberQuest memberQuest);

    /**
     * 删除【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    int deleteMemberQuestById(String id);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMemberQuestByIds(String[] ids );
}
