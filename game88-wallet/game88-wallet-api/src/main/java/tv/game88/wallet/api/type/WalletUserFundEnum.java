package tv.game88.wallet.api.type;

import lombok.Getter;
import tv.game88.wallet.api.dto.RspFundEnumType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 交易类型 type>0  为入金 type>0&&type<100  为充值
 */
@Getter
public enum WalletUserFundEnum {
    TRANSACTION_ORDER_IN( 4, "交易购币", true ),
    CANCEL_ORDER_IN( 3, "撤销挂单", true ),
    PERSONAL_TRANSFER_IN( 2, "个人转账入账", true ),
    WITHDRAW_IN( 1, "提款收币", false ),

    DEPOSIT_OUT( -1, "充值出币", false ),
    PERSONAL_TRANSFER_OUT( -2, "个人转账出账", true ),
    PUT_ORDER_OUT( -3, "挂单出售", true ),
    ;

    private final Integer type;
    private final String  des;
    private final Boolean isTransaction;

    WalletUserFundEnum( Integer type, String des, Boolean isTransaction ) {
        this.type          = type;
        this.des           = des;
        this.isTransaction = isTransaction;
    }

    public static WalletUserFundEnum getByType( Integer type ) {
        for ( WalletUserFundEnum value : WalletUserFundEnum.values() ) {
            if ( Objects.equals( value.getType(), type ) ) {
                return value;
            }
        }
        return null;
    }

    public static List<RspFundEnumType> getFundEnumType() {
        return Arrays.stream( WalletUserFundEnum.values() ).map( m -> {
            RspFundEnumType tradeType = new RspFundEnumType();
            tradeType.setName( m.name() );
            tradeType.setType( m.getType() );
            tradeType.setDes( m.getDes() );
            return tradeType;
        } ).sorted( Comparator.comparing( RspFundEnumType::getType ) ).collect( Collectors.toList() );
    }
}
