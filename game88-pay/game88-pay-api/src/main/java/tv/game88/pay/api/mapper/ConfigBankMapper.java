package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.dto.RspConfigBank;
import tv.game88.pay.api.entity.ConfigBank;

import java.util.List;

public interface ConfigBankMapper extends BaseMapper<ConfigBank> {

	public List<ConfigBank> selectConfigBankList(ConfigBank configBank);

    List<RspConfigBank> selectRspList( Integer vip );
}
