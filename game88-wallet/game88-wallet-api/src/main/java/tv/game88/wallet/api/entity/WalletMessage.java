package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.vo.BaseEntity;
import tv.game88.wallet.api.type.WalletMessageEnum;

import java.io.Serial;

/**
 * 站内信对象 wallet_message
 *
 * @author MengJun
 */
@TableName( "wallet_message" )
@Data
@EqualsAndHashCode( callSuper = true )
public class WalletMessage extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

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
}