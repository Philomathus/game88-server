package tv.game88.pay.api.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ReqMemberRechargeUsdt {
    @Schema( title = "USDT渠道系统编号", required = true )
    @NotNull(message = "USDT渠道系统编号为空")
    private Long   id;
    @Schema( title = "交易ID", required = true )
    @NotBlank(message = "请输入交易ID")
    private String transactionId;
    @Schema( title = "充值数量", required = true )
    @NotNull(message = "请输入充值USDT数量")
    private Long rechargeNumber;

    @Schema( hidden = true )
    private String        memberId;
    @Schema( hidden = true )
    private String        userName;
    @Schema( hidden = true )
    private String        channelName;
    @Schema( hidden = true )
    private BigDecimal    rechargeMoney;
    @Schema( hidden = true )
    private String        status;
    @Schema( hidden = true )
    private BigDecimal    discountBill;
    @Schema( hidden = true )
    private String        chainName;
    @Schema( hidden = true )
    private String        rechargeAddress;
    @Schema( hidden = true )
    private String        opName;
    @Schema( hidden = true )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Schema( hidden = true )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
    @Schema( hidden = true )
    private String        remark;

    @Schema( hidden = true )
    private String[] selectDate;
    @Schema( hidden = true )
    private String   selectStartDate;
    @Schema( hidden = true )
    private String   selectEndDate;
    @Schema( hidden = true )
    private Integer  googleAuthCode;

    /**
     * 请求参数
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField(exist = false)
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if ( params == null ) {
            params = new HashMap<>();
        }
        return params;
    }

}
