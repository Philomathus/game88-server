package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对象 PayRechargeUsdt
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class PayRechargeUsdt {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Excel(name = "渠道名称")
    private String channelName;
    @Excel(name = "链名称")
    private String chainName;
    @Excel(name = "充值地址")
    private String rechargeAddress;
    @Excel(name = "优惠比例")
    private BigDecimal discountBill;
    @Excel(name = "usdt汇率")
    private BigDecimal exchangeRate;
    @Excel(name = "钱包二维码")
    private String icon;
    @Excel(name = "排序")
    private Integer sort;
    @Excel(name = "激活状态")
    private Boolean effect;
    @Excel(name = "开放层级 小")
    private Long openLevelMin;
    @Excel(name = "开放层级 大")
    private Long openLevelMax;

    @Excel(name = "创建人")
    private String createBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel(name = "更新人")
    private String updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

    @Excel(name = "备注")
    private String remark;
}
