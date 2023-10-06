package tv.game88.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName( value = "config_sms_faillog" )
@Data
public class ConfigSmsFaillog {
    /**
     *
     */
    @TableId( type = IdType.AUTO )
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
    private LocalDateTime createTime;

    /**
     * 短信名称
     */
    private String smsSubname;

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + ", id=" + id + ", errorCode=" + errorCode + ", errorMessage=" + errorMessage
                + ", phone=" + phone + ", smsName=" + smsName + ", createTime=" + createTime + ", smsSubname=" + smsSubname + "]";
    }
}