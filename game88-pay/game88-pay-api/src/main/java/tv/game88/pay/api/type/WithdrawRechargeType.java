package tv.game88.pay.api.type;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema( title = "提现/银行卡充值/USDT充值/在线充值" )
public enum WithdrawRechargeType {
    // 提现/银行卡充值/USDT充值/在线充值
    withdraw,
    rechargeBank,
    rechargeUsdt,
    rechargeOnline
}
