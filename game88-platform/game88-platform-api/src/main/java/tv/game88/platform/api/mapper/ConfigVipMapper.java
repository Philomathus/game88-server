package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.ConfigVip;

import java.util.List;

public interface ConfigVipMapper extends BaseMapper<ConfigVip> {
    List<ConfigVip> selectConfigVipList( ConfigVip configVip );
}