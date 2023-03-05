package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeZhaoHType {
    //
    ICBC( 3L ),
    CCB( 2L ),
    ABC( 4L ),
    PSBS( 5L ),
    BOC( 1L ),
    BCM( 6L ),
    CMB( 7L ),
    CEB( 8L ),
    CIB( 9L ),
    CMBC( 10L ),
    BCCB( 11L ),
    CITIC( 12L ),
    CGB( 13L ),
    SDB( 14L ),
    SPDB( 15L ),
    PAB( 16L ),
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
    BOGL( 29L ),
    QDCCB( 30L ),
    BOBBG( 31L ),
    OTHER( -1L ),
    ;

    private final Long bankId;

    BankCodeZhaoHType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeZhaoHType getCodeByBankId( Long bankId ) {
        for ( BankCodeZhaoHType enumType : BankCodeZhaoHType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return OTHER;
    }
}
