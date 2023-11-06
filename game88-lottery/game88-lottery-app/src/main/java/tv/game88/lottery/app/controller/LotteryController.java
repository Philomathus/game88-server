package tv.game88.lottery.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.lottery.dto.RspBetRecord;
import tv.game88.core.lottery.dto.RspLotteryHistory;
import tv.game88.core.session.utils.MemberSecurityUtils;
import tv.game88.lottery.api.dto.*;
import tv.game88.lottery.api.service.LotteryService;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "彩票相关接口" )
@Log4j2
public class LotteryController extends BaseController {
    @Resource
    private LotteryService lotteryService;

    @Operation( summary = "初始化彩票信息" )
    @PostMapping( "lotteryInit" )
    public RspBase<RspLotteryInit> init( @Validated @RequestBody ReqLottery req ) {
        return RspBase.ok( lotteryService.getRspLotteryInit( req.getId() ) );
    }

    @Operation( summary = "期数刷新" )
    @PostMapping( "issue" )
    public RspBase<IssueVo> issue( @Validated @RequestBody ReqLottery req ) {
        return RspBase.ok( lotteryService.getIssueVo( req.getId() ) );
    }

    @Operation( summary = "投注记录" )
    @PostMapping( "betRecord" )
    public RspBase<List<RspBetRecord>> betRecord( @Validated @RequestBody ReqLottery req ) {
        startPage( req );
        List<RspBetRecord> rspBetRecordList = lotteryService.getBetRecordList( req.getId(), MemberSecurityUtils.getUserId() );
        return getRspBasePage( rspBetRecordList, req );
    }

    @Operation( summary = "开奖记录查询" )
    @PostMapping( "issueRecord" )
    public RspBase<List<RspLotteryHistory>> issueRecord( @Validated @RequestBody ReqLottery req ) {
        startPage( req );
        List<RspLotteryHistory> rspBetRecordList = lotteryService.getLotteryHistory( req.getId() );
        return getRspBasePage( rspBetRecordList, req );
    }

    @Operation( summary = "规则说明" )
    @PostMapping( "rule" )
    public RspBase<List<RuleVo>> rule( @Validated @RequestBody ReqLottery req ) {
        return RspBase.ok( lotteryService.getLotteryRule( req.getId() ) );
    }

    @Operation( summary = "投注" )
    @PostMapping( "bet" )
    public RspBase<RspBet> bet( @RequestBody ReqBet reqBet ) {
        return lotteryService.bet( reqBet, MemberSecurityUtils.getLoginUser().getPlatformUser() );
    }
}
