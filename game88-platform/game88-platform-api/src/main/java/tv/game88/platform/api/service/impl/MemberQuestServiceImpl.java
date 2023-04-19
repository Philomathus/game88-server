package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.core.quest.mapper.MemberQuestMapper;
import tv.game88.platform.api.service.MemberQuestService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会员任务列表Service业务层处理
 *
 * @author jake from 77tv
 * @date 2021-03-20
 */
@Service
public class MemberQuestServiceImpl implements MemberQuestService {

    @Resource
    private MemberQuestMapper memberQuestMapper;

    /**
     * 查询会员任务列表
     *
     * @param id 系统编号
     * @return 会员任务
     */
    @Override
    public MemberQuest selectMemberQuestById(String id) {
        return memberQuestMapper.selectMemberQuestById(id);
    }

    /**
     * 查询会员任务列表
     *
     * @param memberQuest 会员任务
     * @return 会员任务列表
     */
    @Override
    public List<MemberQuest> selectMemberQuestList(MemberQuest memberQuest) {
        return memberQuestMapper.selectMemberQuestList(memberQuest);
    }

    @Override
    public int addMemberScore(MemberQuest memberQuest) {
        if(memberQuestMapper.selectMemberQuestById(memberQuest.getId()).getStatus()>0){
            return  0;
        }
        return memberQuestMapper.updateMemberQuest(memberQuest);
    }


    /**
     * 新增会员任务
     *
     * @param memberQuest 【请填写功能名称】
     * @return 执行结果
     */
    @Override
    public int insertMemberQuest(MemberQuest memberQuest) {
        return memberQuestMapper.insertMemberQuest(memberQuest);
    }

    /**
     * 修改会员任务
     *
     * @param memberQuest 会员任务
     * @return 执行结果
     */
    @Override
    public int updateMemberQuest(MemberQuest memberQuest) {
        return memberQuestMapper.updateMemberQuest(memberQuest);
    }

    /**
     * 批量删除会员任务
     *
     * @param ids 系统编号
     * @return 执行结果
     */
    @Override
    public int deleteMemberQuestByIds(String[] ids) {
        return memberQuestMapper.deleteMemberQuestByIds(ids);
    }

    /**
     * 删除会员任务
     *
     * @param id 系统编号
     * @return 执行结果
     */
    @Override
    public int deleteMemberQuestById(String id) {
        return memberQuestMapper.deleteMemberQuestById(id);
    }
}