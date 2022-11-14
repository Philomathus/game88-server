package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeLianFuBaoType {
    //
    ICBC( 3L ),
    CCB( 2L ),
    ABC( 4L ),
    PSBS( 5L ),
    BOC( 1L ),
    BOCO( 6L ),
    CMB( 7L ),
    CEB( 8L ),
    CIB( 9L ),
    CMBC( 10L ),
    BCCB( 11L ),
    CTTIC( 12L ),
    GDB( 13L ),
    SDB( 14L ),
    SPDB( 15L ),
    PINGANBANK( 16L ),
    HXB( 17L ),
    SHB( 18L ),
    CBHB( 19L ),
    HKBEA( 20L ),
    NBCB( 21L ),
    CZB( 22L ),
    NJCB( 23L ),
    HZCB( 24L ),
    BJRCB( 25L ),
    SRCB( 26L ),
    QLB( 27L ),
    LZCB( 28L ),
    GLB( 29L ),
    QDCB( 30L ),
    BOBBG( 31L ),
    OTHER( -1L ),
    ;

    private final Long bankId;

    BankCodeLianFuBaoType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeLianFuBaoType getCodeByBankId( Long bankId ) {
        for ( BankCodeLianFuBaoType enumType : BankCodeLianFuBaoType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return OTHER;
    }
}
