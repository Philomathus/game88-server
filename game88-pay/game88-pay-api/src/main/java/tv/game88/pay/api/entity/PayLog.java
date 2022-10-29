package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * pay_log
 *
 * @author mengJun
 * @date 2021-01-26
 */
@Data
public class PayLog {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "会员编号" )
    private String        memberId;
    @Excel( name = "平台ID" )
    private Long          platformId;
    @Excel( name = "平台名称" )
    private String        platformName;
    @Excel( name = "通道ID" )
    private Long          channelId;
    @Excel( name = "通道名称" )
    private String        channelName;
    @Excel( name = "下单金额" )
    private BigDecimal    money;
    @Excel( name = "是否成功" )
    private Boolean       success;
    @Excel( name = "失败原因" )
    private String        failReason;
    @Excel( name = "创建时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    @TableField( exist = false )
    private Integer    countTotal;//总成功笔数
    @TableField( exist = false )
    private BigDecimal countSuccessMoney;//总成功金额
    @TableField( exist = false )
    private Integer    countSuccess;//成功笔数

    @JsonIgnore
    @TableField( exist = false )
    private String[] selectDate;
    @JsonIgnore
    @TableField( exist = false )
    private String   selectStartDate;
    @JsonIgnore
    @TableField( exist = false )
    private String   selectEndDate;

}
