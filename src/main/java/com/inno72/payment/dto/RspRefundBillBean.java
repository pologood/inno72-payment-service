package com.inno72.payment.dto;

public class RspRefundBillBean {
	
	private String spId;
	
	private String billId;
	
	private String outTradeNo;
	
	private String outRefundNo;
	
	private String refundId;
	
	private Integer status;
	
	private Long refundFee;

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getBillId() {
		return billId;
	}

	public void setBillId(String billId) {
		this.billId = billId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getRefundFee() {
		return refundFee;
	}

	public void setRefundFee(Long refundFee) {
		this.refundFee = refundFee;
	}

	public String getOutRefundNo() {
		return outRefundNo;
	}

	public void setOutRefundNo(String outRefundNo) {
		this.outRefundNo = outRefundNo;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	@Override
	public String toString() {
		return "RspRefundBillBean [spId=" + spId + ", billId=" + billId + ", outTradeNo=" + outTradeNo
				+ ", outRefundNo=" + outRefundNo + ", refundId=" + refundId + ", status=" + status + ", refundFee="
				+ refundFee + "]";
	}

	

	
	
	
	
}
