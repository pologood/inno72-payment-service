package com.inno72.payment.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import com.inno72.common.Result;
import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqRefundBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.dto.RspRefundBillBean;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.PaymentLogDaoBean;
import com.inno72.payment.model.RefundInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;
import com.inno72.payment.utils.HttpFormConnector;
import com.inno72.payment.utils.Utility;
import com.inno72.payment.utils.wechat.WechatCore;
import com.inno72.payment.utils.wechat.WechatXmlParse;

@Service("channelWechatService")
public class ChannelWechatService extends ChannelBaseService {

	private static final String SERVER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	private static final String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

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
		switch (state) {

			case "dev":
				return "https://pay.72solo.com/notify/wechat/native/%s";
			case "test":
				return "https://pay.36solo.com/notify/wechat/native/%s";
			case "stage":
				return "https://pay.32solo.com/notify/wechat/native/%s";
			case "prod":
				return "https://pay.inno72.com/notify/wechat/native/%s";
			default:
				logger.error("not found profile:" + state);
				throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));

		}
	}


	private String getRefundNotifyUrl() throws TransException {

		String state = applicationContext.getEnvironment().getActiveProfiles()[0];
		switch (state) {

			case "dev":
				return "https://pay.72solo.com/notify/wechat/refund/%s/%d";
			case "test":
				return "https://pay.36solo.com/notify/wechat/refund/%s/%d";
			case "stage":
				return "https://pay.32solo.com/notify/wechat/refund/%s/%d";
			case "prod":
				return "https://pay.inno72.com/notify/wechat/refund/%s/%d";
			default:
				logger.error("not found profile:" + state);
				throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));

		}
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<RspCreateBillBean> createBill(long billId, String remoteIp, PaySpInfoDaoBean spInfo,
			ThirdPartnerInfoDaoBean thirdPartnerInfo, ReqCreateBillBean reqBean) throws TransException {


		checkTerminalType(reqBean.getTerminalType());

		checkPayRequest(reqBean);

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		Map<String, String> params = new HashMap<String, String>();

		long currentTime = System.currentTimeMillis();

		params.put("appid", thirdPartnerInfo.getAppId());
		params.put("mch_id", thirdPartnerInfo.getMid());
		params.put("nonce_str", Utility.getUUID());
		params.put("body", reqBean.getSubject());
		if (StringUtils.isNoneBlank(reqBean.getRemark())) {
			params.put("detail", reqBean.getRemark());
		}
		params.put("out_trade_no", Long.toString(billId));
		params.put("fee_type", "CNY");
		params.put("total_fee", Long.toString(reqBean.getTotalFee()));

		//params.put("spbill_create_ip", reqBean.getClientIp());

		if (reqBean.getTransTimeout() != null) {
			params.put("time_start", dateFormat.format(new Date(currentTime)));
			params.put("time_expire", dateFormat.format(new Date(currentTime + reqBean.getTransTimeout() * 60 * 1000)));
		}

		params.put("notify_url", String.format(getNotifyUrl(), spInfo.getId()));

		if (Constants.SOURCE_FLAG_QRCODE == reqBean.getTerminalType()) {
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
			
			Map<String, String> wechatRet = WechatXmlParse.parse(retWechat);
			
			if (!"SUCCESS".equalsIgnoreCase(wechatRet.get("result_code"))
					|| !"SUCCESS".equalsIgnoreCase(wechatRet.get("return_code"))) {
				logger.warn("wechat refund error ResultCode:" + wechatRet.get("result_code") + " ReturnCode:"
						+wechatRet.get("return_code") + " ErrCode:" + wechatRet.get("err_code")
						+ " ErrDes:" + wechatRet.get("err_code_des"));
				throw new TransException(ErrorCode.ERR_CONNECT_WECHAT,
						Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
			}


			try {
				handleDbCreateBill(billId, spInfo, wechatRet.get("prepay_id"), currentTime, remoteIp, reqBean);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TransException(ErrorCode.ERR_DB, Message.getMessage(ErrorCode.ERR_DB));
			}

			Result<RspCreateBillBean> rspBean = new Result<RspCreateBillBean>();
			RspCreateBillBean rspContent = new RspCreateBillBean();
			rspBean.setData(rspContent);
			rspBean.setCode(Constants.RSP_RET_OK);
			rspBean.setMsg(Constants.RSP_MSG_OK);
			rspBean.getData().setSpId(spInfo.getId());
			rspBean.getData().setOutTradeNo(reqBean.getOutTradeNo());
			rspBean.getData().setBillId(Long.toString(billId));
			rspBean.getData().setType(reqBean.getType());
			rspBean.getData().setTerminalType(reqBean.getTerminalType());
			rspBean.getData().setQrCode(wechatRet.get("code_url"));
			return rspBean;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<RspRefundBillBean> refundBill(long refundBillId, ReqRefundBillBean reqBean, BillInfoDaoBean billInfo, PaySpInfoDaoBean spInfo,
			ThirdPartnerInfoDaoBean thirdPartnerInfo, String remoteIp) throws TransException {

		try {
			
			checkRefundRequest(reqBean, billInfo);

			long currentTime = System.currentTimeMillis();

			if(payInfoDao.updatePayRefundInfo(billInfo.getId(), Constants.COMMON_STATUS_YES, 
					reqBean.getAmount(), currentTime, billInfo.getUpdateTime()) == 0) {
				logger.warn("refund data conflict billId:" + billInfo.getId());
				throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
			}
			
			Map<String, String> params = new HashMap<String, String>();

			params.put("appid", thirdPartnerInfo.getAppId());
			params.put("mch_id", thirdPartnerInfo.getMid());
			params.put("nonce_str", Utility.getUUID());
			params.put("transaction_id", billInfo.getTradeNo());
			params.put("out_trade_no", billInfo.getId().toString());
			params.put("out_refund_no", Long.toString(refundBillId));
			params.put("total_fee", Long.toString(billInfo.getTotalFee()));
			params.put("refund_fee", Long.toString(reqBean.getAmount()));
			params.put("refund_fee_type", "CNY");
			params.put("refund_desc", reqBean.getReason());
			params.put("notify_url", String.format(getRefundNotifyUrl(), spInfo.getId(), billInfo.getTerminalType()));
			params.put("refund_account", "REFUND_SOURCE_RECHARGE_FUNDS");

			String sign = Utility.createLinkString(params);
			sign += "&key=" + thirdPartnerInfo.getSecureKey();
			
			sign = Utility.GetMD5Code(sign.getBytes(Constants.SERVICE_CHARSET)).toUpperCase();
			params.put("sign", sign);

			String reqXml = WechatCore.makeXml(params);

			String ret = WechatCore.connectWechatWithSecret(REFUND_URL, reqXml, thirdPartnerInfo.getMid(),
					thirdPartnerInfo.getCertPath());

			Map<String, String> wechatRet = WechatXmlParse.parse(ret);
			
			if (!"SUCCESS".equalsIgnoreCase(wechatRet.get("result_code"))
					|| !"SUCCESS".equalsIgnoreCase(wechatRet.get("return_code"))) {
				logger.warn("wechat refund error ResultCode:" + wechatRet.get("result_code") + " ReturnCode:"
						+wechatRet.get("return_code") + " ErrCode:" + wechatRet.get("err_code")
						+ " ErrDes:" + wechatRet.get("err_code_des"));
				throw new TransException(ErrorCode.ERR_CONNECT_WECHAT,
						Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
			}
			
			PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
			logDaoBean.setBillId(refundBillId);
			logDaoBean.setBuyerId(billInfo.getBuyerId());
			logDaoBean.setIp(remoteIp);
			logDaoBean.setMessage(ret);
			logDaoBean.setOutTradeNo(reqBean.getOutRefundNo());
			logDaoBean.setSpId(reqBean.getSpId());
			logDaoBean.setIsRefund(Constants.COMMON_STATUS_YES);
			logDaoBean.setStatus(Constants.REFUNDSTATUS_APPLY);
			logDaoBean.setTotalFee(reqBean.getAmount());
			logDaoBean.setType(billInfo.getType());
			logDaoBean.setTerminalType(billInfo.getTerminalType());
			logDaoBean.setUpdateTime(currentTime);
			payInfoDao.insertPaymentLog(logDaoBean);
			
			
			RefundInfoDaoBean refundInfoDaoBean = new RefundInfoDaoBean();
			refundInfoDaoBean.setId(refundBillId);
			refundInfoDaoBean.setBillId(billInfo.getId());
			refundInfoDaoBean.setReason(reqBean.getReason());
			refundInfoDaoBean.setPayFee(billInfo.getTotalFee());
			refundInfoDaoBean.setNotifyUrl(reqBean.getNotifyUrl());
			refundInfoDaoBean.setRefundFee(reqBean.getAmount());
			refundInfoDaoBean.setSpId(billInfo.getSpId());
			refundInfoDaoBean.setOutTradeNo(billInfo.getOutTradeNo());
			refundInfoDaoBean.setOutRefundNo(reqBean.getOutRefundNo());
			refundInfoDaoBean.setRefundTradeNo(wechatRet.get("transaction_id"));
			refundInfoDaoBean.setTradeNo(billInfo.getTradeNo());
			refundInfoDaoBean.setMessage(ret);
			refundInfoDaoBean.setType(billInfo.getType());
			refundInfoDaoBean.setStatus(Constants.REFUNDSTATUS_APPLY);
			refundInfoDaoBean.setNotifyStatus(Constants.COMMON_STATUS_NO);
			refundInfoDaoBean.setCreateTime(currentTime);
			refundInfoDaoBean.setUpdateTime(currentTime);
			payInfoDao.insertRefundInfo(refundInfoDaoBean);
			

			Result<RspRefundBillBean> rspBean = new Result<RspRefundBillBean>();
			RspRefundBillBean rspContent = new RspRefundBillBean();
			rspBean.setData(rspContent);
			rspBean.setCode(Constants.RSP_RET_OK);
			rspBean.setMsg(Constants.RSP_MSG_OK);
			rspContent.setBillId(billInfo.getId().toString());
			rspContent.setSpId(billInfo.getSpId());
			rspContent.setOutTradeNo(billInfo.getOutTradeNo());
			rspContent.setOutRefundNo(reqBean.getOutRefundNo());
			rspContent.setRefundFee(reqBean.getAmount());
			rspContent.setStatus(Constants.REFUNDSTATUS_APPLY);
			
			return rspBean;
		} catch (TransException e) {
			throw e;
		} catch (UnsupportedEncodingException e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		} catch (ParserConfigurationException e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		} catch (SAXException e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_CONNECT_WECHAT, Message.getMessage(ErrorCode.ERR_CONNECT_WECHAT));
		}

	}

}
