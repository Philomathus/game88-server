package tv.game88.admin.system.service.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.admin.system.service.ConfigOssService;
import tv.game88.core.config.entity.ConfigOss;
import tv.game88.core.config.mapper.ConfigOssMapper;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * oss文件存储服务配置Service implementation业务层处理
 *
 * @author Rajesh
 * @date 2022-10-26
 */

@Service
public class ConfigOssServiceImpl implements ConfigOssService {

    @Resource
    private ConfigOssMapper configOssMapper;

    /**
     * 查询oss文件存储服务配置列表服务实施
     * select all ConfigOss service implementation
     * @param configOss oss文件存储服务配置
     * @return oss文件存储服务配置集合
     */

    @Override
    public List<ConfigOss> list(ConfigOss configOss) {
        return configOssMapper.selectConfigOssList(configOss);
    }


    /**
     * 按 ID 选择 configOss服务实施
     * select configOss By Id service implementation
     * @param id oss
     * @return 结果
     */
    @Override
    public ConfigOss selectConfigOssById(Long id) {
        return configOssMapper.selectById(id);
    }

    /**
     * 修改oss文件存储服务配置服务实施
     * insert Oss config service implementation
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    @Override
    public int insertConfigOss(ConfigOss configOss) {
        configOss.setUpdateTime(LocalDateTime.now());
        return configOssMapper.insert(configOss);
    }

    /**
     * 修改oss文件存储服务配置服务实施
     * Modify the service configuration service implementation
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    @Override
    public int updateConfigOss(ConfigOss configOss) {
        configOss.setUpdateTime(LocalDateTime.now());
        return configOssMapper.updateConfigOss(configOss);
    }

    /**
     * 批量删除oss文件存储服务配置服务实施
     * delete configOss by Ids service implementation
     * @param ids 需要删除的oss文件存储服务配置ID
     * @return 结果
     */
    @Override
    public int deleteConfigOssByIds(Long[] ids) {
        return configOssMapper.deleteConfigOssDataByIds(ids);
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public int effect(long id) {
        List<ConfigOss> configOssList = configOssMapper.selectConfigOssList( null );
        for ( ConfigOss configOss : configOssList ) {
            ConfigOss update = new ConfigOss();
            update.setId( configOss.getId() );
            update.setIsEffect( 0 );
            configOssMapper.updateConfigOss(update);
        }
        ConfigOss update = new ConfigOss();
        update.setId( id );
        update.setIsEffect( 1 );
        int i =  configOssMapper.updateConfigOss(update);
//        if ( i > 0 ) {
//            serverOssCacheUtil.clear();
//        }
        return i;
    }
}
