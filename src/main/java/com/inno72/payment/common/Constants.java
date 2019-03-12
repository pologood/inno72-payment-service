package com.inno72.payment.common;

public class Constants {

	public static final String APP_CONTEXT 		= "APP_CONTEXT"; 
	
	public static final String JSON_REQ_METHOD = "method";
	public static final String JSON_REQ_CONTENT = "content";

	public static final int RSP_RET_OK 		= 0; 
	public static final int RSP_RET_FALSE 	= 1;
	public static final String RSP_MSG_OK 		= "ok"; 
	
	public static final String NOTIFY_RET_CODE_OK 		= "00"; 
	
	public static final int TRANSTYPE_DIRECT = 1;
	public static final int TRANSTYPE_PRE = 2;
	public static final int TRANSTYPE_PRE_COMPLETE = 3;
	
	
	public static final int PAYSTATUS_TRADE_PREPARE  = 0;
	public static final int PAYSTATUS_TRADE_CLOSED  = 1;
	public static final int PAYSTATUS_TRADE_SUCCESS  = 2;
	public static final int PAYSTATUS_TRADE_FINISHED  = 3;
	public static final int PAYSTATUS_TRADE_ERROR = 4;

	public static final int REFUNDSTATUS_WAIT = 10;
	public static final int REFUNDSTATUS_APPLY = 11;
	public static final int REFUNDSTATUS_SUCCESS = 12;
	public static final int REFUNDSTATUS_ERROR = 13;
	
	
	public static final int COMMON_STATUS_NO = 0;
	public static final int COMMON_STATUS_YES = 1;

		
	public static final int PAY_CHANNEL_ALIPAY = 1;
	public static final int PAY_CHANNEL_WECHAT = 2;
	public static final int PAY_CHANNEL_ALIPAY_SCAN = 3; // 蚂蚁金服当面付 扫码支付

	public static final int SOURCE_FLAG_IGNORE_PLATFORM = 0;
	public static final int SOURCE_FLAG_QRCODE = 1;
	public static final int SOURCE_FLAG_WAP = 2;
	public static final int SOURCE_FLAG_APP = 3;
	public static final int SOURCE_FLAG_MINIAPP = 4;
	public static final int SOURCE_FLAG_PUBLICAPP = 5;
	
	public static final String SERVICE_CHARSET = "utf-8";
	
	public static final String SECURE_KEY = "MhxzKhl";
}
