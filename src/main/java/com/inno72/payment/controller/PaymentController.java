package com.inno72.payment.controller;


import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inno72.common.Result;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqQueryBillBean;
import com.inno72.payment.dto.ReqRefundBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.dto.RspQueryBillBean;
import com.inno72.payment.dto.RspRefundBillBean;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;
import com.inno72.payment.service.BillService;
import com.inno72.payment.service.ChannelFactoryService;
import com.inno72.payment.service.IdWorker;
import com.inno72.payment.utils.Utility;

@RestController
@RequestMapping("/pay")
public class PaymentController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BillService billService;
	
	@Autowired
	private ChannelFactoryService channelFactoryService;
	
	@Autowired
	private IdWorker idWorker;
	
	
	@RequestMapping(value="/queryBillInfo")
    public  Result<RspQueryBillBean> queryBillInfo(ReqQueryBillBean reqBean) throws TransException {

		return billService.queryBill(reqBean);
		
    }
	
	@RequestMapping(value="/create")
	public  Result<RspCreateBillBean> createBill(ReqCreateBillBean reqBean, HttpServletRequest req) throws TransException {
		
		if(StringUtils.isBlank(reqBean.getSpId())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spId"));
		}
		
		if(StringUtils.isBlank(reqBean.getSign())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
		}
		
		PaySpInfoDaoBean spInfo = billService.querySpInfo(reqBean.getSpId());
		
		if(spInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spinfo not found"));
		}
		
//		try {
//
//			String sign = Utility.makeSign(reqBean, spInfo.getSignKey());
//
//			if(!reqBean.getSign().equalsIgnoreCase(sign)) {
//				throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
//			}
//
//		} catch (IllegalArgumentException | IllegalAccessException | UnsupportedEncodingException e) {
//			logger.warn(e.getMessage(), e);
//			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
//		}
		
		ThirdPartnerInfoDaoBean thirdPartnerInfo = billService.getThirdPartnerInfo(spInfo.getThirdpartnerGroupId(), reqBean.getType(), reqBean.getTerminalType());
		if(thirdPartnerInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "thirdPartnerInfo not found"));
		}
		
		return channelFactoryService.getChannel(reqBean.getType()).createBill(idWorker.nextId(), req.getRemoteAddr(), spInfo, thirdPartnerInfo, reqBean);
		
	}
	
	
	@RequestMapping(value="/refund")
	public Result<RspRefundBillBean> refundBill(ReqRefundBillBean reqBean, HttpServletRequest req) throws TransException {
		
		if(StringUtils.isBlank(reqBean.getSpId())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spId"));
		}
		
		if(StringUtils.isBlank(reqBean.getOutTradeNo())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "outTradeNo"));
		}
		
		if(StringUtils.isBlank(reqBean.getSign())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
		}
		
		PaySpInfoDaoBean spInfo = billService.querySpInfo(reqBean.getSpId());
		
		if(spInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spinfo not found"));
		}
		
		try {
			
			String sign = Utility.makeSign(reqBean, spInfo.getSignKey());
			
			if(!reqBean.getSign().equalsIgnoreCase(sign)) {
				throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
			}
			
		} catch (IllegalArgumentException | IllegalAccessException | UnsupportedEncodingException e) {
			logger.warn(e.getMessage(), e);
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "sign"));
		}
		
		BillInfoDaoBean billInfo = billService.getBillInfoByOutTradeNo(reqBean.getSpId(), reqBean.getOutTradeNo());
		
		if(billInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_BILL_NOT_FOUND));
		}
		
		ThirdPartnerInfoDaoBean thirdPartnerInfo = billService.getThirdPartnerInfo(spInfo.getThirdpartnerGroupId(), billInfo.getType(), billInfo.getTerminalType());
		if(thirdPartnerInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "thirdPartnerInfo not found"));
		}
		
		
		return channelFactoryService.getChannel(billInfo.getType()).refundBill(idWorker.nextId(), reqBean, billInfo, spInfo, thirdPartnerInfo, req.getRemoteAddr());
		
	}

}
