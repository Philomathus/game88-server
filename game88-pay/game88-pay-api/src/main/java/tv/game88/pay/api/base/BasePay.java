package tv.game88.pay.api.base;

import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.util.Map;

public interface BasePay {

	String getName();

	/**
	 * 支付下单
	 *
	 * @param payChannel  支付渠道表
	 * @param payPlatform 支付平台表
	 * @param reqPayRecharge 下单数据，包括渠道ID和下单金额
	 * @return 获取的支付URL
	 */
	String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge );

	/**
	 * 支付查询
	 *
	 * @param  memberRechargeOnline       线上支付记录表
	 * @param payPlatform 支付平台表
	 * @param payChannel  支付渠道表
	 * @return 支付状态是否成功
	 */
	boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel );

	/**
	 * 支付回调
	 *
	 * @param requestMap 第三方回调过来的数据
	 * @param realIp     第三方回调IP
	 * @return 是否成功文本
	 */
	String callbackPay( Map<String, Object> requestMap, String realIp );
}
