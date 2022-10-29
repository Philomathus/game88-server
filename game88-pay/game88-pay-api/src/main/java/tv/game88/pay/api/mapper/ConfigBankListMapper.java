package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.ConfigBankList;

import java.util.List;

public interface ConfigBankListMapper extends BaseMapper<ConfigBankList> {
    /**
     * 查询银行卡列表列表
     *
     * @param bankList 银行卡列表
     * @return 银行卡列表集合
     */
    public List<ConfigBankList> selectBankListList( ConfigBankList bankList);

    List<ConfigBankList> findByEffect();
}