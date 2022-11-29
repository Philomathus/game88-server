package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.mapper.ConfigVipMapper;
import tv.game88.platform.api.service.ConfigVipService;

import java.util.List;

@Service
public class ConfigVipServiceImpl extends ServiceImpl<ConfigVipMapper, ConfigVip> implements ConfigVipService {

    @Override
    public List<ConfigVip> selectConfigVipList( ConfigVip configVip ) {
        return this.baseMapper.selectConfigVipList( configVip );
    }
}
