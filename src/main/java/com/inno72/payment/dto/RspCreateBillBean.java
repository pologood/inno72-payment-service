package com.inno72.payment.dto;

public class RspCreateBillBean {

	private Integer type;

	private Integer terminalType;

	private String billId;

	private String prepayId;
	
	private String qrCode;
	
	private String qrCodeUrl;

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

	public String getQrCodeUrl() {
		return qrCodeUrl;
	}

	public void setQrCodeUrl(String qrCodeUrl) {
		this.qrCodeUrl = qrCodeUrl;
	}

	@Override
	public String toString() {
		return "RspCreateBillBean [type=" + type + ", terminalType=" + terminalType + ", billId=" + billId
				+ ", prepayId=" + prepayId + ", qrCode=" + qrCode + ", qrCodeUrl=" + qrCodeUrl + "]";
	}
	
	

	
	
}
