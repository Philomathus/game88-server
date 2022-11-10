package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.member.entity.LogMoney;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.platform.api.service.LogMoneyService;

import java.util.List;
import java.util.Map;

/**
 * 资金日志Service业务层处理
 *
 * @author 77lm
 * @date 2021-10-09
 */
@Service
public class LogMoneyServiceImpl extends ServiceImpl<LogMoneyMapper, LogMoney> implements LogMoneyService {

    /**
     * 查询资金日志列表
     *
     * @param logMoney 资金日志
     *
     * @return 资金日志
     */
    @Override
    public List<LogMoney> selectLogMoneyList( LogMoney logMoney ) {
        this.getTime( logMoney );
        if ( StringUtils.isNotBlank( logMoney.getSearchValue() ) ) {
            String tableLast = logMoney.getSearchValue().substring( logMoney.getSearchValue().length() - 1 );
            logMoney.setTableLast( tableLast );
            return this.baseMapper.selectLogMoneySingleList( logMoney );
        } else if ( logMoney.getTypes() != null && StringUtils.isBlank( logMoney.getSearchValue() ) ) {
            return this.baseMapper.selectLogMoneyList( logMoney );
        } else {
            logMoney.setTableLast( "0" );
            return this.baseMapper.selectLogMoneySingleList( logMoney );
        }
    }

    @Override
    public Map totalCount( LogMoney logMoney ) {
        this.getTime( logMoney );
        return this.baseMapper.totalCount( logMoney );
    }

    @Override
    public Map listCount( LogMoney logMoney ) {
        this.getTime( logMoney );
        if ( StringUtils.isNotBlank( logMoney.getSearchValue() ) ) {
            String tableLast = logMoney.getSearchValue().substring( logMoney.getSearchValue().length() - 1 );
            logMoney.setTableLast( tableLast );
        }
        return this.baseMapper.listCount( logMoney );
    }

    private void getTime( LogMoney logMoney ) {
        if ( logMoney.getSelectDate() != null && logMoney.getSelectDate().length > 0 ) {
            logMoney.setStartTime( logMoney.getSelectDate()[ 0 ] );
            logMoney.setEndTime( logMoney.getSelectDate()[ 1 ] );
        }
    }

}
