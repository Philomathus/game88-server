package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class ReqPayRecharge {
	@Schema( title = "支付通道ID", required = true )
	@NotNull(message = "请选择通道")
	private Integer    channelId;
	@Schema( title = "充值金额", required = true )
	@NotNull(message = "请选择金额")
	private BigDecimal money;
	@Schema( title = "真实IP", required = true )
	private String     realIp;

	@Schema( hidden = true )
	private String     orderNo;
	// 失败原因
	@Schema( hidden = true )
	private String     failReason;

	@Schema( hidden = true )
	private String name;
	@Schema( hidden = true )
	private String upperOrderNo;
	@Schema( hidden = true )
	private String userId;

	@Schema( hidden = true )
	private String ticket;

	//(如果不为空且为“1”，则是跳转HTML)
	@Schema( hidden = true )
	private Integer urlType;

	public BigDecimal getMoney() {
		return money == null ? null : money.setScale( 0, RoundingMode.HALF_UP );
	}
}
