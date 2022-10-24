package tv.game88.core.admin.exception;

/**
 * 用户信息异常类
 *
 * @author MengJun
 */
public class UserException extends BaseException {
	private static final long serialVersionUID = 1L;

	public UserException( String code, Object[] args ) {
		super( "user", code, args, null );
	}
}
