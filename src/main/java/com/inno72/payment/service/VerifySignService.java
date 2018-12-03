package com.inno72.payment.service;

import java.util.Map;

public interface VerifySignService {
	
	public boolean verifySign(String spId, int terminalType, Map<String, String> data);
	
}
