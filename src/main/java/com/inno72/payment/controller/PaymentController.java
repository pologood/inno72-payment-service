package com.inno72.payment.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inno72.common.Result;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqQueryBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.dto.RspQueryBillBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfo;
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
	
	
	@RequestMapping("/queryBillInfo")
    public  Result<RspQueryBillBean> queryBillInfo(ReqQueryBillBean reqBean) throws TransException {

		return billService.queryBill(reqBean);
		
    }
	
	@RequestMapping("/create")
	public Result<RspCreateBillBean> createBill(ReqCreateBillBean reqBean) throws TransException {
		
		if(StringUtils.isBlank(reqBean.getSpId())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spId"));
		}
		
		PaySpInfoDaoBean spInfo = billService.querySpInfo(reqBean.getSpId());
		
		if(spInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "spinfo not found"));
		}
		
		ThirdPartnerInfo thirdPartnerInfo = billService.getThirdPartnerInfo(spInfo.getThirdpartnerGroupId(), reqBean.getType(), reqBean.getTerminalType());
		if(thirdPartnerInfo == null) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "thirdPartnerInfo not found"));
		}
		
		return channelFactoryService.getChannel(reqBean.getType()).createBill(idWorker.nextId(), spInfo, thirdPartnerInfo, reqBean);
		
	}

}
