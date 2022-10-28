package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 支付平台对象 pay_platform
 *
 * @author 77tv
 * @date 2021-01-27
 */
@Data
public class PayPlatform{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 平台名称 */
    @Excel(name = "平台名称")
    private String name;

    /** 平台编码 */
    @Excel(name = "平台编码")
    private String code;

    /** 商户ID */
    @Excel(name = "商户ID")
    private String merId;

    /** 机构号 */
    @Excel(name = "机构号")
    private String orgId;

    /** 平台下单接口地址 */
    @Excel(name = "平台下单接口地址")
    private String platPayUrl;

    /** 平台订单查询地址 */
    @Excel(name = "平台订单查询地址")
    private String platQueryUrl;

    /** md5加密密钥 */
//    @Excel(name = "md5加密密钥")
    private String signMd5;

    /** 加密公钥 */
//    @Excel(name = "加密公钥")
    private String signPublicKey;

    /** 解密私钥 */
//    @Excel(name = "解密私钥")
    private String signPrivateKey;

    /** 平台IP白名单 */
    @Excel(name = "平台IP白名单")
    private String platWhiteIpList;

    /** 创建人 */
    @Excel(name = "创建人")
    private String creator;

    /** 修改人 */
    @Excel(name = "修改人")
    private String updator;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

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
            .append("name", getName())
            .append("code", getCode())
            .append("merId", getMerId())
            .append("orgId", getOrgId())
            .append("platPayUrl", getPlatPayUrl())
            .append("platQueryUrl", getPlatQueryUrl())
            .append("signMd5", getSignMd5())
            .append("signPublicKey", getSignPublicKey())
            .append("signPrivateKey", getSignPrivateKey())
            .append("platWhiteIpList", getPlatWhiteIpList())
            .append("creator", getCreator())
            .append("createTime", getCreateTime())
            .append("updator", getUpdator())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
