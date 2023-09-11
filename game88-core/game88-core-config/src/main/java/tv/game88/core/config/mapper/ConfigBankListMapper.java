package tv.game88.core.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.config.entity.ConfigBankList;

import java.util.List;

public interface ConfigBankListMapper extends BaseMapper<ConfigBankList> {
    /**
     * 查询银行卡列表列表
     *
     * @param bankList 银行卡列表
     * @return 银行卡列表集合
     */
    public List<ConfigBankList> selectConfigBankListList( ConfigBankList bankList);

    Long findBankIdByNameOrCode( String bankName );
}