package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum BankCodeTianXiaHuiType {
    BOC( 1L ),//中国银行
    CCB( 2L ),//中国建设银行
    ICBC( 3L ),//中国工商银行
    ABC( 4L ),//中国农业银行
    PSBC( 5L ),//中国邮政储蓄银行
    BOCOM( 6L ),//中国交通银行
    CMB( 7L ),//中国招商银行
    CEB( 8L ),//中国光大银行
    CIB( 9L ),//中国兴业银行
    CMBC( 10L ),//中国民生银行
    BCCB( 11L ),//北京银行
    CNCB( 12L ),//中信银行
    GDB( 13L ),//广东发展银行
    SDB( 14L ),//深圳发展银行
    SPDB( 15L ),//上海浦东发展银行
    PAB( 16L ),//平安银行
    HXB( 17L ),//华夏银行
    BOS( 18L ),//上海银行
    CBHB( 19L ),//渤海银行
    HKBEA( 20L ),//东亚银行
    NBBANK( 21L ),//宁波银行
    CZBANK( 22L ),//浙商银行
    NJCB( 23L ),//南京银行
    HZCB( 24L ),//杭州银行
    BJRCB( 25L ),//北京农村商业银行
    SHRCB( 26L ),//上海农村商业银行
    QLBANK( 27L ),//齐鲁银行
    LZYH( 28L ),//兰州银行
    GLB( 29L ),//桂林银行
    QDCCB( 30L ),//青岛银行
    BGB( 31L ),//广西北部湾银行
    CSRCB( 32L ),//常熟农商
    ZJTLCB( 35L ),//浙江泰隆商业银行
    SDNCB( 36L ),//山东农村商业银行
    CQBANK( 37L ),//重庆银行
    BHB( 38L ),//河北银行
    CSCB( 39L ),//长沙银行
    SJBANK( 40L ),//盛京银行
    CCAB( 41L ),//长安银行
    JLBANK( 42L ),//吉林银行
    JLRCU( 43L ),//吉林农村信用社
    EGBANK( 44L ),//恒丰银行
    SCRCU( 45L ),//四川农信
    SXCCU( 46L ),//陕西信合
    QSB( 47L ),//齐商银行
    GSRCU( 48L ),//甘肃农信
    FJNX( 49L ),//福建农村信用社
    HNNC( 50L ),//湖南农村信用社
    JSHB( 51L ),//晋商银行
    BOHN( 52L ),//海南省农村信用社
    DRCBANK( 53L ),//东莞农村商业
    PERB( 54L ),//蒙商银行
    FJHXBC( 55L ),//福建海峡银行
    LNRCC( 56L ),//辽宁农村信用社
    JPTX( 57L ),//广东农信
    JXNXS( 58L ),//江西农商
    TCCB( 59L ),//天津银行
    JSBC( 60L ),//江苏银行
    SBANK( 61L ),//微商银行
    SXNXS( 62L ),//陕西农信
    JXLHCZYH( 63L ),//金乡蓝海村镇银行
    YNRCC( 64L ),//云南农村信用社
    HBRCB( 65L ),//河北农信
    GRCB( 66L ),//广州农商银行
    HBRCC( 67L ),//湖北农信
    GXRCU( 70L ),//广西农村信用社
    LSBC( 71L ),//临商银行
    GZNX( 72L ),//贵州农村信用社
    BOJZ( 73L ),//锦州银行
    JNRCB( 74L ),//江南农村商业银行
    ZJNX( 75L ),//浙江省农村信用社
    SXRCU( 76L ),//山西农村信用社
    GSBANK( 77L ),//甘肃银行
    XMBANK( 78L ),//厦门银行
    GYCB( 79L ),//贵阳银行
    NCB( 80L ),//江西银行
    FSCB( 82L ),//抚顺银行
    AHRCU( 83L ),//安徽农村信用联合社
    SZRCB( 84L ),//苏州农商银行
    AHQCHSVB( 85L ),//谯城湖商镇银行
    JSRCU( 86L ),//江苏农村信用社
    GDNY( 89L ),//广东南粤银行
    CDCB( 90L ),//成都银行
    XJRCU( 93L ),//新疆农村信用社
    DZBCHINA( 94L ),//德州银行
    NXRCU( 96L ),//黄河农村商业银行
    HUNB( 97L ),//湖南银行
    HDBANK( 98L ),//邯郸银行
    JSCJBANK( 99L ),//江苏长江商业银行
    HLJRCU( 100L ),//黑龙江省农村信用社
    BOTL( 102L ),//铁岭银行
    LSBK( 105L ),//辽沈银行
    LJBANK( 110L ),//龙江银行
    BOSZ( 112L ),//苏州银行
    NMGNXS( 113L ),//内蒙古农村信用社
    BOIMC( 114L ),//内蒙古银行
    ;

    private final Long bankId;

    BankCodeTianXiaHuiType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeTianXiaHuiType getCodeByBankId( Long bankId ) {
        for ( BankCodeTianXiaHuiType enumType : BankCodeTianXiaHuiType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return null;
    }
}
