package tv.game88.lottery.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.lottery.dto.RspBetRecord;
import tv.game88.core.lottery.dto.RspLotteryHistory;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.lottery.api.dto.*;

import java.util.List;

public interface LotteryService {
    public boolean isLotteryCenter();

    RspLotteryInit getRspLotteryInit( Integer lotteryId );

    public IssueVo getIssueVo( Integer lotteryId );

    List<RspBetRecord> getBetRecordList( Integer lotteryId, String memberId );

    List<RspLotteryHistory> getLotteryHistory( Integer lotteryId );

    RspBase<RspBet> bet( ReqBet reqBet, PlatformUser platformUser );

    void computeResult( Integer lotteryId );

    void awardLottery( Integer lotteryId );

    List<RuleVo> getLotteryRule( Integer lotteryId );

    void catchResult( Integer lotteryId );
}
