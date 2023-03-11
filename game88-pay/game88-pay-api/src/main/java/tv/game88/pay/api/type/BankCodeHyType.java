package tv.game88.pay.api.type;

import lombok.Getter;

import java.util.Objects;


@Getter
public enum BankCodeHyType {

    a1( 25L ), // 北京农商银行
    a2( 5L ), // 邮政储蓄银行
    a3( 4L ), // 农业银行
    a4( 13L ), // 广发银行
    //a5( Arrays.asList( "成都银行" ) ), // 成都银行
    a6( 24L ), // 杭州银行
    a7( 15L ), // 浦发银行
    a8( 8L ), // 光大银行
    a9( 26L ), //上海农商银行
    a10( 1L ), // 中国银行
    a11( 2L ), // 建设银行
    a12( 10L ), // 民生银行
    a13( 17L ), // 华夏银行
    a14( 16L ), // 平安银行
    a15( 21L ), // 宁波银行
    a16( 6L ), // 交通银行
    a17( 7L ), // 招商银行
    a18( 18L ), // 上海银行
    a19( 12L ), // 中信银行
    a20( 3L ), // 工商银行
    a22( 9L ), // 兴业银行
    a23( 11L ), // 北京银行
    // a24( Arrays.asList( "烟台银行" ) ), // 烟台银行
    a25( 30L ), // 青岛银行

    a101( 32L ), // 常熟农商银行
    // a102( Arrays.asList( "成都农商银行", "成都农商", "成都农村商业银行" ) ), // 成都农商银行
    // a103( Arrays.asList( "重庆农村商业银行", "重庆农商银行", "重庆农商" ) ), // 重庆农商银行
    // a104( Arrays.asList( "大连农村商业银行", "大连农商银行", "大连农商" ) ), // 大连农商银行
    a105( 53L ), // 东莞农商银行
    a106( 66L ), // 广州农商银行
    // a107( Arrays.asList( "海口联合农商银行", "海口农商银行", "海口农商" ) ), // 海口农商银行
    // a108( Arrays.asList( "江南农村商业银行", "江南农商银行", "江南农商" ) ), // 江南农商银行
    // a109( Arrays.asList( "江苏江阴农村商业银行", "江苏江阴农商银行", "江苏江阴农商" ) ), // 江苏江阴农商银行
    // a110( Arrays.asList( "昆山农村商业银行", "昆山农商银行", "昆山农商" ) ), // 昆山农商银行
    // a111( Arrays.asList( "南海农商银行" ) ), // 南海农商银行
    // a112( Arrays.asList( "宁夏黄河农村商业银行", "宁夏黄河农商银行", "宁夏黄河农商" ) ), // 宁夏黄河农商银行
    // a113( Arrays.asList( "尧都农商银行村镇银行" ) ), // 尧都农商银行村镇银行
    // a114( Arrays.asList( "顺德农商银行" ) ), // 顺德农商银行
    // a115( Arrays.asList( "天津农商银行" ) ), // 天津农商银行
    // a116( Arrays.asList( "吴江农村商业银行" ) ), // 吴江农村商业银行
    // a117( Arrays.asList( "武汉农村商业银行" ) ), // 武汉农村商业银行
    // a118( Arrays.asList( "张家港农村商业银行" ) ), // 张家港农村商业银行
    // a119( Arrays.asList( "江苏省农村信用社联合社", "江苏省农村信用社", "江苏农信", "江苏农村信用社银行", "江苏农村信用社", "江苏省农村信用社银行" ) ),
    /// a120( Arrays.asList( "安徽省农村信用社", "安徽省农村信用社", "安徽农信", "安徽农村信用社银行", "安徽农村信用社", "安徽省农村信用社银行" ) ),
    a121( 49L ), // 福建农信
    // a122( Arrays.asList( "贵州省农村信用社联合社", "贵州省农村信用社", "贵州农信", "贵州农村信用社银行", "贵州农村信用社", "贵州省农村信用社银行" ) ),
    a123( 48L ), // 甘肃农信
    a124( 57L ), // 广东农信
    // a125( Arrays.asList( "广西壮族自治区农村信用社联合社", "广西省农村信用社", "广西农信", "广西农村信用社银行", "广西农村信用社", "广西省农村信用社银行" ) ),
    a126( 50L ), // 湖南农信
    a127( 69L ), // 河南农信
    a128( 65L ), // 河北农信
    a129( 52L ), // 海南农信
    // a130( Arrays.asList( "黑龙江省农村信用社联合社", "黑龙江省农村信用社", "黑龙江农信", "黑龙江农村信用社银行", "黑龙江农村信用社", "黑龙江省农村信用社银行" ) ),
    a131( 43L ), // 吉林农信
    // a132( Arrays.asList( "江西省农村信用社", "江西省农村信用社", "江西农信", "江西农村信用社银行", "江西农村信用社", "江西省农村信用社银行" ) ),
    a133( 56L ), // 辽宁农信
    // a134( Arrays.asList( "内蒙古农村信用社联合社", "内蒙古省农村信用社", "内蒙古农信", "内蒙古农村信用社银行", "内蒙古农村信用社", "内蒙古省农村信用社银行" ) ),
    // a135( Arrays.asList( "青海省农村信用社", "青海省农村信用社", "青海农信", "青海农村信用社银行", "青海农村信用社", "青海省农村信用社银行" ) ),
    // a136( Arrays.asList( "山西省农村信用社", "山西省农村信用社", "山西农信", "山西农村信用社银行", "山西农村信用社", "山西省农村信用社银行" ) ),
    a137( 45L ), // 四川农信
    // a138( Arrays.asList( "山东省农村信用社联合社", "山东省农村信用社", "山东农信", "山东农村信用社银行", "山东农村信用社", "山东省农村信用社银行" ) ),
    // a139( Arrays.asList( "新疆农村信用社", "新疆省农村信用社", "新疆农信", "新疆农村信用社银行", "新疆农村信用社", "新疆省农村信用社银行" ) ),
    a140( 64L ), // 云南农信
    // a141( Arrays.asList( "浙江省农村信用社联合", "浙江省农村信用社", "浙江农信", "浙江农村信用社银行", "浙江农村信用社", "浙江省农村信用社银行" ) ),
    // a142( Arrays.asList( "台州银行" ) ),
    a143( 29L ), // 桂林银行

