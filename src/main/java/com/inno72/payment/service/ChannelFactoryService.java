package com.inno72.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;

@Service
public class ChannelFactoryService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ChannelAlipayService channelAlipayService;
	
	@Autowired
	ChannelWechatService channelWechatService;
	
	public ChannelService getChannel(int type) throws TransException {
	
		
		switch(type) {
			
			case Constants.PAY_CHANNEL_ALIPAY: return channelAlipayService;
			case Constants.PAY_CHANNEL_WECHAT: return channelWechatService;
			case Constants.PAY_CHANNEL_ALIPAY_SCAN: return channelAlipayService;

		}
		
		logger.warn("channel type do not support:" + type);
		throw new TransException(ErrorCode.ERR_NOT_SUPPORT, Message.getMessage(ErrorCode.ERR_NOT_SUPPORT));
		
	}
	
}
