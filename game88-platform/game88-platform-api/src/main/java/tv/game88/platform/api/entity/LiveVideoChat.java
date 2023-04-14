package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员发言对象 live_video_chat
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class LiveVideoChat implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	/**
	 * 主播ID
	 */
	@Excel( name = "主播ID" )
	private Long poscatId;

	/**
	 * 消息所在聊天组
	 */
	@Excel( name = "消息所在聊天组" )
	private String group;

	/**
	 * 发送者id
	 */
	@Excel( name = "发送者id" )
	private Long userId;

    /**
     * 用户类型
     */
    private Integer status;
	/**
	 * 消息内容
	 */
	@Excel( name = "消息内容" )
	private String msg;

	/**
	 * 消息类型 0 普通消息 1 弹幕消息
	 */
	@Excel( name = "消息类型 0 普通消息 1 弹幕消息" )
	private Integer type;

	/**
	 * 主播昵称
	 */
	@Excel( name = "主播昵称" )
	private String poscatNickName;

	/**
	 * 发送者昵称
	 */
	@Excel( name = "发送者昵称" )
	private String userNickName;

	/**
	 * 发送开始时间
	 */
	private String sendStartTime;

	/**
	 * 发送结束时间
	 */
	private String sendEndTime;

	/**
	 * 平台会员ID
	 */
	@Excel( name = "平台会员ID" )
	private String fromPlatform;
	@Excel( name = "用户ip" )
	private String userIp;
	private String createTimes;

	private boolean isNoSpeaking = false;
	private boolean isForbid     = false;

	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	private LocalDateTime createTime;

	@Override
	public String toString() {
		return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
				.append( "id", getId() )
				.append( "poscatId", getPoscatId() )
				.append( "group", getGroup() )
				.append( "userId", getUserId() )
				.append( "msg", getMsg() )
				.append( "status", getStatus() )
				.append( "createTime", getCreateTime() )
				.append( "type", getType() )
				.append( "poscatNickName", getPoscatNickName() )
				.append( "userNickName", getUserNickName() )
				.append( "fromPlatform", getFromPlatform() )
				.toString();
	}
}
