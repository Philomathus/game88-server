package tv.game88.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.admin.entity.SysLogininfor;

import java.util.List;

/**
 * 系统访问日志情况信息 服务层
 *
 * @author MengJun
 */
public interface ISysLogininforService extends IService<SysLogininfor> {
    /**
     * 查询系统登录日志集合
     *
     * @param logininfor 访问日志对象
     *
     * @return 登录记录集合
     */
    public List<SysLogininfor> selectLogininforList( SysLogininfor logininfor );
}
