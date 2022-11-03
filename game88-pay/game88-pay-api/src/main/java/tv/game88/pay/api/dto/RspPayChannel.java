package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspPayChannel {
	@Schema( title = "主键" )
	private Long    id;
	@Schema( title = "通道名称" )
	private String     name;
	@Schema( title = "充值最低" )
	private BigDecimal rechargeMin;
	@Schema( title = "充值最高" )
	private BigDecimal rechargeMax;
	@Schema( title = "快捷金额" )
	private String     quickAmount;
	@Excel( name = "开放层级-最小" )
	private Integer       openLevelMin;
	@Excel( name = "开放层级-最大" )
	private Integer       openLevelMax;
	@Schema( title = "备注提示" )
	private String     remark;
}
