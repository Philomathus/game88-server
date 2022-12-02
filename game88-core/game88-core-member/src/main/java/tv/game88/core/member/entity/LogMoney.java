package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 资金日志对象 log_money
 *
 * @author mengJun
 */
@Data
public class LogMoney {
    @TableId( value = "id", type = IdType.INPUT )
    private String        id;
    @Excel( name = "会员编号" )
    private String        userId;
    @Excel( name = "会员账号" )
    private String        userName;
    @Excel( name = "变化类型" )
    private Integer       type;
    @Excel( name = "描述" )
    private String        des;
    @Excel( name = "收入" )
    private BigDecimal    income;
    @Excel( name = "支出" )
    private BigDecimal    pay;
    @Excel( name = "变化前余额" )
    private BigDecimal    totalBefore;
    @Excel( name = "变化后余额" )
    private BigDecimal    total;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "备注信息" )
    private String        mark;
    @Excel( name = "备注订单号" )
    private String        markorder;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String[] types;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String[] selectDate;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String   startTime;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String   endTime;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String   tableLast;
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String   searchValue;

    /**
     * 请求参数
     */
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if ( params == null ) {
            params = new HashMap<>();
        }
        return params;
    }
}
