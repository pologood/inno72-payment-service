package com.inno72.payment.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.inno72.common.Result;
import com.inno72.payment.common.Constants;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqNotifyBean;
import com.inno72.payment.dto.ReqRefundNotifyBean;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.service.NotifyService;
import com.inno72.payment.service.VerifySignAlipayService;
import com.inno72.payment.service.VerifySignWechatService;
import com.inno72.payment.utils.wechat.WechatCore;
import com.inno72.payment.utils.wechat.WechatXmlParse;

@Controller
@RequestMapping("/notify")
public class NotifyController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private VerifySignWechatService verifySignWechatService;

	@Autowired
	private VerifySignAlipayService verifySignAlipayService;

	@Autowired
	private NotifyService notifyService;
	
	@Autowired
	private PayInfoDao payInfoDao;


	private static final String WECHAT_RSP_SUCCESS = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

	@RequestMapping("/alipay/{spId}")
	public void notifyFromAlipay(@PathVariable String spId, HttpServletRequest req, HttpServletResponse rsp)
			throws IOException {

		if (StringUtils.isBlank(spId)) {
			logger.info("spid is blank");
			rsp.sendError(401);
			return;
		}

		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> paramNames = req.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			params.put(name, req.getParameter(name));
		}

		if (!verifySignAlipayService.verifySign(spId, Constants.SOURCE_FLAG_IGNORE_PLATFORM, params)) {
			rsp.sendError(401);
			return;
		}

		if (!"TRADE_SUCCESS".equals(req.getParameter("trade_status"))) {
			rsp.getOutputStream().write("success".getBytes());
			rsp.getOutputStream().flush();
			return;
		}

		ReqNotifyBean reqNotifyBean = new ReqNotifyBean();

		SimpleDateFormat ds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {

			reqNotifyBean.setBillId(Long.parseLong(req.getParameter("out_trade_no")));
			reqNotifyBean.setClientIp(req.getRemoteAddr());
			reqNotifyBean.setNotifyTime(ds.parse(req.getParameter("notify_time")).getTime());
			reqNotifyBean.setRspCode("00");
			reqNotifyBean.setTradeNo(req.getParameter("trade_no"));
			reqNotifyBean.setRspMsg("ok");
			reqNotifyBean.setTotalFee(
					new BigDecimal(req.getParameter("total_amount")).multiply(new BigDecimal(100)).longValue());
			reqNotifyBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
			reqNotifyBean.setNotifyId(req.getParameter("notify_id"));
			reqNotifyBean.setUpdateTime(System.currentTimeMillis());
			reqNotifyBean.setNotifyParam(JSON.toJSONString(params));
			reqNotifyBean.setBuyerId(req.getParameter("buyer_id"));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rsp.sendError(401);
			return;
		}

		try {
			Result<Void> res = notifyService.handleNotification(reqNotifyBean);
			if (res.getCode() != Constants.RSP_RET_OK) {
				rsp.sendError(401);
				return;
			} else {
				logger.info("alipy notify success!!!");
				rsp.getOutputStream().write("success".getBytes());
				rsp.getOutputStream().flush();
				return;
			}
		} catch (TransException e) {
			logger.error(e.getMessage(), e);
			rsp.sendError(401);
		}
	}
	
	
	
	@RequestMapping("/wechat/refund/{spId}/{terminalType}")
	public void notifyWechatRefund(@PathVariable String spId, @PathVariable int terminalType, HttpServletRequest req, HttpServletResponse rsp) throws IOException{
		
		if (StringUtils.isBlank(spId)) {
			logger.info("spid is blank");
			rsp.sendError(401);
			return;
		}
		
		String line = null;
		
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
			StringBuilder xmlBuff = new StringBuilder();
			
			while ((line = in.readLine()) != null) {
				xmlBuff.append(line);
			}
			
			logger.info(String.format("wechat refund notify %s:%s", req.getRequestURI(), xmlBuff.toString()));
				
			Map<String, String> params = WechatXmlParse.parse(xmlBuff.toString());
			
			if (!"SUCCESS".equalsIgnoreCase(params.get("return_code"))) {
				rsp.setContentType("text/xml");
				rsp.getOutputStream().write(WECHAT_RSP_SUCCESS.getBytes());
				rsp.getOutputStream().flush();
				return;
			}
			
			SimpleDateFormat ds = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String reqInfo = params.get("req_info");
			
			String secureKey = payInfoDao.getSpSecureKey(spId, Constants.PAY_CHANNEL_WECHAT, terminalType);
			
			String refundInfo = WechatCore.decryptData(reqInfo, secureKey);
			
			logger.info(String.format("wechat refund notify decrypt %s", refundInfo));
			
			Map<String, String> refundParams = WechatXmlParse.parse(refundInfo);
			
			ReqRefundNotifyBean reqBean = new ReqRefundNotifyBean();
			reqBean.setClientIp(req.getRemoteAddr());
			reqBean.setMessage(refundInfo);
			reqBean.setOutRefundNo(Long.parseLong(refundParams.get("out_refund_no")));
			reqBean.setRefundFee(Long.parseLong(refundParams.get("refund_fee")));
			reqBean.setRefundId(refundParams.get("refund_id"));
			if("SUCCESS".equals(refundParams.get("refund_status"))) {
				reqBean.setRefundStatus(Constants.REFUNDSTATUS_SUCCESS);
				reqBean.setRspMsg("ok");
			}else {
				reqBean.setRefundStatus(Constants.REFUNDSTATUS_ERROR );
				reqBean.setRspMsg(String.format("%s:%s", refundParams.get("err_code"), refundParams.get("err_code_des")));
			}
			reqBean.setTotalFee(Long.parseLong(refundParams.get("total_fee")));
			reqBean.setTradeNo(refundParams.get("transaction_id"));
			reqBean.setUpdateTime(ds.parse(refundParams.get("success_time")).getTime());
			Result<Void> res = notifyService.handleRefundNotification(reqBean);
			
			if (res.getCode() != Constants.RSP_RET_OK) {
				rsp.sendError(401);
				return;
			} else {
				logger.info("wechat refunc notify success!!!");
				rsp.getOutputStream().write("success".getBytes());
				rsp.getOutputStream().flush();
				return;
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rsp.sendError(401);
			return;
		}
	}

	@RequestMapping("/wechat/native/{spId}")
	public void notifyFromWechatQr(@PathVariable String spId, HttpServletRequest req, HttpServletResponse rsp)
			throws IOException {

		if (StringUtils.isBlank(spId)) {
			logger.info("spid is blank");
			rsp.sendError(401);
			return;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
		StringBuilder xmlBuff = new StringBuilder();

		String line = null;
		while ((line = in.readLine()) != null) {
			xmlBuff.append(line);
		}
		try {
			logger.info(String.format("wechat notify %s:%s", req.getRequestURI(), xmlBuff.toString()));

			Map<String, String> params = WechatXmlParse.parse(xmlBuff.toString());
			if (!verifySignWechatService.verifySign(spId, Constants.SOURCE_FLAG_QRCODE, params)) {
				rsp.sendError(401);
				return;
			}

			if (!"SUCCESS".equalsIgnoreCase(params.get("result_code"))) {
				rsp.setContentType("text/xml");
				rsp.getOutputStream().write(WECHAT_RSP_SUCCESS.getBytes());
				rsp.getOutputStream().flush();
				return;
			}

			ReqNotifyBean reqNotifyBean = new ReqNotifyBean();
			SimpleDateFormat ds = new SimpleDateFormat("yyyyMMddHHmmss");


			reqNotifyBean.setBillId(Long.parseLong(params.get("out_trade_no")));
			reqNotifyBean.setClientIp(req.getRemoteAddr());
			reqNotifyBean.setNotifyId("0");
			reqNotifyBean.setNotifyTime(ds.parse(params.get("time_end")).getTime());

			reqNotifyBean.setRspCode("00");
			reqNotifyBean.setTradeNo(params.get("transaction_id"));
			reqNotifyBean.setBuyerId(params.get("openid"));
			
			reqNotifyBean.setRspMsg("ok");
			reqNotifyBean.setTotalFee(Long.parseLong(params.get("total_fee")));
			reqNotifyBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
			reqNotifyBean.setUpdateTime(System.currentTimeMillis());
			reqNotifyBean.setNotifyParam(JSON.toJSONString(params));
			
			
			Result<Void> res = notifyService.handleNotification(reqNotifyBean);
			if (res.getCode() != Constants.RSP_RET_OK) {
				rsp.sendError(401);
				return;
			} else {
				logger.info("wechat notify success!!!");
				rsp.setContentType("text/xml");
				rsp.getOutputStream().write(WECHAT_RSP_SUCCESS.getBytes());
				rsp.getOutputStream().flush();
				return;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rsp.sendError(401);
			return;
		}

	}
}
