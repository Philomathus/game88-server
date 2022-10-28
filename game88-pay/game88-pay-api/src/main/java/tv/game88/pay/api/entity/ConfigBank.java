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
 * 对象 config_bank
 *
 * @author 77lm
 * @date 2021-10-14
 */
@Data
public class ConfigBank {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图标 */
    @Excel(name = "图标")
    private String icon;

    /** 银行编码 */
    @Excel(name = "银行编码")
    private String code;

    /** 银行名称 */
    @Excel(name = "银行名称")
    private String name;

    /** 银行账号 */
    @Excel(name = "银行账号")
    private String bankAccount;

    /** 开户人姓名 */
    @Excel(name = "开户人姓名")
    private String accountName;

    /** 开户地址 */
    @Excel(name = "开户地址")
    private String bankAddress;

    /** 优惠比例 */
    @Excel(name = "优惠比例")
    private BigDecimal discountBill;

    /** 开放层级 0所有 1仅vip */
    @Excel(name = "开放层级 0所有 1仅vip")
    private String openLevelType;

    /** 状态(1启用0停用) */
    @Excel(name = "状态(1启用0停用)")
    private String status;

    /** 备注信息 */
    @Excel(name = "备注信息")
    private String remark;

    /** 创建人 */
    @Excel(name = "创建人")
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后更新人 */
    @Excel(name = "最后更新人")
    private String updateBy;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后更新时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    @JsonIgnore
    private Integer googleAuthCode;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("icon", getIcon())
            .append("code", getCode())
            .append("name", getName())
            .append("bankAccount", getBankAccount())
            .append("accountName", getAccountName())
            .append("bankAddress", getBankAddress())
            .append("discountBill", getDiscountBill())
            .append("openLevelType", getOpenLevelType())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
