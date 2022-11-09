package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.MessageHomeNotice;
import tv.game88.platform.api.mapper.MessageHomeNoticeMapper;
import tv.game88.platform.api.service.MessageHomeNoticeService;

import java.util.List;

/**
 * 首页公告Service业务层处理
 *
 * @author MengJun
 */
@Service
public class MessageHomeNoticeServiceImpl extends ServiceImpl<MessageHomeNoticeMapper, MessageHomeNotice> implements MessageHomeNoticeService {
    /**
     * 查询首页公告列表
     *
     * @param messageHomeNotice 首页公告
     *
     * @return 首页公告
     */
    @Override
    public List<MessageHomeNotice> selectMessageHomeNoticeList( MessageHomeNotice messageHomeNotice ) {
        return this.baseMapper.selectMessageHomeNoticeList( messageHomeNotice );
    }
}