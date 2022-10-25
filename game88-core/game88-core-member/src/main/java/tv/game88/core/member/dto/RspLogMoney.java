package tv.game88.core.member.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel( "账户明细" )
public class RspLogMoney {

    @ApiModelProperty( value = "时间" )
    private String     createTime;
    @ApiModelProperty( value = "状态" )
    private String     des;
    @ApiModelProperty( value = "支出" )
    private BigDecimal pay;
    @ApiModelProperty( value = "收入" )
    private BigDecimal income;
    @ApiModelProperty( value = "余额" )
    private BigDecimal total;
    @ApiModelProperty( value = "余额" )
    private BigDecimal totalBefore;

}
