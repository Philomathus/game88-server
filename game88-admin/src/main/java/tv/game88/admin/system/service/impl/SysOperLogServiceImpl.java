package tv.game88.admin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.admin.entity.SysOperLog;
import tv.game88.core.admin.mapper.SysOperLogMapper;
import tv.game88.admin.system.service.ISysOperLogService;

import java.util.List;

/**
 * 操作日志 服务层处理
 *
 * @author MengJun
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     *
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLog> selectOperLogList( SysOperLog operLog ) {
        return this.baseMapper.selectOperLogList( operLog );
    }
}
