package tv.game88.admin.system.service;

import tv.game88.core.config.entity.ConfigOss;

import java.util.List;

/**
 * oss文件存储服务配置Service业务层处理
 *
 * @author Rajesh
 * @date 2022-10-26
 */
public interface ConfigOssService {

    List<ConfigOss> list(ConfigOss configOss);

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
    public int deleteConfigOssById( Long id );

    int effect( long id );

}
