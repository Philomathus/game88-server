package tv.game88.lottery.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.lottery.api.dto.*;

import java.util.List;

public interface LotteryService {
    RspLotteryInit getRspLotteryInit( Integer lotteryId );

    public IssueVo getIssueVo( Integer lotteryId );

    List<RspBetRecord> getBetRecordList( Integer lotteryId, String memberId );

    List<RspLotteryHistory> getLotteryHistory( Integer lotteryId );

    RspBase<RspBet> bet( ReqBet reqBet, PlatformUser platformUser );

    void computeResult( Integer lotteryId );

    void awardLottery( Integer lotteryId );
}
