package com.inno72.payment.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inno72.payment.common.Constants;
import com.inno72.payment.service.VerifySignAlipayService;
import com.inno72.payment.service.VerifySignWechatService;
import com.inno72.payment.utils.wechat.WechatXmlParse;

@Controller
@RequestMapping("/notify")
public class NotifyController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private VerifySignWechatService verifySignWechatService;
	
	@Autowired
	private VerifySignAlipayService verifySignAlipayService;

	
	
	@RequestMapping("/notify/alipay/{spId}")
	public void notifyFromAlipay(@PathVariable String spId, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		
		logger.info("alipay notify:" + req.getRequestURI());
		if(StringUtils.isNoneBlank(spId)) {
			logger.info("alipay spid is blank");
			rsp.sendError(401);
			return;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		Enumeration<String> paramNames = req.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			params.put(name, req.getParameter(name));
		}
			
		if(verifySignAlipayService.verifySign(spId, Constants.SOURCE_FLAG_IGNORE_PLATFORM, params)) {
			rsp.sendError(401);
			return;
		}
		
		rsp.sendError(401);
		
//		ReqNotifyBean reqNotifyBean = new ReqNotifyBean();
//		
//		
//		notifyService.handleNotification();
		
		
	}
	
	@RequestMapping("/notify/wechat/native/{spId}")
	public void notifyFromWechatQr(@PathVariable String spId, HttpServletRequest req, HttpServletResponse rsp) {
		
		try {
			
			if(StringUtils.isNoneBlank(spId)) {
				logger.info("alipay spid is blank");
				rsp.sendError(401);
				return;
			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(req.getInputStream()));
			StringBuilder xmlBuff = new StringBuilder();
		
			String line = null;
			while ((line = in.readLine()) != null) {
				xmlBuff.append(line);
			}
			
			logger.info(String.format("wechat notify %s:%s", req.getRequestURI(), xmlBuff.toString()));
			
			Map<String, String> params = WechatXmlParse.parse(xmlBuff.toString());
			if(verifySignWechatService.verifySign(spId, Constants.SOURCE_FLAG_QRCODE, params)) {
				rsp.sendError(401);
				return;
			}
			
			rsp.sendError(401);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		
	}
	
	
}
