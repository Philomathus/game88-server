package tv.game88.lottery.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.lottery.api.dto.RspLotteryInit;

public interface LotteryService {
    RspBase<RspLotteryInit> getRspLotteryInit( Integer lotteryId );
}
