package tv.game88.wallet.api.type;

import lombok.Getter;

import java.util.Objects;

/**
 * 交易类型 type>0  为入金 type>0&&type<100  为充值
 */
@Getter
public enum WalletMerchantFundEnum {
    WITHDRAW_IN( 1, "提款收币" ),

    DEPOSIT_OUT( -1, "充值出币" ),
    ;

    private final Integer type;
    private final String  des;

    WalletMerchantFundEnum( Integer type, String des ) {
        this.type          = type;
        this.des           = des;
    }

    public static WalletMerchantFundEnum getByType( Integer type ) {
        for ( WalletMerchantFundEnum value : WalletMerchantFundEnum.values() ) {
            if ( Objects.equals( value.getType(), type ) ) {
                return value;
            }
        }
        return null;
    }
}
