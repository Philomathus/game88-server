package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 代付信息日志对象 pay_agent_log
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class PayAgentLog {
    @Excel( name = "提现订单号" )
    @TableId( type = IdType.INPUT )
    private String        withdrawOrderNo;
    @Excel( name = "代付通道ID" )
    private Long          channelId;
    @Excel( name = "代付通道名称" )
    private String        channelName;
    @Excel( name = "提现者ID" )
    private String        withdrawId;
    @Excel( name = "提现金额" )
    private BigDecimal    withdrawMoney;
    @Excel( name = "三方代付订单号" )
    private String        agentOrderNo;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "提交时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "回调时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime callbackTime;
    // 0 回调中 1 成功 2失败
    @Excel( name = "回调状态" )
    private Integer       callbackStatus;
    @Excel( name = "备注" )
    private String        remark;

    /**
     * 请求参数
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField( exist = false )
    private Map<String, Object> params = new HashMap<>();

}
