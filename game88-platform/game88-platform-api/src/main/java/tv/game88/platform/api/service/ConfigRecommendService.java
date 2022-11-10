package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.member.entity.ConfigRecommend;

import java.util.List;

/**
 * 推广设置Service接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ConfigRecommendService extends IService<ConfigRecommend> {

    /**
     * 查询推广设置列表
     *
     * @param configRecommend 推广设置
     *
     * @return 推广设置集合
     */
    public List<ConfigRecommend> selectConfigRecommendList( ConfigRecommend configRecommend );
}
