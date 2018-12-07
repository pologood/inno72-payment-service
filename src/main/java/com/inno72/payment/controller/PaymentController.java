package com.inno72.payment.controller;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.inno72.common.Result;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqQueryBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.dto.RspQueryBillBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;
import com.inno72.payment.service.BillService;
import com.inno72.payment.service.ChannelFactoryService;
import com.inno72.payment.service.IdWorker;

@Controller
@RequestMapping("/pay")
public class PaymentController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BillService billService;
	
	@Autowired
	private ChannelFactoryService channelFactoryService;
	
	@Autowired
	private IdWorker idWorker;
	
	
	@RequestMapping("/queryBillInfo")
	@ResponseBody
    public  Result<RspQueryBillBean> queryBillInfo(ReqQueryBillBean reqBean) throws TransException {

		return billService.queryBill(reqBean);
		
    }
	
	@RequestMapping("/create")
	@ResponseBody
	public  Result<RspCreateBillBean> createBill(ReqCreateBillBean reqBean) throws TransException {
		
		logger.info(reqBean.toString());
		
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();

		logger.info("spId:" + req.getParameter("spId"));
		
		if(StringUtils.isBlank(reqBean.getSpId())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spId"));
		}
		
		PaySpInfoDaoBean spInfo = billService.querySpInfo(reqBean.getSpId());
		
		if(spInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spinfo not found"));
		}
		
		ThirdPartnerInfoDaoBean thirdPartnerInfo = billService.getThirdPartnerInfo(spInfo.getThirdpartnerGroupId(), reqBean.getType(), reqBean.getTerminalType());
		if(thirdPartnerInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "thirdPartnerInfo not found"));
		}
		
		return channelFactoryService.getChannel(reqBean.getType()).createBill(idWorker.nextId(), req.getRemoteAddr(), spInfo, thirdPartnerInfo, reqBean);
		
	}

}
