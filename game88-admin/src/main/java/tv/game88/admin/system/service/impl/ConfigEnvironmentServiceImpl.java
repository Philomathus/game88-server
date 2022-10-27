package tv.game88.admin.system.service.impl;

import org.springframework.stereotype.Service;
import tv.game88.admin.system.cache.DictUtils;
import tv.game88.admin.system.entity.SysDictData;
import tv.game88.admin.system.mapper.SysDictDataMapper;
import tv.game88.admin.system.service.ConfigEnvironmentService;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.entity.ConfigEnvironment;
import tv.game88.core.config.mapper.ConfigEnvironmentMapper;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 环境参数配置Service业务层处理
 *
 * @author MengJun
 * @date 2021-01-27
 */
@Service
public class ConfigEnvironmentServiceImpl implements ConfigEnvironmentService {
    @Resource
    private ConfigEnvironmentMapper configEnvironmentMapper;
    @Resource
    private SysDictDataMapper       dictDataMapper;
    @Resource
    private ConfigEnvCacheUtil      configEnvCacheUtil;
    @Resource
    private DictUtils               dictUtils;

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

    @Override
    public ConfigEnvironment selectConfigEnvironmentByIdTwo( String envCode ) {
        ConfigEnvironment configEnvironment = configEnvironmentMapper.selectById( envCode );
        String            domainValue       = ConfigDomainCacheUtil.me.getValue( "domain.oss" );
        configEnvironment.setEnvValue( configEnvironment.getEnvValue().replace( "${domain.oss}", domainValue ) );
        return configEnvironment;
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

    @Override
    public List<ConfigEnvironment> selectConfigEnvironmentTwo( ConfigEnvironment configEnvironment ) {
        List<ConfigEnvironment> configEnvironments = configEnvironmentMapper.selectConfigEnvironmentTwo( configEnvironment );
        String                  domainValue        = ConfigDomainCacheUtil.me.getValue( "domain.oss" );
        for ( ConfigEnvironment co : configEnvironments ) {
            if (StringUtils.isNotBlank( co.getEnvValue() ) && co.getEnvValue().startsWith( "${domain.oss}" )
                    && !co.getEnvValue().startsWith( "http" )) {
                co.setEnvValue( co.getEnvValue().replace( "${domain.oss}", domainValue ) );
            }
        }
        return configEnvironments;
    }

    /**
     * 新增环境参数配置
     *
     * @param configEnvironment 环境参数配置
     *
     * @return 结果
     */
    @Override
    public int insertConfigEnvironment( ConfigEnvironment configEnvironment ) throws Exception {
        if ("M".equals( configEnvironment.getMenuType() )) {
            //判断名称是否存在
            if (configEnvironmentMapper.checkType( configEnvironment.getEnvTitle() ) != 0) {
                throw new Exception( "名称已存在" );
            }
            //判断编码是否存在
            if (configEnvironmentMapper.checkCode( configEnvironment.getEnvCode() ) != 0) {
                throw new Exception( "编码已存在" );
            }
            SysDictData dictData = new SysDictData();
            dictData.setDictSort( configEnvironment.getEnvSort() );
            dictData.setDictLabel( configEnvironment.getEnvTitle() );
            String value = configEnvironmentMapper.getValue();
            dictData.setDictValue( ( int ) (Float.parseFloat( value ) + 1) + "" );
            dictData.setDictType( "config_environment_group" );
            dictData.setStatus( String.valueOf( 0 ) );
            //加入Redis缓存
            List<SysDictData> dictDataList = dictUtils.getDictCache( "config_environment_group" );
            dictDataList.add( dictData );
            dictUtils.setDictCache( "config_environment_group", dictDataList );
            //加入数据库
            return dictDataMapper.insertDictData( dictData );
        } else {
            //判断名称是否存在
            if (configEnvironmentMapper.checkType2( configEnvironment.getEnvTitle() ) != 0) {
                throw new Exception( "名称已存在" );
            }
            //判断编码是否存在
            if (configEnvironmentMapper.checkCode2( configEnvironment.getEnvCode() ) != 0) {
                throw new Exception( "编码已存在" );
            }
            int i = configEnvironmentMapper.insert( configEnvironment );
            if (i > 0) {
                ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
                if (saveConf.getEnvStatus() == 1) {
                    configEnvCacheUtil.setConfCache( saveConf );
                }
            }
            return i;
        }
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
        if (i > 0) {
            ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
            if (saveConf.getEnvStatus() == 1) {
                configEnvCacheUtil.setConfCache( saveConf );
            }
        }
        return i;
    }

    @Override
    public int changeStatus( ConfigEnvironment configEnvironment ) {
        int i = configEnvironmentMapper.updateById( configEnvironment );
        if (i > 0) {
            if (configEnvironment.getEnvStatus() == 1) {
                ConfigEnvironment saveConf = configEnvironmentMapper.selectById( configEnvironment.getEnvCode() );
                configEnvCacheUtil.setConfCache( saveConf );
            } else if (configEnvironment.getEnvStatus() == 0) {
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
        if (i > 0) {
            configEnvCacheUtil.deleteCache( envCodes );
        }
        return i;
    }

    /**
     * 删除环境参数配置信息
     *
     * @param envCode 环境参数配置ID
     *
     * @return 结果
     */
    @Override
    public int deleteConfigEnvironmentById( String envCode ) {
        int i = configEnvironmentMapper.deleteById( envCode );
        if (i > 0) {
            configEnvCacheUtil.deleteCache( envCode );
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
}
