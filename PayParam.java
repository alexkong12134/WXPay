package com.flyer.nc.wxpay;

import java.io.Serializable;

/**
 * 小程序支付
 * 
 * @author lhy
 * 
 */

public class PayParam implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1712467669291115101L;
	private String timeStamp;// 时间戳
	private String nonceStr;// 32位随机字符串
	private String packAge;// 预支付id
	private String paySign;// 签名

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	public String getPackAge() {
		return packAge;
	}

	public void setPackAge(String packAge) {
		this.packAge = packAge;
	}

	public String getPaySign() {
		return paySign;
	}

	public void setPaySign(String paySign) {
		this.paySign = paySign;
	}

	@Override
	public String toString() {
		return "PayParam [timeStamp=" + timeStamp + ", nonceStr=" + nonceStr + ", packAge=" + packAge + ", paySign="
				+ paySign + "]";
	}

}
