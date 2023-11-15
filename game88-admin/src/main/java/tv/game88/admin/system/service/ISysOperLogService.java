package tv.game88.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.admin.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @author MengJun
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     *
     * @return 操作日志集合
     */
    public List<SysOperLog> selectOperLogList( SysOperLog operLog );
}
