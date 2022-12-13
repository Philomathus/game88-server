package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.MobileLimit;

import java.util.List;

/**
 * Service接口
 *
 * @author MengJun
 */
public interface MobileLimitService extends IService<MobileLimit> {
    /**
     * 查询列表
     *
     * @param mobileLimit
     *
     * @return 集合
     */
    public List<MobileLimit> selectMobileLimitList( MobileLimit mobileLimit );
}