package com.inno72.payment.utils.wechat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RspWechatBillBean extends DefaultHandler {

	private String returnCode="";
	
	private String returnMsg="";
	
	private String resultCode="";
	
	private String errCode="";
	
	private String errCodeDes="";
	
	private String prepayId="";
	
	private String codeUrl="";
	
	private String mwebUrl="";
	
	final private int returnCodeFlag=1;
	
	final private int returnMsgFlag=2;
	
	final private int resultCodeFlag=3;
	
	final private int errCodeFlag=4;
	
	final private int errCodeDesFlag=5;
	
	final private int prepayIdFlag=6;
	
	final private int codeUrlFlag=7;
	
	final private int mwebUrlFlag=8;
	
	private int currentFlag=0;
	
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
			case prepayIdFlag:
				prepayId += new String(arg0, arg1, arg2); break;
			case codeUrlFlag:
				codeUrl += new String(arg0, arg1, arg2); break;
			case mwebUrlFlag:
				mwebUrl += new String(arg0, arg1, arg2); break;
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
			case "prepay_id":
				currentFlag = prepayIdFlag; break;
			case "code_url":
				currentFlag = codeUrlFlag; break;
			case "mweb_url":
				currentFlag = mwebUrlFlag; break;
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

	public String getReturnMsg() {
		return returnMsg;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getErrCode() {
		return errCode;
	}

	public String getErrCodeDes() {
		return errCodeDes;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public String getCodeUrl() {
		return codeUrl;
	}

	public String getMwebUrl() {
		return mwebUrl;
	}

	@Override
	public String toString() {
		return "RspWechatBillBean [returnCode=" + returnCode + ", returnMsg=" + returnMsg + ", resultCode=" + resultCode
				+ ", errCode=" + errCode + ", errCodeDes=" + errCodeDes + ", prepayId=" + prepayId + ", codeUrl="
				+ codeUrl + ", mwebUrl=" + mwebUrl + "]";
	}

	
	
}
