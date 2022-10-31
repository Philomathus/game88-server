package tv.game88.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity基类
 *
 * @author MengJun
 */
@Data
@EqualsAndHashCode( callSuper = false )
public class BaseEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 搜索值
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(exist = false)
	private String searchValue;

	/**
	 * 创建者
	 */
	private String createBy;

	/**
	 * 创建时间
	 */
	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	private LocalDateTime createTime;

	/**
	 * 更新者
	 */
	private String updateBy;

	/**
	 * 更新时间
	 */
	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	private LocalDateTime updateTime;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 请求参数
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(exist = false)
	private Map<String, Object> params;

	public Map<String, Object> getParams() {
		if ( params == null ) {
			params = new HashMap<>();
		}
		return params;
	}
}
