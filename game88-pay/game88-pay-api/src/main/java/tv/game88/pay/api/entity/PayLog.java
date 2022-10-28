package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
    @Excel( name = "支付平台编号" )
    private String        platformId;
    @Excel( name = "支付平台名称" )
    private String        platformName;
    @Excel( name = "支付通道编号" )
    private String        channelId;
    @Excel( name = "支付通道名称" )
    private String        channelName;
    @Excel( name = "下单金额" )
    private BigDecimal    money;
    @Excel( name = "是否下单成功" )
    private boolean       success;
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

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "memberId", getMemberId() )
                .append( "platformId", getPlatformId() )
                .append( "platformName", getPlatformName() )
                .append( "channelId", getChannelId() )
                .append( "channelName", getChannelName() )
                .append( "money", getMoney() )
                .append( "success", isSuccess() )
                .append( "failReason", getFailReason() )
                .append( "createTime", getCreateTime() )
                .toString();
    }
}
