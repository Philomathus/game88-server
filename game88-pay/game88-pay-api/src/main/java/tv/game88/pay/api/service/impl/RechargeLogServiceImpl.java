package tv.game88.pay.api.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.RechargeLog;
import tv.game88.pay.api.mapper.RechargeLogMapper;
import tv.game88.pay.api.service.RechargeLogService;

import jakarta.annotation.Resource;
import java.util.List;


/**
 * 充值日志服务 interface impl
 *
 * @author Rajesh
 * @date 2023-05-20
 */

@Log4j2
@Service
public class RechargeLogServiceImpl extends ServiceImpl<RechargeLogMapper, RechargeLog> implements RechargeLogService {

    @Resource
    private RechargeLogMapper rechargeLogMapper;

    @Override
    public List<RechargeLog> selectAllRechargeLog( RechargeLog rechargeLog ) {
        String[] selectDate = rechargeLog.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            rechargeLog.setSelectStartDate( selectDate[ 0 ] );
            rechargeLog.setSelectEndDate( selectDate[ 1 ] );
        }
        return rechargeLogMapper.selectRechargeLogList( rechargeLog );
    }
}
