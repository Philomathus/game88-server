package tv.game88.core.admin.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户对象 sys_user
 *
 * @author MengJun
 */
@TableName("sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 0;

    /**
     * 用户ID
     */
    @Excel(name = "用户序号")
    @TableId(value = "user_id" , type = IdType.AUTO)
    private Long userId;

    /**
     * 用户账号
     */
    @Excel(name = "登录名称")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    private String userName;

    /**
     * 用户昵称
     */
    @Excel(name = "用户名称")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别")
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 盐加密
     */
    private String salt;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Excel(name = "帐号状态")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel(name = "最后登录时间" , width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    private String otpSecret;

    /**
     * 角色对象
     */
    private List<SysRole> roles;

    /**
     * 角色组
     */
    private Long[] roleIds;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    public SysUser() {
    }

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId" , getUserId())
                .append("userName" , getUserName())
                .append("nickName" , getNickName())
                .append("sex" , getSex())
                .append("avatar" , getAvatar())
                .append("password" , getPassword())
                .append("salt" , getSalt())
                .append("status" , getStatus())
                .append("delFlag" , getDelFlag())
                .append("loginIp" , getLoginIp())
                .append("loginDate" , getLoginDate())
                .append("createBy" , getCreateBy())
                .append("createTime" , getCreateTime())
                .append("updateBy" , getUpdateBy())
                .append("updateTime" , getUpdateTime())
                .append("remark" , getRemark())
                .toString();
    }
}
