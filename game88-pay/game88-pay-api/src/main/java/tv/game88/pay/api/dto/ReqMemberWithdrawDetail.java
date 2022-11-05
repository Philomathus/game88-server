package tv.game88.pay.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReqMemberWithdrawDetail {

	private String[] downLoadDate = new String[2];
	private String       id;
	private List<String> ids;
	private String       memberId;
	private String       remark;
	private String       searchCardBlack;
	private String       cardBlack;
	private Long         payAgentChannelId;
	private Integer      status;
	private String       opName;
	private String       searchValue;

	private String SearchCardBlack;
	private String province;
	private String city;
	private Integer times;
	private BigDecimal money;
	private String statusName;
	//公司入款姓名与提现姓名状态
	private Integer rechargeUserNameStatus;
	private String currencyCode;

	@JsonIgnore
	private String priceMin;
	@JsonIgnore
	private String priceMax;
	@JsonIgnore
	private String[] searchTime;
	@JsonIgnore
	private String startTime;
	@JsonIgnore
	private String endTime;

	public String getStartTime() {
		if (searchTime != null && searchTime.length > 0) {
			return searchTime[0];
		}
		return null;
	}

	public String getEndTime() {
		if (searchTime != null && searchTime.length > 0) {
			return searchTime[1];
		}
		return null;
	}
}
