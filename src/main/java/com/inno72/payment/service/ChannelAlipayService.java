package com.inno72.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
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
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;


@Service("channelAlipayService")
public class ChannelAlipayService extends ChannelBaseService {
	
	private static final String ALIPAY_URL = "https://openapi.alipay.com/gateway.do";

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
			
			case "dev": return "http://pay.72solo.com/notify/alipay/%s";
			case "test": return "http://pay.36solo.com/notify/alipay/%s";
			case "stage": return "http://pay.32solo.com/notify/alipay/%s";
			case "prod": return "http://pay.inno72.com/notify/alipay/%s";
			default:
				logger.error("not found profile:" + state);
				throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
			
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<RspCreateBillBean> createBill(long billId, String remoteIp, PaySpInfoDaoBean spInfo, ThirdPartnerInfoDaoBean thirdPartnerInfo,
			ReqCreateBillBean reqBean) throws TransException {

		checkTerminalType(reqBean.getTerminalType());

		checkPayRequest(reqBean);

		long currentTime = System.currentTimeMillis();

		if (reqBean.getTerminalType() == Constants.SOURCE_FLAG_QRCODE) {

			AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_URL,
					thirdPartnerInfo.getAppId(), thirdPartnerInfo.getPrivateKey(), "json", Constants.SERVICE_CHARSET,
					thirdPartnerInfo.getThirdpartnerPublicKey(), "RSA2");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("seller_id", thirdPartnerInfo.getMid());
			params.put("out_trade_no", Long.toString(billId));
			params.put("total_amount", new BigDecimal(reqBean.getTotalFee()).divide(new BigDecimal(100)).doubleValue());
			params.put("subject", reqBean.getSubject());
			params.put("body", reqBean.getRemark());
			if (reqBean.getTransTimeout() != null) {
				params.put("timeout_express", String.format("%dm", reqBean.getTransTimeout().intValue()));
			}
			if (reqBean.getQrTimeout() != null) {
				params.put("qr_code_timeout_express", String.format("%dm", reqBean.getQrTimeout().intValue()));
			}

			AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
			request.setNotifyUrl(String.format(getNotifyUrl(), spInfo.getId()));
			if(StringUtils.isNoneBlank(reqBean.getReturnUrl())) {
				request.setReturnUrl(reqBean.getReturnUrl());
			}
			request.setBizContent(JSON.toJSON(params).toString());

			AlipayTradePrecreateResponse response;
			try {
				response = alipayClient.execute(request);
				if (response.isSuccess()) {
					logger.info(
							String.format("alipay replay ok: %s, %s", response.getOutTradeNo(), response.getQrCode()));
				} else {
					logger.warn(String.format("alipay replay error: %s,%s", response.getCode(), response.getMsg()));
					throw new TransException(ErrorCode.ERR_CONNECT_ALIPAY,
							Message.getMessage(ErrorCode.ERR_CONNECT_ALIPAY));
				}
			} catch (AlipayApiException e) {
				logger.error(e.getErrMsg(), e);
				throw new TransException(ErrorCode.ERR_CONNECT_ALIPAY,
						Message.getMessage(ErrorCode.ERR_CONNECT_ALIPAY));
			}

			try {
				handleDbCreateBill(billId, spInfo, null, currentTime, remoteIp, reqBean);
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
			rspBean.getData().setQrCodeUrl(response.getQrCode());
			return rspBean;

		}

		logger.warn("terminalType do not support:" + thirdPartnerInfo.getTerminalType());
		throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<RspRefundBillBean> refundBill(ReqRefundBillBean reqBean, PaySpInfoDaoBean spInfo,
			ThirdPartnerInfoDaoBean thirdPartnerInfo, String remoteIp) throws TransException {
		
		
		BillInfoDaoBean billInfo = checkRefundRequest(reqBean);
		
		AlipayClient alipayClient = new DefaultAlipayClient(ALIPAY_URL,
				thirdPartnerInfo.getAppId(), thirdPartnerInfo.getPrivateKey(), "json", Constants.SERVICE_CHARSET,
				thirdPartnerInfo.getThirdpartnerPublicKey(), "RSA2");
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		
		
		
		return null;
	}

}
