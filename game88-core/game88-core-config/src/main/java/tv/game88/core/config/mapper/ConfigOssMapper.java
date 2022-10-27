package tv.game88.core.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.config.entity.ConfigOss;

import java.util.List;

/**
 * oss文件存储服务配置Mapper接口
 *
 * @author MengJun
 */
public interface ConfigOssMapper extends BaseMapper<ConfigOss> {

	/**
	 * 查询oss文件存储服务配置列表
	 *
	 * @param configOss oss文件存储服务配置
	 * @return oss文件存储服务配置集合
	 */
	public List<ConfigOss> selectConfigOssList(ConfigOss configOss);

	ConfigOss selectConfigOssByEffect();

	int deleteConfigOssDataByIds(Long[] ids);

	int updateConfigOss(ConfigOss configOss);

}
