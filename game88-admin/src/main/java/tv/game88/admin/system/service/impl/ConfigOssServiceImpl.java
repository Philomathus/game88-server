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

    @Override
    public List<ConfigOss> list(ConfigOss configOss) {
        return configOssMapper.selectConfigOssList(configOss);
    }

    @Override
    public ConfigOss selectConfigOssById(Long id) {
        return configOssMapper.selectById(id);
    }

    @Override
    public int insertConfigOss(ConfigOss configOss) {
        configOss.setUpdateTime(LocalDateTime.now());
        return configOssMapper.insert(configOss);
    }

    @Override
    public int updateConfigOss(ConfigOss configOss) {
        configOss.setUpdateTime(LocalDateTime.now());
        return configOssMapper.updateConfigOss(configOss);
    }

    @Override
    public int deleteConfigOssByIds(Long[] ids) {
        return configOssMapper.deleteConfigOssDataByIds(ids);
    }

    @Override
    public int deleteConfigOssById(Long id) {
        return configOssMapper.deleteById(id);
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
