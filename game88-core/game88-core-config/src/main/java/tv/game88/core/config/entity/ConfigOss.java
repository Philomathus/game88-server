package tv.game88.core.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

import java.io.Serial;

/**
 * 对象 config_oss
 *
 * @author MengJun
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigOss extends BaseEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(value = "id",type = IdType.AUTO)
	private Long id;

	/** 名称 */
	private String name;

	/** Access Key ID */
	private String accessKey;

	/** Access Key Secret */
	private String accessSecret;

	/** OSS Endpoint */
	private String endpoint;

	/** 文件存储 */
	private String bucket;

	/** 上传域名 */
	private String vhost;

	/** 状态 */
	private Integer isEffect;

	/** 0 阿里 1亚马逊 2快快云 */
	private Long provider;

	/** 访问域名 */
	private String doMain;

	@Override
	public String toString() {
		return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
				.append("id", getId())
				.append("name", getName())
				.append("accessKey", getAccessKey())
				.append("accessSecret", getAccessSecret())
				.append("endpoint", getEndpoint())
				.append("bucket", getBucket())
				.append("vhost", getVhost())
				.append("isEffect", getIsEffect())
				.append("provider", getProvider())
				.append("doMain", getDoMain())
				.append("createBy", getCreateBy())
				.append("createTime", getCreateTime())
				.append("updateBy", getUpdateBy())
				.append("updateTime", getUpdateTime())
				.append("remark" , getRemark())
				.toString();
	}
}