package tv.game88.core.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author MengJun
 */
@Data
@TableName( "sys_user_role" )
public class SysUserRole {
	/**
	 * 用户ID
	 */
	@TableId(value = "user_id", type = IdType.INPUT)
	private Long userId;

	/**
	 * 角色ID
	 */
	private Long roleId;

	@Override
	public String toString() {
		return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
				.append( "userId", getUserId() )
				.append( "roleId", getRoleId() )
				.toString();
	}
}
