package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

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
    private String type;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE ).append( "id", getId() ).append( "title", getTitle() )
                                                                          .append( "content", getContent() )
                                                                          .append( "receiverUserId", getReceiverUserId() )
                                                                          .append( "isRead", getIsRead() )
                                                                          .append( "createTime", getCreateTime() )
                                                                          .append( "createBy", getCreateBy() )
                                                                          .append( "type", getType() ).toString();
    }
}