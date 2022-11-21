package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Data
@EqualsAndHashCode( callSuper = false )
@Schema( title = "MemberInfoHistory对象" )
public class MemberInfoHistory implements Serializable {


    @Schema( title = "会员编号" )
    @Excel( name = "会员编号" )
    @TableId( value = "id", type = IdType.INPUT )
    private String id;

    @Schema( title = "登录密码" )
    @Excel( name = "登录密码" )
    private String password;

    @Schema( title = "昵称" )
    @Excel( name = "昵称" )
    private String nickName;

    @Schema( title = "状态", description = "0禁用 1正常 2测试号 3超管号 4套利号 5稀有号 6投诉号 7审查号" )
    @Excel( name = "状态" )
    private Integer status;

    @Schema( title = "会员vip" )
    @Excel( name = "会员vip" )
    private Integer vip;

    @Schema( title = "提现密码" )
    private String withdrawalPass;

    @Schema( title = "当前可用余额" )
    @Excel( name = "当前可用余额" )
    private BigDecimal accountNow;

    @Schema( title = "累计充值金额" )
    @Excel( name = "累计充值金额" )
    private BigDecimal accountCharge;

    @Schema( title = "累计有效打码" )
    @Excel( name = "累计有效打码" )
    private BigDecimal codeNow;

    @Schema( title = "累计打码" )
    @Excel( name = "累计打码" )
    private BigDecimal codeTotal;

    @Schema( title = "累计需求打码（充值+优惠）" )
    @Excel( name = "累计需求打码" )
    private BigDecimal codeWill;

    @Schema( title = "升级还需打码量" )
    @TableField( exist = false )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;

    @Schema( title = "手机" )
    @Excel( name = "手机" )
    private String phone;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Schema( title = "注册时间" )
    @Excel( name = "注册时间" )
    private LocalDateTime registerTime;

    @Schema( title = "注册ip" )
    @Excel( name = "注册ip" )
    private String registerIp;

    @Schema( title = "登录ip" )
    @Excel( name = "登录ip" )
    private String loginIp;

    @Schema( title = "登录设备", description = "1 ios 2 android" )
    @Excel( name = "登录设备" )
    private Integer loginDev;

    @Schema( title = "登录省份" )
    @Excel( name = "登录省份" )
    private String loginProvince;

    @Schema( title = "头像" )
    private String headImg;

    @Schema( title = "登录域名" )
    @Excel( name = "登录域名" )
    private String linkUrl;

    @Schema( title = "登录时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "登录时间" )
    private LocalDateTime loginTime;

    @Schema( title = "保险箱余额" )
    @Excel( name = "保险箱余额" )
    private BigDecimal boxAccount;

    @Schema( title = "保险箱密码" )
    private String boxPass;

    @Schema( title = "登陆次数" )
    @Excel( name = "登陆次数" )
    private Integer loginNum;

    @Schema( title = "客户端版本号" )
    @Excel( name = "客户端版本号" )
    private String version;

    @Schema( title = "手机型号" )
    @Excel( name = "手机型号" )
    private String phoneModel;

    @Schema( title = "设备ID" )
    @Excel( name = "设备ID" )
    private String deviceId;

    @Schema( title = "注册类型", description = "0游客 1会员" )
    @Excel( name = "注册类型" )
    private Integer registerType;

    @Schema( title = "备注" )
    @Excel( name = "备注" )
    private String remark;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Schema( title = "洗码时间" )
    @Excel( name = "洗码时间" )
    private LocalDateTime cleanTime;

    @Schema( title = "邀请码" )
    @Excel( name = "邀请码" )
    private String inviterCode;

    @Schema( title = "佣金" )
    @Excel( name = "佣金" )
    private String inviterMoney;

    @Schema( title = "渠道号" )
    @Excel( name = "渠道号" )
    private String channelCode;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String searchValue;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<String> searchValues;

    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime selectStartDate;

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

    public Set<String> getSearchValues() {
        if ( StringUtils.isNotBlank( searchValue ) ) {
            String[]    strings        = searchValue.split( "," );
            Set<String> searchValueSet = new HashSet<>();
            for ( String s : strings ) {
                if ( StringUtils.isNotBlank( s ) ) {
                    searchValueSet.add( s.trim() );
                }
            }
            return searchValueSet;
        }
        return searchValues;
    }

}
