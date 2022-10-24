package tv.game88.admin.system.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前在线会话
 *
 * @author MengJun
 */
@Data
public class SysUserOnline {
	/**
	 * 会话编号
	 */
	private String tokenId;
	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 登录IP地址
	 */
	private String ipaddr;

	/**
	 * 浏览器类型
	 */
	private String browser;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 登录时间
	 */
	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	private LocalDateTime loginTime;
}
