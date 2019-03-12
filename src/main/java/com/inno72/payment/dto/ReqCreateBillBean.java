package com.inno72.payment.dto;

public class ReqCreateBillBean {
	
	private String spId;
	
	private String subject;
	
	private String outTradeNo;
	
	private long totalFee;
	
	private String returnUrl;
	
	private String merchantUrl;
	
	private String notifyUrl;
	
	private int type;
	
	private int terminalType = 0;
	
	private String remark;
	
	private Integer transTimeout;
	
	private Integer qrTimeout;
	
	private long unitPrice;
	
	private int quantity;
	
	private String clientIp;
			
	private String extra;
	
	private String sign;

	// 机器code
	private String terminalId;

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(long totalFee) {
		this.totalFee = totalFee;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getMerchantUrl() {
		return merchantUrl;
	}

	public void setMerchantUrl(String merchantUrl) {
		this.merchantUrl = merchantUrl;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(int terminalType) {
		this.terminalType = terminalType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getTransTimeout() {
		return transTimeout;
	}

	public void setTransTimeout(Integer transTimeout) {
		this.transTimeout = transTimeout;
	}

	public Integer getQrTimeout() {
		return qrTimeout;
	}

	public void setQrTimeout(Integer qrTimeout) {
		this.qrTimeout = qrTimeout;
	}

	public long getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(long unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getSign() {
		return sign;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public String toString() {
		return "ReqCreateBillBean{" + "spId='" + spId + '\'' + ", subject='" + subject + '\'' + ", outTradeNo='"
				+ outTradeNo + '\'' + ", totalFee=" + totalFee + ", returnUrl='" + returnUrl + '\'' + ", merchantUrl='"
				+ merchantUrl + '\'' + ", notifyUrl='" + notifyUrl + '\'' + ", type=" + type + ", terminalType="
				+ terminalType + ", remark='" + remark + '\'' + ", transTimeout=" + transTimeout + ", qrTimeout="
				+ qrTimeout + ", unitPrice=" + unitPrice + ", quantity=" + quantity + ", clientIp='" + clientIp + '\''
				+ ", extra='" + extra + '\'' + ", sign='" + sign + '\'' + ", terminalId='" + terminalId + '\'' + '}';
	}
}
