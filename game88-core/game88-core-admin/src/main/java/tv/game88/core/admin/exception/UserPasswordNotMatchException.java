package tv.game88.core.admin.exception;

/**
 * 用户密码不正确或不符合规范异常类
 *
 * @author MengJun
 */
public class UserPasswordNotMatchException extends UserException {
	private static final long serialVersionUID = 1L;

	public UserPasswordNotMatchException() {
		super( "用户不存在/密码错误", null );
	}
}
