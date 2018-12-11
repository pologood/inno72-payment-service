package com.inno72.payment.dto;

public class RspQueryBillBean {
	
	private String id;

	private String spId;

	private int terminalType;

	private int type;

	private int status;

	private String outTradeNo;

	private String subject;

	private String sellerId;

	private String buyerId;

	private int isRefund;

	private int notifyStatus;

	private long totalFee;

	private long refundAmount;

	private long price;

	private int quantity;

	private String notifyUrl;

	private String returnUrl;

	private String showUrl;

	private String notifyId;

	private String notifyTime;

	private String createTime;

	private String updateTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public int getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(int terminalType) {
		this.terminalType = terminalType;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}

	public int getIsRefund() {
		return isRefund;
	}

	public void setIsRefund(int isRefund) {
		this.isRefund = isRefund;
	}

	public int getNotifyStatus() {
		return notifyStatus;
	}

	public void setNotifyStatus(int notifyStatus) {
		this.notifyStatus = notifyStatus;
	}

	public long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(long totalFee) {
		this.totalFee = totalFee;
	}

	public long getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(long refundAmount) {
		this.refundAmount = refundAmount;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public String getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(String notifyTime) {
		this.notifyTime = notifyTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "RspQueryBillBean [id=" + id + ", spId=" + spId + ", terminalType=" + terminalType + ", type=" + type
				+ ", status=" + status + ", outTradeNo=" + outTradeNo + ", subject=" + subject + ", sellerId="
				+ sellerId + ", buyerId=" + buyerId + ", isRefund=" + isRefund + ", notifyStatus=" + notifyStatus
				+ ", totalFee=" + totalFee + ", refundAmount=" + refundAmount + ", price=" + price + ", quantity="
				+ quantity + ", notifyUrl=" + notifyUrl + ", returnUrl=" + returnUrl + ", showUrl=" + showUrl
				+ ", notifyId=" + notifyId + ", notifyTime=" + notifyTime + ", createTime=" + createTime
				+ ", updateTime=" + updateTime + "]";
	}

	
	
	
	
	
}
