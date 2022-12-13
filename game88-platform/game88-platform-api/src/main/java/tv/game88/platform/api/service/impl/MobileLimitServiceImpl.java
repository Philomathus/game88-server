package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.MobileLimit;
import tv.game88.platform.api.mapper.MobileLimitMapper;
import tv.game88.platform.api.service.MobileLimitService;

import java.util.List;

/**
 * Service业务层处理
 *
 * @author MengJun
 */
@Service
public class MobileLimitServiceImpl extends ServiceImpl<MobileLimitMapper, MobileLimit> implements MobileLimitService {
    /**
     * 查询列表
     *
     * @param mobileLimit
     */
    @Override
    public List<MobileLimit> selectMobileLimitList( MobileLimit mobileLimit ) {
        return this.baseMapper.selectMobileLimitList( mobileLimit );
    }
}