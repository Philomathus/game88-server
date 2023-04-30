package tv.game88.platform.api.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.SpeakIpBlackList;
import tv.game88.platform.api.mapper.SpeakIpBlackListMapper;
import tv.game88.platform.api.service.SpeakIpBlackListService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 封禁IPService业务层处理
 *
 * @author 77tv
 * @date 2021-02-22
 */
@Service
public class SpeakIpBlackListServiceImpl implements SpeakIpBlackListService {
    @Resource
    private SpeakIpBlackListMapper speakIpBlackListMapper;

    /**
     * 查询封禁IP
     *
     * @param id 封禁IPID
     * @return 封禁IP
     */
    @Override
    public SpeakIpBlackList selectSpeakIpBlackListById(String id) {
        return speakIpBlackListMapper.selectSpeakIpBlackListById(id);
    }

    /**
     * 查询封禁IP列表sdasdasd
     *
     * @param speakIpBlackList 封禁IP
     * @return 封禁IP
     */
    @Override
    public List<SpeakIpBlackList> selectSpeakIpBlackListList(SpeakIpBlackList speakIpBlackList) {
        return speakIpBlackListMapper.selectSpeakIpBlackListList(speakIpBlackList);
    }

    /**
     * 新增封禁IP
     *
     * @param speakIpBlackList 封禁IP
     * @return 结果
     */
    @Override
    public int insertSpeakIpBlackList(SpeakIpBlackList speakIpBlackList) {
        speakIpBlackList.setCreateTime(LocalDateTime.now());
        return this.speakIpBlackListMapper.insertSpeakIpBlackList( speakIpBlackList );
    }

    /**
     * 批量删除封禁IP
     *
     * @param ids 需要删除的封禁IPID
     * @return 结果
     */
    @Override
    public int deleteSpeakIpBlackListByIds(String[] ids) {
        return speakIpBlackListMapper.deleteSpeakIpBlackListByIds(ids);
    }

    /**
     * 删除封禁IP信息
     *
     * @param id 封禁IPID
     * @return 结果
     */
    @Override
    public int deleteSpeakIpBlackListById(String id) {
        return speakIpBlackListMapper.deleteSpeakIpBlackListById(id);
    }
}