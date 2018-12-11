package com.inno72.payment.dto;

public class RspCreateBillBean {

	private String spId;
	
	private String outTradeNo;
	
	private Integer type;

	private Integer terminalType;

	private String billId;

	private String prepayId;
	
	private String qrCode;
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(Integer terminalType) {
		this.terminalType = terminalType;
	}

	public String getBillId() {
		return billId;
	}

	public void setBillId(String billId) {
		this.billId = billId;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

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

	@Override
	public String toString() {
		return "RspCreateBillBean [spId=" + spId + ", outTradeNo=" + outTradeNo + ", type=" + type + ", terminalType="
				+ terminalType + ", billId=" + billId + ", prepayId=" + prepayId + ", qrCode=" + qrCode + "]";
	}

	
	
}
