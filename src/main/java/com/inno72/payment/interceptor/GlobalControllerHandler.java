package com.inno72.payment.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.inno72.common.Result;
import com.inno72.payment.common.TransException;

/**
 * 全局异常处理
 */
@SuppressWarnings("rawtypes")
@ControllerAdvice
@ResponseBody
public class GlobalControllerHandler implements ResponseBodyAdvice<Result> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(Exception.class)
	public Result handleServiceException(Exception ex) {
		
		Result result = new Result();
		if(ex instanceof TransException) {
			result.setCode(((TransException)ex).getRet());
			result.setMsg(((TransException)ex).getMsg());
		}else {
			int retCode = -100;
			String msg = "系统错误";
			result.setCode(retCode);
			result.setMsg(msg);
		}
			
		return result;
	}

	public boolean supports(MethodParameter returnType, Class converterType) {
		return true;
	}

	public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		
		String out = null;
		if(body != null)
			out = String.format("{code:%s, msg:%s, data:%s}", body.getCode(), body.getMsg(), body.getData());
		
		logger.info(String.format("http rsp: %s", out));
		
		return body;
	}
}
