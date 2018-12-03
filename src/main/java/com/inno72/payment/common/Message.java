package com.inno72.payment.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Message {

	private static final Map<Integer, String> message = new HashMap<Integer, String>();;

	static {
		
		Properties props = new Properties();
		try {
			InputStream in = Message.class.getResourceAsStream("/msg.properties");
			Reader reader = new InputStreamReader(in, "utf-8");
			props.load(reader);
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String key;
		for (Map.Entry<Object, Object> en : props.entrySet()) {
			key = (String) en.getKey();
			message.put(Integer.parseInt(key), props.getProperty(key));
		}
	}
	
	public static String getMessage(int key){
		if(message.containsKey(key)){
			return message.get(key);
		}
		return "";
	}

}
