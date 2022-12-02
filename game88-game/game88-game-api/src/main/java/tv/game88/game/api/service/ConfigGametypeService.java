package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.ConfigGametype;

import java.util.List;

/**
 * Service接口
 *
 * @author MengJun
 */
public interface ConfigGametypeService extends IService<ConfigGametype> {
	/**
	 * 查询列表
	 *
	 * @param configGametype 
	 * @return 集合
	 */
	public List<ConfigGametype> selectConfigGametypeList(ConfigGametype configGametype);
}