package tv.game88.game.admin.controllor;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.dto.ReqMemberGameData;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.service.MemberGameDataService;

import java.util.List;

/**
 * 会员注单数据Controller
 *
 * @author 77tv
 * {@code @date} 2021-01-29
 */
@Log4j2
@RestController
@RequestMapping ( "/member/memberGameData" )
public class MemberGameDataController extends BaseController {
    @Resource
    private MemberGameDataService memberGameDataService;

    /**
     * 查询会员注单数据列表
     */
    @PreAuthorize ( "@ss.hasPermi('member:gameData:list')" )
    @GetMapping ( "/list" )
    public RspBase<List<MemberGameData>> list( ReqMemberGameData reqMemberGameData ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberGameData> list = memberGameDataService.selectMemberGameDataList( reqMemberGameData );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询会员注单数据统计
     */
    @PreAuthorize ( "@ss.hasPermi('member:gameData:list')" )
    @GetMapping ( "/getCount" )
    public RspBase<MemberGameData> getCount( ReqMemberGameData reqMemberGameData ) {
        MemberGameData memberGameData = memberGameDataService.getCount( reqMemberGameData );
        return RspBase.ok( memberGameData );
    }

    /**
     * 导出会员注单数据列表
     */
    @PreAuthorize ( "@ss.hasPermi('member:gameData:export')" )
    @Log ( title = "会员注单数据", businessType = BusinessType.EXPORT )
    @GetMapping ( "/export" )
    public RspBase<List<MemberGameData>> export( ReqMemberGameData reqMemberGameData ) {
        return RspBase.ok(  memberGameDataService.selectMemberGameDataList( reqMemberGameData ) );
    }

    @PreAuthorize ( "@ss.hasPermi('member:gameData:recordList')" )
    @GetMapping ( value = "/recordList" )
    public RspBase<?> getGameRecordList( MemberGameData memberGameData ) {
        //        return memberGameDataService.getGameBetRecordData( memberGameData );

        return getRspBasePage( memberGameDataService.getGameBetRecordData( memberGameData ) );

    }

    @PreAuthorize ( "@ss.hasPermi('member:gameData:detailList')" )
    @GetMapping ( value = "/detailList" )
    public RspBase<?> getGameDetailList( MemberGameData memberGameData ) {
        return getRspBasePage( memberGameDataService.getGameBetDetailData( memberGameData ) );
    }
}
