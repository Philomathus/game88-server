package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.platform.api.cache.MemberForbidUtil;
import tv.game88.platform.api.entity.SpeakIpBlackList;
import tv.game88.platform.api.mapper.SpeakIpBlackListMapper;
import tv.game88.platform.api.service.SpeakIpBlackListService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author 77tv
 * @date 2021-02-22
 */
@Service
public class SpeakIpBlackListServiceImpl implements SpeakIpBlackListService {
    @Resource
    private SpeakIpBlackListMapper speakIpBlackListMapper;
    @Resource
    private MemberInfoMapper memberInfoMapper;
    @Resource
    private MemberForbidUtil memberForbidUtil;

    @Resource
    private RedisUtils redisUtil;

    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    @Override
    public SpeakIpBlackList selectSpeakIpBlackListById(String id) {
        return speakIpBlackListMapper.selectSpeakIpBlackListById(id);
    }

    /**
     * 查询【请填写功能名称】列表sdasdasd
     *
     * @param speakIpBlackList 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<SpeakIpBlackList> selectSpeakIpBlackListList(SpeakIpBlackList speakIpBlackList) {
        return speakIpBlackListMapper.selectSpeakIpBlackListList(speakIpBlackList);
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param speakIpBlackList 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertSpeakIpBlackList(SpeakIpBlackList speakIpBlackList) {
        speakIpBlackList.setCreateTime(LocalDateTime.now());
        return this.speakIpBlackListMapper.insertSpeakIpBlackList( speakIpBlackList );
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param speakIpBlackList 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateSpeakIpBlackList(SpeakIpBlackList speakIpBlackList) {
        // 解封账号
        MemberInfo update = new MemberInfo();
        update.setId( speakIpBlackList.getUserId() );
        update.setStatus( 1 );
//        update.setSpeak("0");
        memberInfoMapper.updateById( update );
        memberForbidUtil.setPlatformUserSpeak( speakIpBlackList.getUserId(), false );
        speakIpBlackListMapper.deleteSpeakIp( speakIpBlackList.getUserIp());
        return 1;
    }

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteSpeakIpBlackListByIds(String[] ids) {
        return speakIpBlackListMapper.deleteSpeakIpBlackListByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     *
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteSpeakIpBlackListById(String id) {
        return speakIpBlackListMapper.deleteSpeakIpBlackListById(id);
    }
}