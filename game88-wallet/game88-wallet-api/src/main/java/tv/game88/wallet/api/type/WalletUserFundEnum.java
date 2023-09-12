package tv.game88.wallet.api.type;

import lombok.Getter;

import java.util.Objects;

/**
 * 交易类型 type>0  为入金 type>0&&type<100  为充值
 * <p>
 * <p>
 * type<0  为消费
 */
@Getter
public enum WalletUserFundEnum {
    PERSONAL_TRANSFER_IN( 2, "个人转账入账", false ),
    WITHDRAW_IN( 1, "提款收币", false ),

    DEPOSIT_OUT( -1, "充值出币", false ),
    PERSONAL_TRANSFER_OUT( -2, "个人转账出账", false ),
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
}
