package tv.game88.core.member.enums;

import lombok.Getter;

import java.util.Objects;

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

	GAME_OUT( 101, "游戏下分", false ),
	ACTIVITY( 102, "优惠活动", true ),
	BOHUI( 103, "取款驳回", false ),
	UP_SCORE( 105, "补分", false ),
	WONGIVE(113,"赠送彩金",false),
	COMMISSION( 104, "推广佣金", true ),
	DICE_BONUS( 105, "投注中奖", false ),
	QUEST_BONUS( 106, "任务奖金", false ),
	SAFE_BOX( 107, "保险箱记录", false ),
	CODE_CLEAN(108,"洗码",false),
	REFUND_BET_AMOUNT( 109, "回退金额", false ),

	GM(110,"人工入款",true),

	/****************type>0 入款type<0  消费  *************************/
	GAME_IN( -101, "游戏上分", false ),
	WITHDRAW( -103, "人工取款", false ),
	LOTTERY_BET( -104, "筹码投注", false ),
	PLATFORM(-1,"平台资金切换",false);

	private Integer type;
	private String  des;
	private Boolean bcode;

	EnumMoney( Integer type, String des, Boolean bcode ) {
		this.type = type;
		this.des = des;
		this.bcode = bcode;
	}

	public static EnumMoney getByType(Integer type){
		for ( EnumMoney value : EnumMoney.values() ) {
			if( Objects.equals( value.getType(), type ) ){
				return value;
			}
		}
		return null;
	}
}
