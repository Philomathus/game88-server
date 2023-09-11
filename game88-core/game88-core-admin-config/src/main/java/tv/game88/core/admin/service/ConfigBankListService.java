package tv.game88.core.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.config.entity.ConfigBankList;

import java.util.List;

public interface ConfigBankListService extends IService<ConfigBankList> {

    List<ConfigBankList> selectConfigBankListList( ConfigBankList configBankList );
}

