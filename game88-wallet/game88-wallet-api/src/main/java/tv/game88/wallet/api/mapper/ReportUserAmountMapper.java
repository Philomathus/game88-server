package tv.game88.wallet.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.wallet.api.entity.ReportUserAmount;

import java.util.List;

public interface ReportUserAmountMapper extends BaseMapper<ReportUserAmount> {
    List<ReportUserAmount> selectReportUserAmountList(ReportUserAmount walletUserPayMethod );

}
