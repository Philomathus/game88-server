package tv.game88.core.admin.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

import java.io.Serial;

/**
 * 角色表 sys_role
 *
 * @author MengJun
 */
@EqualsAndHashCode( callSuper = true )
@Data
@TableName( "sys_role" )
public class SysRole extends BaseEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	@Excel( name = "角色序号" )
	@TableId(value = "role_id", type = IdType.AUTO)
	private Long roleId;

	/**
	 * 角色名称
	 */
	@Excel( name = "角色名称" )
	@NotBlank( message = "角色名称不能为空" )
	@Size( min = 0, max = 30, message = "角色名称长度不能超过30个字符" )
	private String roleName;

	/**
	 * 角色权限
	 */
	@Excel( name = "角色权限" )
	@NotBlank( message = "权限字符不能为空" )
	@Size( min = 0, max = 100, message = "权限字符长度不能超过100个字符" )
	private String roleKey;

	/**
	 * 角色排序
	 */
	@Excel( name = "角色排序" )
	@NotBlank( message = "显示顺序不能为空" )
	private String roleSort;

	/**
	 * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限）
	 */
	@Excel( name = "数据范围" )
	private String dataScope;

	/**
	 * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
	 */
	private boolean menuCheckStrictly;

	/**
	 * 角色状态（0正常 1停用）
	 */
	@Excel( name = "角色状态" )
	private String status;

	/**
	 * 删除标志（0代表存在 2代表删除）
	 */
	private String delFlag;

	/**
	 * 用户是否存在此角色标识 默认不存在
	 */
	private boolean flag = false;

	/**
	 * 菜单组
	 */
	private Long[] menuIds;

	public SysRole() {
	}

	public SysRole( Long roleId ) {
		this.roleId = roleId;
	}

	public static boolean isAdmin( Long roleId ) {
		return roleId != null && 1L == roleId;
	}

	public boolean isAdmin() {
		return isAdmin( this.roleId );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
				.append( "roleId", getRoleId() )
				.append( "roleName", getRoleName() )
				.append( "roleKey", getRoleKey() )
				.append( "roleSort", getRoleSort() )
				.append( "dataScope", getDataScope() )
				.append( "menuCheckStrictly", isMenuCheckStrictly() )
				.append( "status", getStatus() )
				.append( "delFlag", getDelFlag() )
				.append( "createBy", getCreateBy() )
				.append( "createTime", getCreateTime() )
				.append( "updateBy", getUpdateBy() )
				.append( "updateTime", getUpdateTime() )
				.append( "remark", getRemark() )
				.toString();
	}
}
