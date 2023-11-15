package tv.game88.core.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.admin.entity.SysLogininfor;

import java.util.List;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author MengJun
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {
	/**
	 * 查询系统登录日志集合
	 *
	 * @param logininfor 访问日志对象
	 * @return 登录记录集合
	 */
	public List<SysLogininfor> selectLogininforList( SysLogininfor logininfor );
}
