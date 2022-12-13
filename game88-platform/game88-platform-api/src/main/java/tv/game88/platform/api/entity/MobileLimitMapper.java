package tv.game88.platform.api.entity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * Mapper接口
 *
 * @author MengJun
 */
public interface MobileLimitMapper extends BaseMapper<MobileLimit> {

    /**
     * 查询列表
     *
     * @param mobileLimit
     *
     * @return 集合
     */
    public List<MobileLimit> selectMobileLimitList( MobileLimit mobileLimit );
}