package tv.game88.wallet.api.type;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum WalletTransEnum {
    @Schema( title = "买家取消订单" ) BUYER_CANCEL,
    @Schema( title = "卖家取消订单" ) SELLER_CANCEL,
    @Schema( title = "系统取消订单" ) SYSTEM_CANCEL,


    // 首次详情订单生成时状态
    // 买家确定购买
    @Schema( title = "买家确定购买" ) BUYER_CONFIRM_BUY,
    // 卖家确认交易
    @Schema( title = "卖家确认交易" ) SELLER_CONFIRM_TRANS,
    // 买家确认转账
    @Schema( title = "买家确认转账" ) BUYER_CONFIRM_TRANSFER,

    // 卖家确认转币
    @Schema( title = "卖家确认转币" ) SELLER_CONFIRM_TRANSFER,
    // 系统确认转币
    @Schema( title = "系统确认转币" ) SYSTEM_CONFIRM_TRANSFER,

    // 卖家未收到转账
    @Schema( title = "卖家未收到转账" ) SELLER_NOT_RECEIVED,
    ;

    public static List<String> getPayMethodTypes() {
        return Arrays.stream( WalletTransEnum.values() ).map( Enum::name ).collect( Collectors.toList() );
    }
}
