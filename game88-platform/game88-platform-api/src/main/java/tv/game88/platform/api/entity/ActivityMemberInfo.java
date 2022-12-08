package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员推广管理对象 member_info
 *
 * @author 77tv
 * @date 2021-03-19
 */
@Data
public class ActivityMemberInfo {

    /**
     * 会员ID
     */
    @Excel( name = "会员ID", width = 30 )
    private String        id;
    @Excel( name = "会员vip" )
    private Integer       vip;
    @Excel( name = "手机" )
    private String        phone;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "注册时间" )
    private LocalDateTime registerTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "登录时间" )
    private LocalDateTime loginTime;
    @Excel( name = "登录ip" )
    private String        loginIp;
    @Excel( name = "邀请码" )
    private String        inviterCode;

    private String[] selectDate;
    private Boolean  isTwoPw;
}
