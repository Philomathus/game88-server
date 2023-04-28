package tv.game88.platform.api.service;

import tv.game88.core.config.entity.ConfigOss;

import java.util.List;

/**
 * oss文件存储服务配置Service业务层处理
 *
 * @author Rajesh
 */
public interface ConfigOssService {

    List<ConfigOss> selectConfigOssList(ConfigOss configOss, boolean hideAccess );

    public ConfigOss selectConfigOssById( Long id );

    /**
     * 新增oss文件存储服务配置
     *
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    public int insertConfigOss( ConfigOss configOss );

    /**
     * 修改oss文件存储服务配置
     *
     * @param configOss oss文件存储服务配置
     * @return 结果
     */
    public int updateConfigOss( ConfigOss configOss );

    /**
     * 批量删除oss文件存储服务配置
     *
     * @param ids 需要删除的oss文件存储服务配置ID
     * @return 结果
     */
    public int deleteConfigOssByIds( Long[] ids );

    /**
     * 删除oss文件存储服务配置信息
     *
     * @param id oss文件存储服务配置ID
     * @return 结果
     */

    int effect( long id );

}
