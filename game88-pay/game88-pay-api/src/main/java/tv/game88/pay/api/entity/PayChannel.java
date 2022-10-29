package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * pay_channel
 *
 * @author mengJun
 * @date 2021-01-27
 */
@Data
public class PayChannel {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "通道名称" )
    private String        name;
    @Excel( name = "通道编码" )
    private String        channelCode;
    @Excel( name = "平台ID" )
    private Long          platformId;
    @Excel( name = "类型ID" )
    private Long          typeId;
    @Excel( name = "状态" )
    private Boolean       effect      = false;
    @Excel( name = "是否允许回调" )
    private Boolean       canCallback = true;
    @Excel( name = "开放层级-最小" )
    private Integer       openLevelMin;
    @Excel( name = "开放层级-最大" )
    private Integer       openLevelMax;
    @Excel( name = "优惠比例" )
    private String        discountBill;
    @Excel( name = "快捷金额" )
    private String        quickAmount;
    @Excel( name = "通道费率" )
    private BigDecimal    rate;
    @Excel( name = "创建人" )
    private String        createBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "修改人" )
    private String        updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

    @TableField( exist = false )
    private String successRate;

    @TableField( exist = false )
    private String rateStr;

    public String getRateStr() {
        if ( rate != null ) {
            return rate
                    .multiply( new BigDecimal( 100 ) )
                    .setScale( 1, RoundingMode.HALF_UP )
                    .toString()
                    .concat( "%" );
        }
        return "";
    }
}
