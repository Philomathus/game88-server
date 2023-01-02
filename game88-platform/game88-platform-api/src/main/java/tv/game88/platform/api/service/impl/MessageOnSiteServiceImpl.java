package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.MessageOnSite;
import tv.game88.platform.api.mapper.MessageOnSiteMapper;
import tv.game88.platform.api.service.MessageOnSiteService;

import java.util.List;

/**
 * 站内信Service业务层处理
 *
 * @author MengJun
 */
@Service
public class MessageOnSiteServiceImpl extends ServiceImpl<MessageOnSiteMapper, MessageOnSite> implements MessageOnSiteService {
    /**
     * 查询站内信列表
     *
     * @param messageOnSite 站内信
     *
     * @return 站内信
     */
    @Override
    public List<MessageOnSite> selectMessageOnSiteList( MessageOnSite messageOnSite ) {
        return this.baseMapper.selectMessageOnSiteList( messageOnSite );
    }

    /**
     * 新增站内信息
     *
     * @param messageOnSite 站内信息
     * @return 结果
     */
    @Override
    public int insertMessageOnSite( MessageOnSite messageOnSite ) {
        return this.baseMapper.insertMessageOnSite(messageOnSite);
    }
}