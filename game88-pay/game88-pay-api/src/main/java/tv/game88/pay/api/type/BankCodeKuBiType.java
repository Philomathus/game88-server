package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeKuBiType {
    //
    cmb( 7L ),
    icbc( 3L ),
    ccb( 2L ),
    spdb( 15L ),
    abc( 4L ),
    cmbc( 10L ),
    cib( 9L ),
    comm( 6L ),
    ceb( 8L ),
    boc( 1L ),
    bccb( 11L ),
    pingan( 16L ),
    cgb( 13L ),
    psbc( 5L ),
    ecitic( 12L ),
    hxb( 17L ),
    shb( 18L ),
    ;

    private final Long bankId;

    BankCodeKuBiType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeKuBiType getCodeByBankId( Long bankId ) {
        for ( BankCodeKuBiType enumType : BankCodeKuBiType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return null;
    }
}
