package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeMyType {
    //
    ICBC( 3 ),
    CCB( 2 ),
    ABC( 4 ),
    PSBC( 5 ),
    BOC( 1 ),
    COMM( 6 ),
    CMB( 7 ),
    CEB( 8 ),
    CIB( 9 ),
    CMBC( 10 ),
    BCCB( 11 ),
    CITIC( 12 ),
    GDB( 13 ),
    SDB( 14 ),
    SPDB( 15 ),
    PINGANBANK( 16 ),
    HXB( 17 ),
    SHB( 18 ),
    CBHB( 19 ),
    HKBEA( 20 ),
    NBCB( 21 ),
    CZB( 22 ),
    NJCB( 23 ),
    HZCB( 24 ),
    BJRCB( 25 ),
    SRCB( 26 ),
    QTBC( -1 ),
    ;

    private final Integer bankId;

    BankCodeMyType( Integer bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeMyType getCodeByType( Long bankId ) {
        for ( BankCodeMyType enumType : BankCodeMyType.values() ) {
            if ( Objects.equals( enumType.getBankId().longValue(), bankId ) ) {
                return enumType;
            }
        }
        return null;
    }
}
