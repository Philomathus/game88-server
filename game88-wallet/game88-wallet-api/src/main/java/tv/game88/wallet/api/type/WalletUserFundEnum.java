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
    TRANSFER_IN( 1, "资金转入", false ),

    TRANSFER_OUT( -1, "资金转出", false ),
    ;

    private Integer type;
    private String  des;
    private Boolean isTransaction;

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
