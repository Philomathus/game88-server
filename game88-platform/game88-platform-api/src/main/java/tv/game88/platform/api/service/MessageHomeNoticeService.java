package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.MessageHomeNotice;

import java.util.List;

/**
 * 首页公告Service接口
 *
 * @author MengJun
 */
public interface MessageHomeNoticeService extends IService<MessageHomeNotice> {
    /**
     * 查询首页公告列表
     *
     * @param messageHomeNotice 首页公告
     *
     * @return 首页公告集合
     */
    public List<MessageHomeNotice> selectMessageHomeNoticeList( MessageHomeNotice messageHomeNotice );
}