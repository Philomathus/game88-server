package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.lottery.api.dto.HistoryResult;
import tv.game88.lottery.api.dto.RspLotteryInfo;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.lottery.api.entity.LotteryHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LotteryHistoryService extends IService<LotteryHistory> {
    void newIssue( RspLotteryInfo lotteryInfo, String issue, LocalDateTime time, int i );

    void awardByLotteryResult( List<LotteryBet> updateList, Map<String, BigDecimal> prizeMap, String historyId, Map<String,
            BigDecimal> nowMoney, String lotteryName );

    List<HistoryResult> selectResultWaite( String lotteryAgent, Integer lotteryId );
}
