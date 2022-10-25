package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema( name = "MemberInfo对象" )
public class MemberInfo implements Serializable {

    @Schema( name = "系统编号" )
    @Excel( name = "会员编号" )
    @TableId( value = "id", type = IdType.INPUT )
    private String id;

    @Schema( name = "登录账号" )
    @Excel( name = "登录账号" )
    private String userName;

    @Schema( name = "登录密码" )
    @Excel( name = "登录密码" )
    private String password;

    @Schema( name = "昵称" )
    @Excel( name = "昵称" )
    private String nickName;

    @Schema( name = "状态", description = "0禁用 1正常 2测试号 3超管号 4套利号 5稀有号 6投诉号 7审查号" )
    @Excel( name = "状态" )
    private Integer status;

    @Schema( name = "会员vip" )
    @Excel( name = "会员vip" )
    private Integer vip;

    @Schema( name = "提现密码" )
    private String withdrawalPass;

    @Schema( name = "当前可用余额" )
    @Excel( name = "当前可用余额" )
    private BigDecimal accountNow;

    @Schema( name = "累计充值金额" )
    @Excel( name = "累计充值金额" )
    private BigDecimal accountCharge;

    @Schema( name = "累计有效打码" )
    @Excel( name = "累计有效打码" )
    private BigDecimal codeNow;

    @Schema( name = "累计打码" )
    @Excel( name = "累计打码" )
    private BigDecimal codeTotal;

    @Schema( name = "累计需求打码（充值+优惠）" )
    @Excel( name = "累计需求打码" )
    private BigDecimal codeWill;

    @Schema( name = "升级还需打码量" )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;

    @Schema( name = "手机" )
    @Excel( name = "手机" )
    private String phone;

    @Schema( name = "邀请码" )
    @Excel( name = "邀请码" )
    private String inviterCode;

    @Schema( name = "佣金" )
    @Excel( name = "佣金" )
    private BigDecimal inviteMoney;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Schema( name = "注册时间" )
    @Excel( name = "注册时间" )
    private LocalDateTime registerTime;

    @Schema( name = "注册ip" )
    @Excel( name = "注册ip" )
    private String registerIp;

    @Schema( name = "登录ip" )
    @Excel( name = "登录ip" )
    private String loginIp;

    @Schema( name = "登录设备", description = "1 ios 2 android")
    @Excel( name = "登录设备" )
    private Integer loginDev;

    @Schema( name = "登录省份" )
    @Excel( name = "登录省份" )
    private String loginProvince;

    @Schema( name = "头像" )
    private String headImg;

    @Schema( name = "在线时长" )
    @Excel( name = "在线时长" )
    private Integer onlineTime;

    @Schema( name = "登录域名" )
    @Excel( name = "登录域名" )
    private String linkUrl;

    @Schema( name = "登录时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "登录时间" )
    private LocalDateTime loginTime;

    @Schema( name = "保险箱余额" )
    @Excel( name = "保险箱余额" )
    private BigDecimal boxAccount;

    @Schema( name = "保险箱密码" )
    private String boxPass;

    @Schema( name = "登陆次数" )
    @Excel( name = "登陆次数" )
    private Integer loginNum;

    @Schema( name = "客户端版本号" )
    @Excel( name = "客户端版本号" )
    private String version;

    @Schema( name = "手机型号" )
    @Excel( name = "手机型号" )
    private String phoneModel;

    @Schema( name = "设备ID" )
    @Excel( name = "设备ID" )
    private String deviceId;

    @Schema( name = "0游客 1会员" )
    @Excel( name = "注册类型" )
    private Integer registerType;

    @Schema( name = "备注" )
    @Excel( name = "备注" )
    private String remark;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Schema( name = "洗码时间" )
    @Excel( name = "洗码时间" )
    private LocalDateTime cleanTime;
}
