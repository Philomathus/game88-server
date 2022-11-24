package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Data
public class RspPayChannel {
    @Schema( title = "主键" )
    private Long       id;
    @Schema( title = "通道名称" )
    private String     name;
    @Schema( title = "充值最低" )
    private BigDecimal rechargeMin;
    @Schema( title = "充值最高" )
    private BigDecimal rechargeMax;
    @Schema( title = "快捷金额" )
    private String     quickAmount;
    @Excel( name = "开放层级-最小" )
    private Integer    openLevelMin;
    @Excel( name = "开放层级-最大" )
    private Integer    openLevelMax;
    @Schema( title = "备注提示" )
    private String     remark;

    public BigDecimal getRechargeMin() {
        if ( StringUtils.isNotBlank( quickAmount ) ) {
            List<String> quickAmounts = Arrays.asList( quickAmount.split( "," ) );
            return new BigDecimal( quickAmounts.get( 0 ) );
        }
        return rechargeMin;
    }

    public BigDecimal getRechargeMax() {
        if ( StringUtils.isNotBlank( quickAmount ) ) {
            List<String> quickAmounts = Arrays.asList( quickAmount.split( "," ) );
            return new BigDecimal( quickAmounts.get( quickAmounts.size() - 1 ) );
        }
        return rechargeMax;
    }
}
