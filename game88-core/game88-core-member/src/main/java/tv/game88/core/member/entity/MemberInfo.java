package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since 2021-09-29
 */
@Data
@EqualsAndHashCode( callSuper = false )
@ApiModel( value = "MemberInfo对象" )
public class MemberInfo implements Serializable {

    @ApiModelProperty( value = "系统编号" )
    @Excel( name = "会员编号" )
    @TableId( value = "id", type = IdType.INPUT )
    private String id;

    @ApiModelProperty( value = "登录账号" )
    @Excel( name = "登录账号" )
    private String userName;

    @ApiModelProperty( value = "登录密码" )
    @Excel( name = "登录密码" )
    private String password;

    @ApiModelProperty( value = "昵称" )
    @Excel( name = "昵称" )
    private String nickName;

    @ApiModelProperty( value = "0禁用 1正常 2测试号 3超管号 4套利号 5稀有号 6投诉号 7审查号" )
    @Excel( name = "状态" )
    private Integer status;

    @ApiModelProperty( value = "会员vip" )
    @Excel( name = "会员vip" )
    private Integer vip;

    @ApiModelProperty( value = "提现密码" )
    private String withdrawalPass;

    @ApiModelProperty( value = "当前可用余额" )
    @Excel( name = "当前可用余额" )
    private BigDecimal accountNow;

    @ApiModelProperty( value = "累计充值金额" )
    @Excel( name = "累计充值金额" )
    private BigDecimal accountCharge;

    @ApiModelProperty( value = "累计有效打码" )
    @Excel( name = "累计有效打码" )
    private BigDecimal codeNow;

    @ApiModelProperty( value = "累计打码" )
    @Excel( name = "累计打码" )
    private BigDecimal codeTotal;

    @ApiModelProperty( value = "累计需求打码（充值+优惠）" )
    @Excel( name = "累计需求打码" )
    private BigDecimal codeWill;

    @ApiModelProperty( value = "升级还需打码量" )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;

    @ApiModelProperty( value = "手机" )
    @Excel( name = "手机" )
    private String phone;

    @ApiModelProperty( value = "邀请码" )
    @Excel( name = "邀请码" )
    private String inviterCode;

    @ApiModelProperty( value = "佣金" )
    @Excel( name = "佣金" )
    private BigDecimal inviteMoney;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @ApiModelProperty( value = "注册时间" )
    @Excel( name = "注册时间" )
    private LocalDateTime registerTime;

    @ApiModelProperty( value = "注册ip" )
    @Excel( name = "注册ip" )
    private String registerIp;

    @ApiModelProperty( value = "登录ip" )
    @Excel( name = "登录ip" )
    private String loginIp;

    @ApiModelProperty( value = "登录设备(1 ios 2 android)" )
    @Excel( name = "登录设备" )
    private Integer loginDev;

    @ApiModelProperty( value = "登录省份" )
    @Excel( name = "登录省份" )
    private String loginProvince;

    @ApiModelProperty( value = "头像" )
    private String headImg;

    @ApiModelProperty( value = "在线时长" )
    @Excel( name = "在线时长" )
    private Integer onlineTime;

    @ApiModelProperty( value = "登录域名" )
    @Excel( name = "登录域名" )
    private String linkUrl;

    @ApiModelProperty( value = "登录时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "登录时间" )
    private LocalDateTime loginTime;

    @ApiModelProperty( value = "保险箱余额" )
    @Excel( name = "保险箱余额" )
    private BigDecimal boxAccount;

    @ApiModelProperty( value = "保险箱密码" )
    private String boxPass;

    @ApiModelProperty( value = "登陆次数" )
    @Excel( name = "登陆次数" )
    private Integer loginNum;

    @ApiModelProperty( value = "客户端版本号" )
    @Excel( name = "客户端版本号" )
    private String version;

    @ApiModelProperty( value = "手机型号" )
    @Excel( name = "手机型号" )
    private String phoneModel;

    @ApiModelProperty( value = "设备ID" )
    @Excel( name = "设备ID" )
    private String deviceId;

    @ApiModelProperty( value = "0游客 1会员" )
    @Excel( name = "注册类型" )
    private Integer registerType;

    @ApiModelProperty( value = "备注" )
    @Excel( name = "备注" )
    private String remark;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @ApiModelProperty( value = "洗码时间" )
    @Excel( name = "洗码时间" )
    private LocalDateTime cleanTime;
}
