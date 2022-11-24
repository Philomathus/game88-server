package tv.game88.lottery.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.lottery.api.dto.ReqBet;
import tv.game88.lottery.api.dto.RspBet;
import tv.game88.core.lottery.entity.LotteryBet;

import java.math.BigDecimal;
import java.util.List;

/**
 * 彩票会员下注详情Service接口
 *
 * @author mengJun
 */
public interface LotteryBetService extends IService<LotteryBet> {
    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情集合
     */
    public List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet );

    RspBase<RspBet> userBet( PlatformUser platformUser, ReqBet reqBet, String[] bet_select, String lotteryName, String issue,
                             BigDecimal cost );

    String procherckQuzhiImport( Integer lotteryId );
}