package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代付通道对象 pay_agent_channel
 *
 * @author mengJun
 * @date 2021-01-26
 */
@Data
public class PayAgentPlatform {
    @TableId( type = IdType.AUTO )
    private Long   id;
    @Excel( name = "代付编码" )
    private String code;
    @Excel( name = "代付名称" )
    private String name;
    @Excel( name = "代付下单地址" )
    private String orderUrl;
    @Excel( name = "代付查询地址" )
    private String orderQueryUrl;
    @Excel( name = "回调IP白名单" )
    private String whiteIp;

    @Excel( name = "是否配置头部值" )
    private boolean headerValue;
    @Excel( name = "是否配置MD5密钥" )
    private boolean signMd5;
    @Excel( name = "是否配置加密公钥" )
    private boolean signPublicKey;
    @Excel( name = "是否配置解密私钥" )
    private boolean signPrivateKey;

    @Excel( name = "头部值配置说明" )
    private String headerValueExplain;
    @Excel( name = "MD5密钥配置说明" )
    private String signMd5Explain;
    @Excel( name = "加密公钥配置说明" )
    private String signPublicKeyExplain;
    @Excel( name = "解密私钥配置说明" )
    private String signPrivateKeyExplain;

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
}
