package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.member.dto.RspVipSet;
import tv.game88.core.member.entity.ConfigVip;

import java.util.List;

public interface ConfigVipMapper extends BaseMapper<ConfigVip> {
    List<ConfigVip> selectConfigVipList( ConfigVip configVip );

    List<RspVipSet> findListForCache();
}