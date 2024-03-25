package tv.game88.wallet.api.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum WalletTransEnum {
    // 买家取消订单
    BUYER_CANCEL,
    // 卖家取消订单
    SELLER_CANCEL,

    // 首次详情订单生成时状态
    // 买家确定购买
    BUYER_CONFIRM_BUY,
    // 卖家确认交易
    SELLER_CONFIRM_TRANS,
    // 买家确认转账
    BUYER_CONFIRM_TRANSFER,

    // 卖家确认转币
    SELLER_CONFIRM_TRANSFER,
    // 系统确认转币
    SYSTEM_CONFIRM_TRANSFER,

    //系统取消交易
    SYSTEM_CANCEL_TRANS,

    // 卖家未收到转账
    SELLER_NOT_RECEIVED,

    // 计时器到期时的状态，等待卖方确认
    SELLER_CONFIRM_TIMEOUT,

    SELLER_CONFIRM_TRANSFER_TIMEOUT,

    //计时器到期时的状态，等待买方转账
    BUYER_CONFIRM_TRANSFER_TIMEOUT,
    ;

    public static List<String> getPayMethodTypes() {
        return Arrays.stream( WalletTransEnum.values() ).map( Enum::name ).collect( Collectors.toList() );
    }
}
