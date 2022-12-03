package tv.game88.core.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.vo.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 对象 config_oss
 *
 * @author MengJun
 */
@Data
@EqualsAndHashCode( callSuper = true )
public class ConfigOss extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId( value = "id", type = IdType.AUTO )
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * Access Key ID
     */
    private String accessKey;

    /**
     * Access Key Secret
     */
    private String accessSecret;

    /**
     * OSS Endpoint
     */
    private String endpoint;
    /**
     * 地区
     */
    private String region;
    /**
     * 文件存储
     */
    private String bucket;

    /**
     * 上传域名
     */
    private String vhost;

    /**
     * 状态
     */
    private Boolean effect;

    /**
     * 0 阿里 1亚马逊 2快快云
     */
    private Integer provider;

    /**
     * 访问域名
     */
    private String doMain;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    private LocalDateTime createTime;
    /**
     * 更新者
     */
    private String        updateBy;
    /**
     * 创建者
     */
    private String        createBy;
}