package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.ConfigCleanCode;
import java.util.List;

/**
 * 洗码配置Service接口
 *
 * @author krzystof
 */
public interface ConfigCleanCodeService extends IService<ConfigCleanCode> {

    /**
     * 查询洗码配置列表
     *
     * @param configCleanCode 洗码配置
     *
     * @return 洗码配置集合
     */
    List<ConfigCleanCode> selectConfigCleanCodeList(ConfigCleanCode configCleanCode);
}
