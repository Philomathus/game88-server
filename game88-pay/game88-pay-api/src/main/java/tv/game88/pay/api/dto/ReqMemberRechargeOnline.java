package tv.game88.pay.api.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class ReqMemberRechargeOnline {
    private String[] downLoadDate = new String[2];
    private String id;
    private String memberId;
    private String platformId;
    private String channelId;
    private String tradeSn;
    private BigDecimal money;
    private BigDecimal subMoney;
    private String paymentCode;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private String payTime;
    private String status;
    private Integer isPatchOrder;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private String createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private String updateTime;
    private String remark;
    private Long first;
    private String currencyCode;

    @JsonIgnore
    private String channelName;
    @JsonIgnore
    private String   searchValue;
    @JsonIgnore
    private String   searchOrderNo;
    @JsonIgnore
    private String[] selectDate;
    @JsonIgnore
    private String   selectStartDate;
    @JsonIgnore
    private String   selectEndDate;

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