    // a144( Arrays.asList( "中国农业发展银行" ) ),
    // a145( Arrays.asList( "湖北银行" ) ),
    // a146( Arrays.asList( "华融湘江银行" ) ),
    // a147( Arrays.asList( "汉口银行" ) ),
    a148( 67L ), // 湖北农信
    a149( 59L ),
    // a150( Arrays.asList( "贵州银行" ) ),
    a151( 31L ), // 广西北部湾银行
    // a152( Arrays.asList( "柳州银行" ) ),
    // a153( Arrays.asList( "广州银行" ) ),
    a154( 37L ),
    a155( 35L ), // 浙江泰隆商业银行
    // a156( Arrays.asList( "苏州农商银行" ) ),
    // a157( Arrays.asList( "浙江网商银行" ) ),
    // a158( Arrays.asList( "东莞银行" ) ),
    // a159( Arrays.asList( "广东和平商业银行" ) ),
    // a160( Arrays.asList( "松桃长征村镇银行" ) ),
    // a161( Arrays.asList( "上饶银行" ) ),
    // a162( Arrays.asList( "瑞丰银行" ) ),
    a163( 52L ), // 海南农信
    // a164( Arrays.asList( "廊坊银行" ) ),
    // a165( Arrays.asList( "苏州农村商业银行" ) ),
    // a166( Arrays.asList( "泰安银行" ) ),
    a167( 47L ), // 齐商银行
    //a168( Arrays.asList( "浙信村镇银行" ) ),
    // a169( Arrays.asList( "丹东银行" ) ),
    // a170( Arrays.asList( "抚顺银行" ) ),
    // a171( Arrays.asList( "临商银行" ) ),
    // a172( Arrays.asList( "阜新银行" ) ),
    // a173( Arrays.asList( "福建农商银行" ) ),
    a174( 46L ), // 陕西信合
    // a175( Arrays.asList( "鄞州银行" ) ),
    // a176( Arrays.asList( "曲靖市商业银行" ) ),
    // a177( Arrays.asList( "长城华西银行" ) ),
    // a178( Arrays.asList( "葫芦岛银行" ) ),
    // a179( Arrays.asList( "河北农村商业银行" ) ),
    // a180( Arrays.asList( "营口银行" ) ),
    // a181( Arrays.asList( "东营银行" ) ),
    // a182( Arrays.asList( "江苏江南农村商业银行" ) ),
    // a183( Arrays.asList( "陕西省农村信用合作行" ) ),
    a184( 44L ), // 恒丰银行
    // a185( Arrays.asList( "沧州银行" ) ),
    // a186( Arrays.asList( "唐山银行" ) ),
    // a187( Arrays.asList( "邢台银行" ) ),
    // a189( Arrays.asList( "大同银行" ) ),
    // a190( Arrays.asList( "内蒙古银行" ) ),
    // a191( Arrays.asList( "鄂尔多斯银行" ) ),
    // a192( Arrays.asList( "乌海银行" ) ),
    // a193( Arrays.asList( "大连银行" ) ),
    // a194( Arrays.asList( "龙江银行" ) ),
    // a195( Arrays.asList( "宁波通商银行" ) ),
    // a196( Arrays.asList( "宁波东海银行" ) ),
    // a197( Arrays.asList( "湖州银行" ) ),
    // a198( Arrays.asList( "嘉兴银行" ) ),
    // a199( Arrays.asList( "金华银行" ) ),
    // a200( Arrays.asList( "绍兴银行" ) ),
    // a201( Arrays.asList( "浙江稠州商业银行" ) ),
    a202( 55L ), // 福建海峡银行
    // a203( Arrays.asList( "泉州银行" ) ),
    // a204( Arrays.asList( "厦门国际银行" ) ),
    // a205( Arrays.asList( "九江银行" ) ),
    a206( 27L ), // 齐鲁银行
    // a207( Arrays.asList( "莱商银行" ) ),
    // a208( Arrays.asList( "日照银行" ) ),
    // a209( Arrays.asList( "平顶山银行" ) ),
    // a210( Arrays.asList( "洛阳银行" ) ),
    // a211( Arrays.asList( "焦作中旅银行" ) ),
    // a213( Arrays.asList( "广东南粤银行" ) ),
    // a214( Arrays.asList( "珠海华润银行" ) ),
    // a215( Arrays.asList( "海南银行" ) ),
    // a216( Arrays.asList( "达州银行" ) ),
    // a217( Arrays.asList( "四川天府银行" ) ),
    // a218( Arrays.asList( "自贡银行" ) ),
    // a219( Arrays.asList( "富滇银行" ) ),
    // a220( Arrays.asList( "西安银行" ) ),
    a221( 28L ), // 兰州银行
    // a222( Arrays.asList( "新疆银行" ) ),
    // a223( Arrays.asList( "乌鲁木齐银行" ) ),
    // a224( Arrays.asList( "青海银行" ) ),
    // a225( Arrays.asList( "西藏银行" ) ),
    // a227( Arrays.asList( "上海华瑞银行" ) ),
    // a228( Arrays.asList( "温州民商银行" ) ),
    // a229( Arrays.asList( "天津金城银行" ) ),
    // a230( Arrays.asList( "重庆富民银行" ) ),
    // a231( Arrays.asList( "四川新网银行" ) ),
    // a232( Arrays.asList( "湖南三湘银行" ) ),
    // a233( Arrays.asList( "福建华通银行" ) ),
    // a234( Arrays.asList( "武汉众邦银行" ) ),
    // a235( Arrays.asList( "北京中关村银行" ) ),
    // a236( Arrays.asList( "安徽新安银行" ) ),
    // a237( Arrays.asList( "江苏苏宁银行" ) ),
    // a238( Arrays.asList( "梅州客商银行" ) ),
    // a242( Arrays.asList( "陕西省农村信用行" ) ),
    // a243( Arrays.asList( "浙江民泰商业银行" ) ),
    // a244( Arrays.asList( "赣州银行" ) ),
    // a248( Arrays.asList( "江西赣州银座村镇银行" ) ),
    // a249( Arrays.asList( "湖南省农村信用行" ) ),
    // a250( Arrays.asList( "张家口银行" ) ),
    // a251( Arrays.asList( "锦州银行" ) ),
    // a252( Arrays.asList( "昆仑银行" ) ),
    // a253( Arrays.asList( "贵州农信银行" ) ),
    // a254( Arrays.asList( "宁夏银行" ) ),
    // a255( Arrays.asList( "江苏农商银行" ) ),
    // a256( Arrays.asList( "包商银行" ) ),
    // a257( Arrays.asList( "网商银行" ) ),
    // a258( Arrays.asList( "长治银行" ) ),
    // a259( Arrays.asList( "宜宾市商业银行" ) ),
    // a260( Arrays.asList( "中原银行" ) ),
    // a262( Arrays.asList( "重庆三峡银行" ) ),
    // a263( Arrays.asList( "甘肃银行" ) ),
    // a264( Arrays.asList( "邯郸银行" ) ),
    // a265( Arrays.asList( "云南省农村信用行" ) ),
    // a266( Arrays.asList( "苏州银行" ) ),
    // a267( Arrays.asList( "石嘴山银行" ) ),
    // a268( Arrays.asList( "石嘴山市城市信用行" ) ),
    a269( 40L ), // 盛京银行
    // a271( Arrays.asList( "江西银行" ) ),
    // a272( Arrays.asList( "四川农村商业银行" ) ),
    // a273( Arrays.asList( "威海市商业银行" ) ),
    // a274( Arrays.asList( "天津农村商业银行" ) ),
    a275( 41L ), // 长安银行
    a276( 39L ),// 长沙银行
    // a277( Arrays.asList( "湘江银行" ) ),
    // a278( Arrays.asList( "厦门银行" ) ),
    // a280( Arrays.asList( "晋城银行" ) ),
    // a281( Arrays.asList( "广东大展银行" ) ),
    a282( 38L ), // 河北银行
    // a283( Arrays.asList( "保定银行" ) ),
    // a284( Arrays.asList( "广西国民村镇银行" ) ),
    // a285( Arrays.asList( "贵阳银行" ) ),
    // a286( Arrays.asList( "湖北农村信用行" ) ),
    // a287( Arrays.asList( "哈尔滨银行" ) ),
    // a288( Arrays.asList( "河南农村信用行" ) ),
    // a289( Arrays.asList( "浙江农商银行" ) ),
    // a290( Arrays.asList( "郑州银行" ) ),
    // a293( Arrays.asList( "江苏农村商业银行" ) ),
    // a294( Arrays.asList( "北京农村商业银行" ) ),
    // a295( Arrays.asList( "广西农村信用行" ) ),
    // a298( Arrays.asList( "吉林银行" ) ),
    a299( 60L ), // 江苏银行
    // a300( Arrays.asList( "温州银行" ) ),
    // a30( Arrays.asList( "深圳农村商业银行" ) ),
    // a302( Arrays.asList( "江西农村信用行" ) ),
    a303( 23L ), // 南京银行
    a305( 26L ), // 上海农村商业银行
    a306( 20L ), // 东亚银行
    ;

    private final Long bankId;

    BankCodeHyType( Long bankId ) {
        this.bankId = bankId;
    }

    public static BankCodeHyType getCodeByBankId( Long bankId ) {
        for ( BankCodeHyType enumType : BankCodeHyType.values() ) {
            if ( Objects.equals( enumType.getBankId(), bankId ) ) {
                return enumType;
            }
        }
        return null;
    }

}
