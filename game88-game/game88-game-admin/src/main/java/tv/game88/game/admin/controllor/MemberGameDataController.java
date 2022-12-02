package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.dto.ReqMemberGameData;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.service.MemberGameDataService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会员注单数据Controller
 *
 * @author 77tv
 * @date 2021-01-29
 */
@RestController
@RequestMapping( "/member/memberGameData" )
public class MemberGameDataController extends BaseController {
    @Resource
    private MemberGameDataService memberGameDataService;

    /**
     * 查询会员注单数据列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberGameData:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberGameData>> list( ReqMemberGameData reqMemberGameData ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberGameData> list = memberGameDataService.selectMemberGameDataList( reqMemberGameData );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询会员注单数据统计
     */
    @PreAuthorize( "@ss.hasPermi('member:memberGameData:list')" )
    @GetMapping( "/getCount" )
    public RspBase<MemberGameData> getCount( ReqMemberGameData reqMemberGameData ) {
        MemberGameData memberGameData = memberGameDataService.getCount( reqMemberGameData );
        return RspBase.ok( memberGameData );
    }

    /**
     * 导出会员注单数据列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberGameData:export')" )
    @Log( title = "会员注单数据", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( ReqMemberGameData reqMemberGameData, HttpServletResponse response ) {
        List<MemberGameData> list = memberGameDataService.selectMemberGameDataList( reqMemberGameData );
        ExportExcelUtil.exportExcel( list, "会员注单数据", "会员注单数据表", MemberGameData.class, response );
    }

    @PreAuthorize( "@ss.hasPermi('member:memberGameData:recordList')" )
    @GetMapping( value = "/recordList" )
    public RspBase<?> getGameRecordList( MemberGameData memberGameData ) {
        return memberGameDataService.getGameBetRecordData( memberGameData );
    }

    @PreAuthorize( "@ss.hasPermi('member:memberGameData:detailList')" )
    @GetMapping( value = "/detailList" )
    public RspBase<?> getGameDetailList( MemberGameData memberGameData ) {
        return memberGameDataService.getGameBetDetailData( memberGameData );
    }
}
