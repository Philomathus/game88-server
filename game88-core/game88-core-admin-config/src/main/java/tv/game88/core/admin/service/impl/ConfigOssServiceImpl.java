package tv.game88.core.admin.service.impl;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.core.admin.service.ConfigOssService;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigOssCacheUtil;
import tv.game88.core.config.entity.ConfigOss;
import tv.game88.core.config.mapper.ConfigOssMapper;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * oss文件存储服务配置Service implementation业务层处理
 *
 * @author Rajesh
 */

@Service
public class ConfigOssServiceImpl implements ConfigOssService {

    @Resource
    private ConfigOssMapper configOssMapper;
    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;

    /**
     * 查询oss文件存储服务配置列表服务实施
     * select all ConfigOss service implementation
     *
     * @param configOss oss文件存储服务配置
     * @return oss文件存储服务配置集合
     */

    @Override
    public List<ConfigOss> selectConfigOssList(ConfigOss configOss, boolean hideAccess) {
        List<ConfigOss> configOssList = configOssMapper.selectConfigOssList(configOss);

        if (hideAccess) {
            configOssList.forEach(( r) -> {
                        r.setAccessKey("");
                        r.setAccessSecret("");
                    }
            );
        }

        return configOssList;
    }


    /**
     * 按 ID 选择 configOss服务实施
     * select configOss By Id service implementation
     *
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
     *
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    @Override
    public int insertConfigOss(ConfigOss configOss) {
        return configOssMapper.insert(configOss);
    }

    /**
     * 修改oss文件存储服务配置服务实施
     * Modify the service configuration service implementation
     *
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    @Override
    public int updateConfigOss(ConfigOss configOss) {
        int i = configOssMapper.updateById(configOss);
        if (i > 0) {
            configOssCacheUtil.clear();
            ConfigDomainCacheUtil.me.clearDomainOss();
        }
        return i;
    }

    /**
     * 批量删除oss文件存储服务配置服务实施
     * delete configOss by Ids service implementation
     *
     * @param ids 需要删除的oss文件存储服务配置ID
     * @return 结果
     */
    @Override
    public int deleteConfigOssByIds(Long[] ids) {
        return configOssMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int effect(long id) {
        List<ConfigOss> configOssList = configOssMapper.selectConfigOssList(null);
        for (ConfigOss configOss : configOssList) {
            ConfigOss update = new ConfigOss();
            update.setId(configOss.getId());
            update.setEffect(false);
            configOssMapper.updateById(update);
        }
        ConfigOss update = new ConfigOss();
        update.setId(id);
        update.setEffect(true);
        int i = configOssMapper.updateById(update);
        if (i > 0) {
            configOssCacheUtil.clear();
            ConfigDomainCacheUtil.me.clearDomainOss();
        }
        return i;
    }
}
