package tv.game88.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.admin.entity.SysLogininfor;
import tv.game88.core.admin.mapper.SysLogininforMapper;
import tv.game88.admin.system.service.ISysLogininforService;

import java.util.List;

/**
 * 系统访问日志情况信息 服务层处理
 *
 * @author MengJun
 */
@Service
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements ISysLogininforService {

	/**
	 * 查询系统登录日志集合
	 *
	 * @param logininfor 访问日志对象
	 * @return 登录记录集合
	 */
	@Override
	public List<SysLogininfor> selectLogininforList( SysLogininfor logininfor ) {
		return this.baseMapper.selectLogininforList( logininfor );
	}
}
