package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import tv.game88.wallet.api.entity.ReportUserAmount;
import tv.game88.wallet.api.mapper.ReportUserAmountMapper;
import tv.game88.wallet.api.service.ReportUserAmountService;

import java.util.List;

@Service
public class ReportUserAmountServiceImpl extends ServiceImpl<ReportUserAmountMapper, ReportUserAmount> implements ReportUserAmountService {

    @Resource
    private ReportUserAmountMapper reportUserAmountMapper;

    @Override
    public List<ReportUserAmount> getReportUserAmountList( ReportUserAmount reportUserAmount ) {
        return reportUserAmountMapper.selectReportUserAmountList( reportUserAmount );
    }
}
