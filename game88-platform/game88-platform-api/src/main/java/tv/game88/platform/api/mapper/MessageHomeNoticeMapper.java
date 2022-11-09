package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.MessageHomeNotice;

import java.util.List;

/**
 * 首页公告Mapper接口
 *
 * @author MengJun
 */
public interface MessageHomeNoticeMapper extends BaseMapper<MessageHomeNotice> {

    /**
     * 查询首页公告列表
     *
     * @param messageHomeNotice 首页公告
     *
     * @return 首页公告集合
     */
    public List<MessageHomeNotice> selectMessageHomeNoticeList( MessageHomeNotice messageHomeNotice );
}