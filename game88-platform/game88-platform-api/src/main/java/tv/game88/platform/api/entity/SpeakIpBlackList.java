package tv.game88.platform.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 【请填写功能名称】对象 speak_ip_black_list
 *
 * @author 77tv
 * @date 2021-02-22
 */
@Data
public class SpeakIpBlackList implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会员ID
     */
    private Integer       id;
    private String        userId;
    private String        userIp;
    private String        msg;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "createTime", getCreateTime() )
                .append( "userIp", getUserIp() )
                .append( "msg", getMsg() )
                .toString();
    }
}