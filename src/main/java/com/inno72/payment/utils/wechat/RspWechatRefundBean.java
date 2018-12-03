package com.inno72.payment.utils.wechat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RspWechatRefundBean extends DefaultHandler {

	private String returnCode = "";

	private String returnMsg = "";

	private String resultCode = "";

	private String errCode = "";

	private String errCodeDes = "";

	private String refundId = "";

	private String refundChannel = "";

	final private int returnCodeFlag = 1;

	final private int returnMsgFlag = 2;

	final private int resultCodeFlag = 3;

	final private int errCodeFlag = 4;

	final private int errCodeDesFlag = 5;

	final private int refundIdFlag = 6;

	final private int refundChannelFlag = 7;

	private int currentFlag = 0;

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		switch(currentFlag){
			case returnCodeFlag:
				returnCode += new String(arg0, arg1, arg2); break;
			case returnMsgFlag:
				returnMsg += new String(arg0, arg1, arg2); break;
			case resultCodeFlag:
				resultCode += new String(arg0, arg1, arg2); break;
			case errCodeFlag:
				errCode += new String(arg0, arg1, arg2); break;
			case errCodeDesFlag:
				errCodeDes += new String(arg0, arg1, arg2); break;
			case refundIdFlag:
				refundId += new String(arg0, arg1, arg2); break;
			case refundChannelFlag:
				refundChannel += new String(arg0, arg1, arg2); break;
			default:
				return;
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		switch(qName){
			case "return_code":
				currentFlag = returnCodeFlag; break;
			case "return_msg":
				currentFlag = returnMsgFlag; break;
			case "result_code":
				currentFlag = resultCodeFlag; break;
			case "err_code":
				currentFlag = errCodeFlag; break;
			case "err_code_des":
				currentFlag = errCodeDesFlag; break;
			case "refund_id":
				currentFlag = refundIdFlag; break;
			case "refund_channel":
				currentFlag = refundChannelFlag; break;
			default:
				currentFlag = 0; 
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentFlag = 0;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrCodeDes() {
		return errCodeDes;
	}

	public void setErrCodeDes(String errCodeDes) {
		this.errCodeDes = errCodeDes;
	}

	public String getRefundId() {
		return refundId;
	}

	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}

	public String getRefundChannel() {
		return refundChannel;
	}

	public void setRefundChannel(String refundChannel) {
		this.refundChannel = refundChannel;
	}
	
	
	
}
