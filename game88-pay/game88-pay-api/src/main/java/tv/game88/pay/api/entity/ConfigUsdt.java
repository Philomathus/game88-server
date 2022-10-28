package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 对象 ConfigUsdt
 *
 * @author 77tv
 * @date 2021-09-11
 */
@Data
public class ConfigUsdt {
    private static final long serialVersionUID = 1L;

    /** 系统编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 渠道名称 */
    @Excel(name = "渠道名称")
    private String channelName;

    /** 链名称 */
    @Excel(name = "链名称")
    private String chainName;

    /** 充值地址 */
    @Excel(name = "充值地址")
    private String rechargeAddress;

    /** 优惠比例 */
    @Excel(name = "优惠比例")
    private BigDecimal discountBill;

    /** usdt汇率 */
    @Excel(name = "usdt汇率")
    private BigDecimal exchangeRate;

    /** 排序 */
    @Excel(name = "排序")
    private Long indexs;

    /** 钱包二维码 */
    @Excel(name = "钱包二维码")
    private String icon;

    /** 状态(1启用0停用) */
    @Excel(name = "状态(1启用0停用)")
    private String status;

    /** 开放层级最小 */
    @Excel(name = "开放层级最小")
    private Long openLevelType;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("channelName", getChannelName())
            .append("chainName", getChainName())
            .append("rechargeAddress", getRechargeAddress())
            .append("discountBill", getDiscountBill())
            .append("exchangeRate", getExchangeRate())
            .append("indexs", getIndexs())
            .append("icon", getIcon())
            .append("status", getStatus())
            .append("openLevelType", getOpenLevelType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
