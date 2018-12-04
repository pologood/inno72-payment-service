package com.inno72.payment.service;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inno72.common.Result;
import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfo;
import com.inno72.payment.utils.HttpFormConnector;
import com.inno72.payment.utils.Utility;
import com.inno72.payment.utils.wechat.RspWechatBillBean;
import com.inno72.payment.utils.wechat.WechatCore;

@Service("channelWechatService")
public class ChannelWechatService  extends ChannelBaseService {
	
	private static final String SERVER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ApplicationContext applicationContext;

	private void checkTerminalType(int terminalType) throws TransException {

		switch (terminalType) {
			case Constants.SOURCE_FLAG_QRCODE:
				return;
			default:
				logger.warn("terminalType do not support:" + terminalType);
				throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
		}

	}
	
	private String getNotifyUrl() throws TransException {
		
		String state = applicationContext.getEnvironment().getActiveProfiles()[0];
		switch(state) {
			
			case "dev": 	return "http://pay.72solo.com/notify/wechat/native/%s";
			case "test": 	return "http://pay.36solo.com/notify/wechat/native/%s";
			case "stage": 	return "http://pay.32solo.com/notify/wechat/native/%s";
			case "prod": 	return "http://pay.inno72.com/notify/wechat/native/%s";
			default:
				logger.error("not found profile:" + state);
				throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
			
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<RspCreateBillBean> createBill(long billId, String remoteIp, PaySpInfoDaoBean spInfo, ThirdPartnerInfo thirdPartnerInfo,
			ReqCreateBillBean reqBean) throws TransException {
		
		
		checkTerminalType(reqBean.getTerminalType());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Map<String, String> params = new HashMap<String, String>();
		
		long currentTime = System.currentTimeMillis();
		
		params.put("appid", thirdPartnerInfo.getAppId());
		params.put("mch_id", thirdPartnerInfo.getMid());
		params.put("nonce_str", Utility.getUUID());
		params.put("body", reqBean.getSubject());
		if(StringUtils.isNoneBlank(reqBean.getRemark())) {
			params.put("detail", reqBean.getRemark());
		}
		params.put("out_trade_no", Long.toString(billId));
		params.put("fee_type", "CNY");
		params.put("total_fee", Long.toString(reqBean.getTotalFee()));
		
		params.put("spbill_create_ip", reqBean.getClientIp());
		
		if(reqBean.getTransTimeout() != null) {
			params.put("time_start", dateFormat.format(new Date(currentTime)));
			params.put("time_expire", dateFormat.format(new Date(currentTime + reqBean.getTransTimeout() * 60 * 1000)));
		}
		
		params.put("notify_url", String.format(getNotifyUrl(), spInfo.getId()));
		
		if(Constants.SOURCE_FLAG_QRCODE == reqBean.getTerminalType()) {
			params.put("trade_type", "NATIVE");
		}
		
		String sign = Utility.createLinkString(params);
		sign += "&key=" + thirdPartnerInfo.getSecureKey();
		try {
			params.put("sign", Utility.GetMD5Code(sign.getBytes(Constants.SERVICE_CHARSET)).toUpperCase());
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
		}

		String reqXml = WechatCore.makeXml(params);
		
		try {
			logger.info("connect to wechat:" + reqXml);
			byte[] ret = HttpFormConnector.doPost(SERVER_URL, reqXml.getBytes(Constants.SERVICE_CHARSET),
					"application/xml", 3000);
			String retWechat = new String(ret, Constants.SERVICE_CHARSET);
			
			logger.info("wechat return:" + retWechat);

			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			RspWechatBillBean rspWechatBillBean = new RspWechatBillBean();
			parser.parse(new ByteArrayInputStream(retWechat.getBytes()), rspWechatBillBean);
			
			if (!"SUCCESS".equalsIgnoreCase(rspWechatBillBean.getResultCode())
					|| !"SUCCESS".equalsIgnoreCase(rspWechatBillBean.getReturnCode())) {
				logger.warn("wechat bill error ResultCode:" + rspWechatBillBean.getResultCode() + " ReturnCode:"
						+ rspWechatBillBean.getReturnCode() + " ErrCode:" + rspWechatBillBean.getErrCode() + " ErrDes:"
						+ rspWechatBillBean.getErrCodeDes() + " ReturnMsg:" + rspWechatBillBean.getReturnMsg());
				throw new TransException(ErrorCode.ERR_CONNECT_WECHAT,
						Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
			}
			

			try {
				handleDbCreateBill(billId, spInfo, rspWechatBillBean.getPrepayId(), currentTime, remoteIp,
						reqBean);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TransException(ErrorCode.ERR_DB, Message.getMessage(ErrorCode.ERR_DB));
			}
			
			Result<RspCreateBillBean> rspBean = new Result<RspCreateBillBean>();
			RspCreateBillBean rspContent = new RspCreateBillBean();
			rspBean.setData(rspContent);
			rspBean.setCode(Constants.RSP_RET_OK);
			rspBean.setMsg(Constants.RSP_MSG_OK);
			rspBean.getData().setBillId(Long.toString(billId));
			rspBean.getData().setType(reqBean.getType());
			rspBean.getData().setTerminalType(reqBean.getTerminalType());
			rspBean.getData().setQrCodeUrl(rspWechatBillBean.getCodeUrl());
			return rspBean;
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		}
	}

}
