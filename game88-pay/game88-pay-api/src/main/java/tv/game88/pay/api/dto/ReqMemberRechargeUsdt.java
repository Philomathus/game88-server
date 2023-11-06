package tv.game88.pay.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
