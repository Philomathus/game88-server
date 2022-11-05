package tv.game88.pay.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class RspPayRechargeBank {
    private Long       id;
    @Schema( title = "图标" )
    private String     bankIcon;
    @Schema( title = "银行名称" )
    private String     bankName;
    @Schema( title = "银行账号" )
    private String     bankAccount;
    @Schema( title = "开户人姓名" )
    private String     accountName;
    @Schema( title = "开户地址" )
    private String     bankAddress;
    @Schema( title = "优惠比例" )
    private BigDecimal discountBill;
    @Schema( title = "充值限额" )
    private String     bankChargeLimit;
    @Schema( title = "备注信息" )
    private String     remark;

    @Schema( hidden = true )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private BigDecimal rechargeLimitMin;
    @Schema( hidden = true )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private BigDecimal rechargeLimitMax;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @Schema( hidden = true )
    private String     restProvince;
}
