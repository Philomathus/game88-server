package tv.game88.platform.api.mapper;

import tv.game88.platform.api.entity.SpeakIpBlackList;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author 77tv
 * @date 2021-02-22
 */
public interface SpeakIpBlackListMapper {
	/**
	 * 查询【请填写功能名称】
	 *
	 * @param id 【请填写功能名称】ID
	 * @return 【请填写功能名称】
	 */
	public SpeakIpBlackList selectSpeakIpBlackListById(String id);

	/**
	 * 查询【请填写功能名称】列表
	 *
	 * @param speakIpBlackList 【请填写功能名称】
	 * @return 【请填写功能名称】集合
	 */
	public List<SpeakIpBlackList> selectSpeakIpBlackListList(SpeakIpBlackList speakIpBlackList);

	/**
	 * 新增【请填写功能名称】
	 *
	 * @param speakIpBlackList 【请填写功能名称】
	 * @return 结果
	 */
	public int insertSpeakIpBlackList(SpeakIpBlackList speakIpBlackList);

	/**
	 * 修改【请填写功能名称】
	 *
	 * @param speakIpBlackList 【请填写功能名称】
	 * @return 结果
	 */
	public int updateSpeakIpBlackList(SpeakIpBlackList speakIpBlackList);

	/**
	 * 删除【请填写功能名称】
	 *
	 * @param id 【请填写功能名称】ID
	 * @return 结果
	 */
	public int deleteSpeakIpBlackListById(String id);

	/**
	 * 批量删除【请填写功能名称】
	 *
	 * @param ids 需要删除的数据ID
	 * @return 结果
	 */
	public int deleteSpeakIpBlackListByIds(String[] ids );

	void deleteSpeakIp(String userIp);
}