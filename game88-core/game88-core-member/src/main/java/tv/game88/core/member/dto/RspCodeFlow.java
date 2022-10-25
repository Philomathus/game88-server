package tv.game88.core.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspCodeFlow {
    @ApiModelProperty( value = "充值时间" )
    private String     createTime;
    @ApiModelProperty( value = "需求打码量" )
    private BigDecimal income;
    @ApiModelProperty( value = "充值类型" )
    private String     des;
    @ApiModelProperty( value = "实际打码量" )
    private BigDecimal cur;
    @ApiModelProperty( value = "0=未打码 1=已打码" )
    private Integer    status;
    @ApiModelProperty( value = "充值金额" )
    private BigDecimal charge;
}
