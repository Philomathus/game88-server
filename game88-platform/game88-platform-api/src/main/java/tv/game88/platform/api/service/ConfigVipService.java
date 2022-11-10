package tv.game88.platform.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.ConfigVip;

import java.util.List;

public interface ConfigVipService extends IService<ConfigVip> {
    public List<ConfigVip> selectConfigVipList( ConfigVip configVip );
}