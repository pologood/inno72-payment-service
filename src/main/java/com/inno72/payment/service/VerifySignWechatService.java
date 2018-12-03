package com.inno72.payment.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.inno72.payment.common.Constants;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.utils.Utility;


@Service
public class VerifySignWechatService implements VerifySignService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayInfoDao payInfoDao;

	@Override
	public boolean verifySign(String spId, int terminalType, Map<String, String> data) {
		
		
		String secureKey = payInfoDao.getSpSecureKey(spId, Constants.PAY_CHANNEL_WECHAT, terminalType);
		
		String sign = data.remove("sign");
		if(StringUtils.isEmpty(sign)){
			logger.error(String.format("not found secureKey: spID:%s, type:%d, terminalType:%d", spId, Constants.PAY_CHANNEL_ALIPAY, terminalType));
			return false;
		}
		
    	String presignStr = Utility.createLinkString(data);
    	
    	presignStr += "&key=" + secureKey;
    	try {
			String ourSign = Utility.GetMD5Code(presignStr.getBytes(Constants.SERVICE_CHARSET));
			if(sign.equalsIgnoreCase(ourSign)){
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
    	        
        logger.error("wechat sign is wrong");	
		return false;
	}

}
