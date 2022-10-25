package tv.game88.core.member.enums;

import lombok.Getter;

/**
 * 交易类型 type>0  为入金 type>0&&type<100  为充值
 * <p>
 * <p>
 * type<0  为消费
 */
@Getter
public enum EnumMoney {
	DEPOSIT( 1L, "银行卡充值", true ),
	USDT( 2L, "USDT充值", true ),
	PAY( 3L, "支付充值", true ),

	GAME_OUT( 101L, "游戏下分", false ),
	ACTIVITY( 102L, "优惠活动", true ),
	BOHUI( 103L, "取款驳回", false ),
	UP_SCORE( 105L, "补分", false ),
	WONGIVE(113L,"赠送彩金",false),
	COMMISSION( 104L, "推广佣金", true ),
	DICE_BONUS( 105L, "投注中奖", false ),
	QUEST_BONUS( 106L, "任务奖金", false ),
	SAFE_BOX( 107L, "保险箱记录", false ),
	CODE_CLEAN(108L,"洗码",false),
	REFUND_BET_AMOUNT( 109L, "回退金额", false ),

	GM(110L,"人工入款",true),

	/****************type>0 入款type<0  消费  *************************/
	GAME_IN( -101L, "游戏上分", false ),
	BUY_VIP( -102L, "购买VIP", false ),
	WITHDRAW( -103L, "人工取款", false ),
	LOTTERY_BET( -104L, "筹码投注", false ),
	PLATFORM(-1L,"平台资金切换",false);

	private Long    type;
	private String  des;
	private Boolean bcode;

	EnumMoney( Long type, String des, Boolean bcode ) {
		this.type = type;
		this.des = des;
		this.bcode = bcode;
	}

}
