package tv.game88.pay.api.service;

import tv.game88.pay.api.entity.ConfigBank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ConfigBankService extends IService<ConfigBank> {
    List<ConfigBank> selectConfigBankList( ConfigBank configBank );
}

