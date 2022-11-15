package tv.game88.lottery.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.vo.RspBase;
import tv.game88.lottery.api.dto.RspLotteryInit;
import tv.game88.lottery.api.service.LotteryService;

@Log4j2
@Service
public class LotteryServiceImpl implements LotteryService {
    @Override
    public RspBase<RspLotteryInit> getRspLotteryInit( Integer lotteryId ) {
        return null;
    }
}
