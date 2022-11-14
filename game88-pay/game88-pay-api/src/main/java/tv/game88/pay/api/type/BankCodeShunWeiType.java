package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeShunWeiType {
    //
    ICBC( 3L ),
    ABC( 4L ),
    CCB( 2L ),
    BOC( 1L ),
    CMB( 7L ),
    BCM( 6L ),
    CIB( 9L ),
    CMBC( 10L ),
    CEB( 8L ),
    PAB( 16L ),
    CITIC( 12L ),
    CGB( 13L ),
    SPDB( 15L ),
    PSBC( 5L ),
    HXB( 17L ),
    QTBC( -1L ),
    ;

    private final Long bankId;

    BankCodeShunWeiType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeShunWeiType getCodeByBankId( Long bankId ) {
        for ( BankCodeShunWeiType enumType : BankCodeShunWeiType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return QTBC;
    }
}
