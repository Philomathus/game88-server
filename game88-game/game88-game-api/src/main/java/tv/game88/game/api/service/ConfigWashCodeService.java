package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.ConfigWashCode;
import java.util.List;

/**
 * 洗码配置Service接口
 *
 * @author krzystof
 */
public interface ConfigWashCodeService extends IService<ConfigWashCode> {

    /**
     * 查询洗码配置列表
     *
     * @param configCleanCode 洗码配置
     *
     * @return 洗码配置集合
     */
    List<ConfigWashCode> selectConfigWashCodeList(ConfigWashCode configCleanCode);
}
