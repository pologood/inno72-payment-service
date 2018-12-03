package com.inno72.payment.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

public class Utility {

	private final static String[] strDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
			"e", "f" };

	

	public static String GetMD5Code(byte[] bByte) {
		String resultString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			resultString = byteToString(md.digest(bByte));

		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return resultString;
	}

	private static String byteToString(byte[] bByte) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < bByte.length; i++) {
			sBuffer.append(byteToArrayString(bByte[i]));
		}
		return sBuffer.toString();
	}

	private static String byteToArrayString(byte bByte) {
		int iRet = bByte;
		// System.out.println("iRet="+iRet);
		if (iRet < 0) {
			iRet += 256;
		}
		int iD1 = iRet / 16;
		int iD2 = iRet % 16;
		return strDigits[iD1] + strDigits[iD2];
	}

	public static String makeCheckSign(String lisence, List<String> items) {

		String sign = StringUtils.join(items.toArray(), '&');
		sign += '&' + lisence;

		return GetMD5Code(sign.getBytes());
	}

	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		return (s.replaceAll("-", "")).toUpperCase();
	}


	public static String createLinkString(Map<String, String> params) {

		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		String prestr = "";

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			if (i == keys.size() - 1) {
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr;
	}
	
	
	public static String makeSign(Object obj, String secureKey) throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException{
		
		Field[] fields = obj.getClass().getDeclaredFields();
		Map<String, String> signMap = new HashMap<String, String>();
		for(Field field : fields){
			
			field.setAccessible(true);
			
			String type = field.getType().toString();
			String value = "";
			String key = field.getName();
			switch(type){
				case "Long":
				case "long":
					value = field.get(obj) != null ? Long.toString((Long)field.get(obj)):"";
					break;
				case "int":
				case "Integer":
					value = field.get(obj) != null ? Long.toString((Integer)field.get(obj)):"";
					break;
				case "String":
					value = (String)field.get(obj);
					break;
			}
				
			signMap.put(key, value);
		}
		
		signMap.remove("sign");
		
		String sign = createLinkString(signMap) + "&" + secureKey;
		sign = GetMD5Code(sign.getBytes("utf-8"));
		return sign;
		
	}
		
}