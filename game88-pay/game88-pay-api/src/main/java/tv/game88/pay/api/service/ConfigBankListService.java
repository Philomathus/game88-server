package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.ConfigBankList;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConfigBankListService extends IService<ConfigBankList> {

    List<ConfigBankList> selectConfigBankListList( ConfigBankList configBankList );
}

