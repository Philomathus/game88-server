package tv.game88.wallet.admin.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.entity.ReportUserAmount;
import tv.game88.wallet.api.service.ReportUserAmountService;

import java.util.List;

@RestController
@RequestMapping("/admin/reportUserAmount")
public class ReportUserAmountController extends BaseController {

    @Resource
    private ReportUserAmountService reportUserAmountService;

    @GetMapping("/list")
    public RspBase<List<ReportUserAmount>> list(ReportUserAmount reportUserAmount ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );

        List<ReportUserAmount> reportUserAmountList = reportUserAmountService.getReportUserAmountList( reportUserAmount );
        return getRspBasePage( reportUserAmountList, pageDomain );
    }
}
