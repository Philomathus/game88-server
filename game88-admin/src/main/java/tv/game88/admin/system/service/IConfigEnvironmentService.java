package tv.game88.admin.system.service;

import tv.game88.core.config.entity.ConfigEnvironment;

import java.util.List;

/**
 * 环境参数配置Service接口
 *
 * @author MengJun
 * @date 2021-01-27
 */
public interface IConfigEnvironmentService {
	/**
	 * 查询环境参数配置
	 *
	 * @param envCode 环境参数配置ID
	 * @return 环境参数配置
	 */
	public ConfigEnvironment selectConfigEnvironmentById( String envCode );
	public ConfigEnvironment selectConfigEnvironmentByIdTwo(String envCode);

	/**
	 * 查询环境参数配置列表
	 *
	 * @param configEnvironment 环境参数配置
	 * @return 环境参数配置集合
	 */
	public List<ConfigEnvironment> selectConfigEnvironmentList(ConfigEnvironment configEnvironment);
	public List<ConfigEnvironment> selectConfigEnvironmentTwo(ConfigEnvironment configEnvironment);

	/**
	 * 新增环境参数配置
	 *
	 * @param configEnvironment 环境参数配置
	 * @return 结果
	 */
	public int insertConfigEnvironment( ConfigEnvironment configEnvironment) throws Exception;

	/**
	 * 修改环境参数配置
	 *
	 * @param configEnvironment 环境参数配置
	 * @return 结果
	 */
	public int updateConfigEnvironment(ConfigEnvironment configEnvironment);


	int changeStatus(ConfigEnvironment configEnvironment);

	/**
	 * 批量删除环境参数配置
	 *
	 * @param envCodes 需要删除的环境参数配置ID
	 * @return 结果
	 */
	public int deleteConfigEnvironmentByIds(String[] envCodes );

	/**
	 * 删除环境参数配置信息
	 *
	 * @param envCode 环境参数配置ID
	 * @return 结果
	 */
	public int deleteConfigEnvironmentById(String envCode);

    public int getTitleIndex(String title, String code);

	void refreshCache();
}
