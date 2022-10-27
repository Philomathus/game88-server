package tv.game88.platform.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName(value ="config_sms_faillog")
@Data
public class ConfigSmsFaillog {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 短信异常消息 
     */
    private String errorMessage;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信提供方
     */
    private String smsName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 短信名称
     */
    private String smsSubname;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append(", id=").append(id);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorMessage=").append(errorMessage);
        sb.append(", phone=").append(phone);
        sb.append(", smsName=").append(smsName);
        sb.append(", createTime=").append(createTime);
        sb.append(", smsSubname=").append(smsSubname);
        sb.append("]");
        return sb.toString();
    }
}