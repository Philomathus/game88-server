package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 代付通道对象 pay_agent_channel
 *
 * @author mengJun
 * @date 2021-01-26
 */
@Data
@NoArgsConstructor
public class PayAgentChannel {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "代付通道名称" )
    private String        name;
    @Excel( name = "代付平台ID" )
    private Long          platformId;
    @Excel( name = "商户ID" )
    private String        merId;
    /**
     * 头部值
     */
    private String        headerValue;
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
    @Excel( name = "激活状态" )
    private Boolean       effect;
    @Excel( name = "创建人" )
    private String        createBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "修改人" )
    private String        updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "修改时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
}
