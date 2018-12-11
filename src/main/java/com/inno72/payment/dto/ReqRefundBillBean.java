package com.inno72.payment.dto;

public class ReqRefundBillBean {
	
	private String spId;
	
	private String outTradeNo;
	
	private String outRefundNo;
	
	private long amount;
	
	private String notifyUrl;
	
	private String reason;
	
	private String sign;

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getOutRefundNo() {
		return outRefundNo;
	}

	public void setOutRefundNo(String outRefundNo) {
		this.outRefundNo = outRefundNo;
	}

	@Override
	public String toString() {
		return "ReqRefundBillBean [spId=" + spId + ", outTradeNo=" + outTradeNo + ", outRefundNo=" + outRefundNo
				+ ", amount=" + amount + ", notifyUrl=" + notifyUrl + ", reason=" + reason + ", sign=" + sign + "]";
	}

	
	
	
	
}
