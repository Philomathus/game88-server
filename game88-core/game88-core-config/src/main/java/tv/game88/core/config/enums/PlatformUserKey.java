package tv.game88.core.config.enums;

import lombok.Getter;

@Getter
public enum PlatformUserKey {
	//
	USER_ID( "userId", "用户平台ID" ),
	USER_NAME( "userName", "用户登录账户" ),
	NICK_NAME( "nickName", "用户昵称" ),
	HEAD_IMAGE( "headImage", "用户头像" ),
	VIP( "vip", "vip等级" ),
	STATUS( "status", "用户状态" ),
	SPEAK( "speak", "是否永久禁言" ),
	INVITER( "inviter", "邀请码" ),
	;

	private String key;
	private String des;

	PlatformUserKey(String key, String des ) {
		this.key = key;
		this.des = des;
	}
}
