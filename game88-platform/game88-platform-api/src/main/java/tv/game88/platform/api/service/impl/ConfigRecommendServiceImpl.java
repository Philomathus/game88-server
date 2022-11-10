package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.member.entity.ConfigRecommend;
import tv.game88.core.member.mapper.ConfigRecommendMapper;
import tv.game88.platform.api.service.ConfigRecommendService;

import java.util.List;

/**
 * 推广设置Service业务层处理
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Service
public class ConfigRecommendServiceImpl extends ServiceImpl<ConfigRecommendMapper, ConfigRecommend> implements ConfigRecommendService {

    /**
     * 查询推广设置列表
     *
     * @param configRecommend 推广设置
     *
     * @return 推广设置
     */
    @Override
    public List<ConfigRecommend> selectConfigRecommendList( ConfigRecommend configRecommend ) {
        return this.baseMapper.selectConfigRecommendList( configRecommend );
    }

}
