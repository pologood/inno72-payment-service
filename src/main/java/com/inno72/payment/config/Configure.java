package com.inno72.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.inno72.payment.service.IdWorker;

@Configuration
public class Configure {
	
	@Bean
	IdWorker idWorkerBean() {
		return new IdWorker(1, 1);
	}

}
