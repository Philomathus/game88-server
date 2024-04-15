package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.wallet.api.entity.ReportUserAmount;

import java.util.List;

public interface ReportUserAmountService extends IService<ReportUserAmount> {
    List<ReportUserAmount> getReportUserAmountList (ReportUserAmount reportUserAmount );
}
