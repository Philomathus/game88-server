package tv.game88.platform.api.service.impl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.entity.ConfigEnvironment;
import tv.game88.core.config.mapper.ConfigEnvironmentMapper;
import tv.game88.platform.api.service.ConfigEnvironmentService;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 环境参数配置Service业务层处理
 *
 * @author MengJun
 */
@Service
public class ConfigEnvironmentServiceImpl implements ConfigEnvironmentService {
    @Resource
    private ConfigEnvironmentMapper configEnvironmentMapper;
    @Resource
    private ConfigEnvCacheUtil      configEnvCacheUtil;

    @Resource
    private ConfigDomainCacheUtil configDomainCacheUtil;

    /**
     * 查询环境参数配置
     *
     * @param envCode 环境参数配置ID
     *
     * @return 环境参数配置
     */
    @Override
    public ConfigEnvironment selectConfigEnvironmentById( String envCode ) {
        return configEnvironmentMapper.selectById( envCode );
    }

    /**
     * 查询环境参数配置列表
     *
     * @param configEnvironment 环境参数配置
     *
     * @return 环境参数配置
     */
    @Override
    public List<ConfigEnvironment> selectConfigEnvironmentList( ConfigEnvironment configEnvironment ) {
        return configEnvironmentMapper.selectConfigEnvironmentList( configEnvironment );
    }

    /**
     * 新增环境参数配置
     *
     * @param configEnvironment 环境参数配置
     *
     * @return 结果
     */
    @Override
    public int insertConfigEnvironment( ConfigEnvironment configEnvironment ) {
        //判断名称是否存在
        if ( configEnvironmentMapper.checkType( configEnvironment.getEnvTitle() ) != 0 ) {
            throw new BusinessException( "名称已存在" );
        }
        //判断编码是否存在
        if ( configEnvironmentMapper.checkCode( configEnvironment.getEnvCode() ) != 0 ) {
            throw new BusinessException( "编码已存在" );
        }
        int i = configEnvironmentMapper.insert( configEnvironment );
        if ( i > 0 ) {
            ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
            if ( saveConf.getEnvStatus() == 1 ) {
                configEnvCacheUtil.setConfCache( saveConf );
            }
        }
        return i;
    }

    /**
     * 修改环境参数配置
     *
     * @param configEnvironment 环境参数配置
     *
     * @return 结果
     */
    @Override
    public int updateConfigEnvironment( ConfigEnvironment configEnvironment ) {
        int i = configEnvironmentMapper.updateById( configEnvironment );
        if ( i > 0 ) {
            ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
            if ( saveConf.getEnvStatus() == 1 ) {
                configEnvCacheUtil.setConfCache( saveConf );
            }
        }
        return i;
    }

    @Override
    public int changeStatus( ConfigEnvironment configEnvironment ) {
        int i = configEnvironmentMapper.updateById( configEnvironment );
        if ( i > 0 ) {
            if ( configEnvironment.getEnvStatus() == 1 ) {
                ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
                configEnvCacheUtil.setConfCache( saveConf );
            } else if ( configEnvironment.getEnvStatus() == 0 ) {
                configEnvCacheUtil.deleteCache( configEnvironment.getEnvCode() );
            }
        }
        return i;
    }

    /**
     * 批量删除环境参数配置
     *
     * @param envCodes 需要删除的环境参数配置ID
     *
     * @return 结果
     */
    @Override
    public int deleteConfigEnvironmentByIds( String[] envCodes ) {
        int i = configEnvironmentMapper.deleteBatchIds( Arrays.asList( envCodes ) );
        if ( i > 0 ) {
            configEnvCacheUtil.deleteCache( envCodes );
        }
        return i;
    }

    @Override
    public int getTitleIndex( String title, String code ) {
        return configEnvironmentMapper.getTitleIndex( title, code );
    }

    @Override
    public void refreshCache() {
        configEnvCacheUtil.refreshConfCache();
    }

    /**
     * 查询环境参数配置列表
     *
     * @param configEnvironment 环境参数配置
     *
     * @return 环境参数配置
     */
    @Override
    public List<ConfigEnvironment> selectConfigRecommendPic( ConfigEnvironment configEnvironment ) {
        List<ConfigEnvironment> configEnvironments = configEnvironmentMapper.selectConfigRecommendPic( configEnvironment );
        String domainValue = configDomainCacheUtil.getDomainOssValue();
        for ( ConfigEnvironment co : configEnvironments ) {
            if ( StringUtils.isNotBlank( co.getEnvValue() ) && co.getEnvValue().startsWith( "${domain.oss}" ) && !co.getEnvValue().startsWith( "http" ) ) {
                co.setEnvValue(co.getEnvValue().replace("${domain.oss}",domainValue));
            }
        }
        return configEnvironments;
    }

}
