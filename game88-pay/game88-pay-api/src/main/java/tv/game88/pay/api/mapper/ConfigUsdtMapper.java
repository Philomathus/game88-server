package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.ConfigUsdt;

import java.util.List;

public interface ConfigUsdtMapper extends BaseMapper<ConfigUsdt> {
    public List<ConfigUsdt> selectConfigUsdtList( ConfigUsdt payRechargeUsdt );
}