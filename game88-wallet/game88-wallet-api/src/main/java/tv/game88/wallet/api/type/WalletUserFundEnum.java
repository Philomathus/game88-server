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
    TRANSFER_IN( 1, "转入" ),

    TRANSFER_OUT( -1, "转出" ),
    ;

    private Integer type;
    private String  des;

    WalletUserFundEnum( Integer type, String des ) {
        this.type = type;
        this.des  = des;
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
