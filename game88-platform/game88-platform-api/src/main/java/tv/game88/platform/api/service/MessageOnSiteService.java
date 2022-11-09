package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.MessageOnSite;

import java.util.List;

/**
 * 站内信Service接口
 *
 * @author MengJun
 */
public interface MessageOnSiteService extends IService<MessageOnSite> {
    /**
     * 查询站内信列表
     *
     * @param messageOnSite 站内信
     *
     * @return 站内信集合
     */
    public List<MessageOnSite> selectMessageOnSiteList( MessageOnSite messageOnSite );
}