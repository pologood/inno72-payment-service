package com.inno72.payment.controller;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	@Autowired
	private BillService billService;
	
	@Autowired
	private ChannelFactoryService channelFactoryService;
	
	@Autowired
	private IdWorker idWorker;
	
	
	@RequestMapping(value="/queryBillInfo", method={RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
    public  Result<RspQueryBillBean> queryBillInfo(ReqQueryBillBean reqBean) throws TransException {

		return billService.queryBill(reqBean);
		
    }
	
	@RequestMapping(value="/create", method={RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public  Result<RspCreateBillBean> createBill(ReqCreateBillBean reqBean, HttpServletRequest req) throws TransException {
		
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
