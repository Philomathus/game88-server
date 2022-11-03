package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.member.entity.ConfigRecommend;

import java.util.List;

/**
 * Created by admin
 */
public interface ConfigRecommendMapper extends BaseMapper<ConfigRecommend> {
    /**
     * 查询推广设置列表
     *
     * @param configRecommend 推广设置
     * @return 推广设置集合
     */
    public List<ConfigRecommend> selectConfigRecommendList( ConfigRecommend configRecommend );
}
