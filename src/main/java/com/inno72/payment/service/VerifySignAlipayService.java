package com.inno72.payment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.DefaultSignChecker;
import com.alipay.api.SignChecker;
import com.inno72.payment.common.Constants;
import com.inno72.payment.mapper.PayInfoDao;

@Service
public class VerifySignAlipayService implements VerifySignService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PayInfoDao payInfoDao;
	
	@Override
	public boolean verifySign(String spId, int terminalType, Map<String, String> data) {
		
		
		String secureKey = payInfoDao.getSpThirdPartnerPublicKey(spId, Constants.PAY_CHANNEL_ALIPAY, terminalType);
		
		if(StringUtils.isBlank(secureKey)) {
			
			logger.error(String.format("not found secureKey: spID:%s, type:%d, terminalType:%d", spId, Constants.PAY_CHANNEL_ALIPAY, terminalType));
			
			return false;
		}
		
		String sign = data.get("sign");
		String signType = data.get("sign_type");
		if(StringUtils.isEmpty(sign)){
			logger.error("not found sign param");
			return false;
		}
		//过滤空值、sign与sign_type参数
    	Map<String, String> sParaNew = paraFilter(data);
        //获取待签名字符串
        String preSignStr = createLinkString(sParaNew);
		
        SignChecker checker = new DefaultSignChecker(secureKey);
		
        try {
        	if(!checker.check(preSignStr, sign, signType, Constants.SERVICE_CHARSET)) {
        		logger.warn("alipay sign is wrong");	
        		return false;
        	}
        	return true;
        }catch(Exception e) {
        	logger.error(e.getMessage(), e);
        	return false;
        }
	}
	
	
	public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
	
	 public static String createLinkString(Map<String, String> params) {

	        List<String> keys = new ArrayList<String>(params.keySet());
	        Collections.sort(keys);

	        String prestr = "";

	        for (int i = 0; i < keys.size(); i++) {
	            String key = keys.get(i);
	            String value = params.get(key);
	            
	            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
	                prestr = prestr + key + "=" + value;
	            } else {
	                prestr = prestr + key + "=" + value + "&";
	            }
	        }

	        return prestr;
	    }
	
}
