package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.MessageOnSite;

import java.util.List;

/**
 * 站内信Mapper接口
 *
 * @author MengJun
 */
public interface MessageOnSiteMapper extends BaseMapper<MessageOnSite> {

    /**
     * 查询站内信列表
     *
     * @param messageOnSite 站内信
     *
     * @return 站内信集合
     */
    public List<MessageOnSite> selectMessageOnSiteList( MessageOnSite messageOnSite );
}