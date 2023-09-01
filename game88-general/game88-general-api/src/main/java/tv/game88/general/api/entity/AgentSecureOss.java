package tv.game88.general.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

/**
 * 代理域名oss对象 agent_secure_oss
 *
 * @author 77tv
 * @date 2021-04-05
 */
@Data
public class AgentSecureOss extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId( type = IdType.AUTO )
    private Long id;

    /**
     * 状态(1启用0停用)
     */
    @Excel( name = "状态(1启用0停用)" )
    private Long status;

    /**
     * 代理号
     */
    @Excel( name = "代理号" )
    private String agent;

    /**
     * 名称
     */
    @Excel( name = "名称" )
    private String name;

    /**
     * Access Key ID
     */
    @Excel( name = "Access Key ID" )
    private String accessKey;

    /**
     * Access Key Secret
     */
    @Excel( name = "Access Key Secret" )
    private String accessSecret;

    /**
     * OSS Endpoint
     */
    @Excel( name = "OSS Endpoint" )
    private String endpoint;

    /**
     * 文件存储
     */
    @Excel( name = "文件存储" )
    private String bucket;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE ).append( "id", getId() ).append( "status", getStatus() )
                                                                          .append( "agent", getAgent() )
                                                                          .append( "name", getName() )
                                                                          .append( "accessKey", getAccessKey() )
                                                                          .append( "accessSecret", getAccessSecret() )
                                                                          .append( "endpoint", getEndpoint() )
                                                                          .append( "bucket", getBucket() )
                                                                          .append( "updateTime", getUpdateTime() )
                                                                          .append( "updateBy", getUpdateBy() )
                                                                          .append( "createBy", getCreateBy() )
                                                                          .append( "createTime", getCreateTime() ).toString();
    }
}
