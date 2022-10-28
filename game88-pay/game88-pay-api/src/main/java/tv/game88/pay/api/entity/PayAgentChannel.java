package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 代付通道对象 pay_agent_channel
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class PayAgentChannel {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 代付通道编码 */
    @Excel(name = "代付通道编码")
    private String channelCode;

    /** 代付通道名称 */
    @Excel(name = "代付通道名称")
    private String channelName;

    /** 代付平台ID */
    @Excel(name = "代付平台ID")
    private Integer platformId;

    /** 商户ID */
    @Excel(name = "商户ID")
    private String merId;

    /** 代付下单地址 */
    @Excel(name = "代付下单地址")
    private String payOrderAddr;

    /** 代付查询地址 */
    @Excel(name = "代付查询地址")
    private String payOrderQueryAddr;

    /** 币种编码 */
    @Excel(name = "币种编码")
    private String currencyCode;

    /** 头部key */
    private String headerKey;

    /** md5加密密钥 */
    private String signMd5;

    /** 加密公钥 */
    private String signPublicKey;

    /** 解密私钥 */
    private String signPrivateKey;

    /** 平台IP白名单 */
    private String platWhiteIpList;

    /** 状态 1启用 0禁用 */
    private String status;

    /** 创建人 */
    @Excel(name = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /** 修改人 */
    @Excel(name = "修改人")
    private String updator;

    @TableField(exist = false)
    private String platformCode;

    @TableField(exist = false)
    private String platformName;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("channelCode", getChannelCode())
            .append("channelName", getChannelName())
            .append("platformId", getPlatformId())
            .append("merId", getMerId())
            .append("payOrderAddr", getPayOrderAddr())
            .append("payOrderQueryAddr", getPayOrderQueryAddr())
            .append("currencyCode", getCurrencyCode())
            .append("headerKey", getHeaderKey())
            .append("signMd5", getSignMd5())
            .append("signPublicKey", getSignPublicKey())
            .append("signPrivateKey", getSignPrivateKey())
            .append("platWhiteIpList", getPlatWhiteIpList())
            .append("status", getStatus())
            .append("creator", getCreator())
            .append("createTime", getCreateTime())
            .append("updator", getUpdator())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
