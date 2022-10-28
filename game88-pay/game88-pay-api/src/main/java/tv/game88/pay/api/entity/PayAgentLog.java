package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 代付信息日志对象 pay_agent_log
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class PayAgentLog {
	private static final long serialVersionUID = 1L;

	/** 主键 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/** 提现订单号 */
	@Excel( name = "提现订单号" )
	private String withdrawOrderNo;

	/** 三方代付订单号 */
	@Excel( name = "三方代付订单号" )
	private String payAgentOrderNo;

	/** 三方代付平台ID */
	private Long payAgentPlatId;

	/** 三方代付平台名称 */
	@Excel( name = "三方代付平台名称" )
	private String payAgentPlatName;

	/** 会员ID */
	@Excel( name = "会员ID" )
	private String memberId;

	/** 提现金额 */
	@Excel( name = "提现金额" )
	private BigDecimal withdrawMoney;

	@JsonFormat( pattern = "yyyy-MM-dd" )
	@Excel( name = "提交时间", width = 30, isImportField = "true", exportFormat = "yyyy-MM-dd",
			importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd" )
	private Date createTime;

	/** 回调时间 */
	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	@Excel( name = "回调时间", width = 30, isImportField = "true", exportFormat = "yyyy-MM-dd",
			importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd" )
	private Date callbackTime;

	/** 回调状态 0 回调中 1 成功 2失败 */
	@Excel( name = "回调状态" )
	private Integer callbackStatus;

	@Excel( name = "备注" )
	private String remark;

	/**
	 * 请求参数
	 */
	@JsonIgnore
	private Map<String, Object> params = new HashMap<>();

	@Override
	public String toString() {
		return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
				.append( "id", getId() )
				.append( "withdrawOrderNo", getWithdrawOrderNo() )
				.append( "payAgentOrderNo", getPayAgentOrderNo() )
				.append( "payAgentPlatId", getPayAgentPlatId() )
				.append( "payAgentPlatName", getPayAgentPlatName() )
				.append( "memberId", getMemberId() )
				.append( "withdrawMoney", getWithdrawMoney() )
				.append( "createTime", getCreateTime() )
				.append( "callbackTime", getCallbackTime() )
				.append( "callbackStatus", getCallbackStatus() )
				.toString();
	}
}
