package tv.game88.core.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.admin.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志 数据层
 *
 * @author MengJun
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

	/**
	 * 查询系统操作日志集合
	 *
	 * @param operLog 操作日志对象
	 * @return 操作日志集合
	 */
	public List<SysOperLog> selectOperLogList( SysOperLog operLog );

	/**
	 * 清空操作日志
	 */
	public void cleanOperLog();
}
