package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tv.game88.wallet.api.type.WalletMessageEnum;

import java.time.LocalDateTime;

/**
 * 站内信对象 wallet_message
 *
 * @author MengJun
 */
@TableName( "wallet_message" )
@Data
public class WalletMessage {

    /**
     * 系统编号
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 接收用户id
     */
    private String receiverUserId;

    /**
     * 是否已读
     * // 1 已读 0 未读
     */
    private Boolean isRead;

    /**
     * 消息类型
     */
    private WalletMessageEnum type;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String beginTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String endTime;
}