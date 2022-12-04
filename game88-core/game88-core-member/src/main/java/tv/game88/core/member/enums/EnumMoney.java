package tv.game88.core.member.enums;

import lombok.Getter;
import tv.game88.core.member.dto.RspConfigTradeType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 交易类型 type>0  为入金 type>0&&type<100  为充值
 * <p>
 * <p>
 * type<0  为消费
 */
@Getter
public enum EnumMoney {
    DEPOSIT( 1, "银行卡充值", true ),
    USDT( 2, "USDT充值", true ),
    PAY( 3, "支付充值", true ),
    PAY_AGENT( 4, "代充入款", true ),

    GAME_FAIL( 100, "游戏上分失败", false ),
    GAME_OUT( 101, "游戏下分", false ),
    ACTIVITY( 102, "优惠活动", true ),
    BOHUI( 103, "取款驳回", false ),
    COMMISSION( 104, "推广佣金", true ),
    LOTTERY_BONUS( 105, "彩票中奖", false ),
    QUEST_BONUS( 106, "任务奖金", false ),
    SAFE_BOX( 107, "保险箱记录", false ),
    CODE_CLEAN( 108, "洗码", false ),
    REFUND_BET_AMOUNT( 109, "回退金额", false ),
    WONGIVE( 110, "赠送彩金", false ),

    GM( 111, "人工入款", true ),

    DEPOSIT_BONUS( 120, "充值彩金", true ),

    /****************type>0 入款type<0  消费  *************************/
    GAME_IN( -101, "游戏上分", false ),
    WITHDRAW( -103, "会员提现", false ),
    LOTTERY_BET( -104, "彩票投注", false ),
    PLATFORM( -1, "平台资金切换", false );

    private Integer type;
    private String  des;
    private Boolean bcode;

    EnumMoney( Integer type, String des, Boolean bcode ) {
        this.type  = type;
        this.des   = des;
        this.bcode = bcode;
    }

    public static EnumMoney getByType( Integer type ) {
        for ( EnumMoney value : EnumMoney.values() ) {
            if ( Objects.equals( value.getType(), type ) ) {
                return value;
            }
        }
        return null;
    }

    public static List<RspConfigTradeType> getTradeTypes() {
        return Arrays.stream( EnumMoney.values() ).map( m -> {
            RspConfigTradeType tradeType = new RspConfigTradeType();
            tradeType.setName( m.name() );
            tradeType.setType( m.getType() );
            tradeType.setDes( m.getDes() );
            return tradeType;
        } ).sorted( Comparator.comparing( RspConfigTradeType::getType ) ).collect( Collectors.toList() );
    }
}
