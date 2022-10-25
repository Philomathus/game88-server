package tv.game88.core.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import tv.game88.common.page.PageDomain;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.enums.EnumReqTime;

@Data
@ApiModel( "账户明细请求数据" )
public class ReqLogMoney extends PageDomain {
	@ApiModelProperty( value = "交易状态" )
	private EnumMoney   enumMoney;
	@ApiModelProperty( value = "交易时间" )
	private EnumReqTime enumReqTime;
}
