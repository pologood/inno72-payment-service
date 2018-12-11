package com.inno72.payment.dto;

public class ReqRefundNotifyBean {
	
	private String tradeNo;
	
	private int refundStatus;
	
	private String refundId;
	
	private long outRefundNo;
	
	private long totalFee;
	
	private long refundFee;
	
	private String clientIp;
		
	private String message;
	
	private long updateTime;

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public int getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(int refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	public long getOutRefundNo() {
		return outRefundNo;
	}

	public void setOutRefundNo(long outRefundNo) {
		this.outRefundNo = outRefundNo;
	}

	public long getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(long totalFee) {
		this.totalFee = totalFee;
	}

	public long getRefundFee() {
		return refundFee;
	}

	public void setRefundFee(long refundFee) {
		this.refundFee = refundFee;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "ReqRefundNotifyBean [tradeNo=" + tradeNo + ", refundStatus=" + refundStatus + ", refundId=" + refundId
				+ ", outRefundNo=" + outRefundNo + ", totalFee=" + totalFee + ", refundFee=" + refundFee + ", clientIp="
				+ clientIp + ", message=" + message + ", updateTime=" + updateTime + "]";
	}
	
	
	
	
}
