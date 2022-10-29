package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付平台对象 pay_platform
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class PayPlatform {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "平台名称" )
    private String        name;
    @Excel( name = "平台编码" )
    private String        code;
    @Excel( name = "商户ID" )
    private String        merId;
    @Excel( name = "应用ID" )
    private String        appId;
    @Excel( name = "下单接口地址" )
    private String        payUrl;
    @Excel( name = "订单查询地址" )
    private String        queryUrl;
    /**
     * md5加密密钥
     */
    private String        signMd5;
    /**
     * 加密公钥
     */
    private String        signPublicKey;
    /**
     * 解密私钥
     */
    private String        signPrivateKey;
    @Excel( name = "回调IP白名单" )
    private String        whiteIp;
    @Excel( name = "创建人" )
    private String        createBy;
    @Excel( name = "修改人" )
    private String        updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
    @Excel( name = "备注" )
    private String        remark;
}
