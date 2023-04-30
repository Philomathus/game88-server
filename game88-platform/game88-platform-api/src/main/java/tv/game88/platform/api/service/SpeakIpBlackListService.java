package tv.game88.platform.api.service;

import tv.game88.platform.api.entity.SpeakIpBlackList;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author jake
 * @date 2023-04-13
 * Copied from 77tv
 */
public interface SpeakIpBlackListService {
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
	 * 批量删除【请填写功能名称】
	 *
	 * @param ids 需要删除的【请填写功能名称】ID
	 * @return 结果
	 */
	public int deleteSpeakIpBlackListByIds(String[] ids );

	/**
	 * 删除【请填写功能名称】信息
	 *
	 * @param id 【请填写功能名称】ID
	 * @return 结果
	 */
	public int deleteSpeakIpBlackListById(String id);
}