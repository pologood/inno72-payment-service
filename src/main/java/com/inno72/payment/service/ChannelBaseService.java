package com.inno72.payment.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqRefundBillBean;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.PaymentLogDaoBean;

public abstract class ChannelBaseService implements ChannelService{

	private Logger logger = LoggerFactory.getLogger(ChannelBaseService.class);
	
	@Autowired
	protected PayInfoDao payInfoDao;
	
	
	protected void checkPayRequest(ReqCreateBillBean reqBean) throws TransException{
		
		if(StringUtils.isBlank(reqBean.getOutTradeNo())){
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "OutTradeNo"));
		}
		
		if(!StringUtils.isBlank(reqBean.getExtra()) && reqBean.getExtra().length() > 128) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "extra"));
		}
		
		if(reqBean.getTotalFee() == 0){
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "fee"));
		}
		
		if(StringUtils.isBlank(reqBean.getSubject())) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "subject"));
		}
		
		if(reqBean.getUnitPrice() > 0 || reqBean.getQuantity() > 0){	
			if(reqBean.getTotalFee() != reqBean.getUnitPrice()*reqBean.getQuantity() )
				throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "fee"));
		}
		
		BillInfoDaoBean billInfo = payInfoDao.getBillInfoByOutTradeNo(reqBean.getSpId(), reqBean.getOutTradeNo());
		if(billInfo != null){
			logger.warn("bill is repeated:" + billInfo.toString());
			throw new TransException(ErrorCode.ERR_HADPAYED, Message.getMessage(ErrorCode.ERR_HADPAYED));
		}
		
	}
	
	
	public void checkRefundRequest(ReqRefundBillBean reqBean, BillInfoDaoBean billInfo) throws TransException{
		
		if(StringUtils.isBlank(reqBean.getOutRefundNo())){
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "outRefundNo"));
		}
		
		if(StringUtils.isBlank(reqBean.getOutTradeNo())){
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "outTradeNo"));
		}
		
		if(StringUtils.isBlank(reqBean.getReason())){
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "reason"));
		}
		
		if(reqBean.getAmount() <= 0) {
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "amount"));
		}
		
		if(billInfo == null){
			logger.warn(String.format("bill not found spid:%s, outTradeNo:%s", reqBean.getSpId(), reqBean.getOutTradeNo()));
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "OutTradeNo or spid"));
		}
		
		if(billInfo.getStatus() != Constants.PAYSTATUS_TRADE_SUCCESS) {
			logger.warn(String.format("bill status is wrong:%s, outTradeNo:%s", reqBean.getSpId(), reqBean.getOutTradeNo()));
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "OutTradeNo or spid"));
		}
		
		if(billInfo.getIsRefund() == 1) {
			logger.warn("bill had refound");
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "amount"));

		}
		
		if(reqBean.getAmount() != billInfo.getTotalFee()) {
			logger.warn("bill can not refound amount is wrong");
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, String.format(Message.getMessage(ErrorCode.ERR_WRONG_PARAS), "amount"));
		}
		
	}
	
	
	
	
	protected void handleDbCreateBill(long payId,
			PaySpInfoDaoBean spInfo,
			String prepayId,
			long updateTime,
			String remoteIp,
			ReqCreateBillBean reqBean){
		
		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(payId);
		logDaoBean.setBuyerId("");
		logDaoBean.setIp(remoteIp);
		if(StringUtils.isNotBlank(reqBean.getClientIp())) {
			logDaoBean.setMessage("create bill client ip:" + reqBean.getClientIp());
		}else {
			logDaoBean.setMessage("create bill");
		}
		logDaoBean.setOutTradeNo(reqBean.getOutTradeNo());
		logDaoBean.setSpId(reqBean.getSpId());
		logDaoBean.setIsRefund(0);
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_PREPARE);
		logDaoBean.setTotalFee(reqBean.getTotalFee());
		logDaoBean.setType(reqBean.getType());
		logDaoBean.setTerminalType(reqBean.getTerminalType());
		logDaoBean.setUpdateTime(updateTime);
		
		BillInfoDaoBean billInfo = new BillInfoDaoBean();
		billInfo.setId(payId);
		billInfo.setBuyerId(null);
		billInfo.setSellerId(spInfo.getId());
		billInfo.setIsRefund(Constants.COMMON_STATUS_NO);
		billInfo.setTerminalType(reqBean.getTerminalType());
		billInfo.setType(reqBean.getType());
		billInfo.setNotifyStatus(Constants.COMMON_STATUS_NO);
		billInfo.setRefundAmount(0);
		billInfo.setSubject(reqBean.getSubject());
		billInfo.setShowUrl(reqBean.getMerchantUrl());
		billInfo.setOutTradeNo(reqBean.getOutTradeNo());
		billInfo.setSpId(reqBean.getSpId());
		billInfo.setPrice(reqBean.getUnitPrice());
		billInfo.setQuantity(reqBean.getQuantity());
		billInfo.setReturnUrl(reqBean.getReturnUrl());
		billInfo.setNotifyUrl(reqBean.getNotifyUrl());
		billInfo.setNotifyParam(reqBean.getExtra());
		billInfo.setStatus(Constants.PAYSTATUS_TRADE_PREPARE);
		billInfo.setTotalFee(reqBean.getTotalFee());
		billInfo.setUpdateTime(updateTime);
		billInfo.setCreateTime(updateTime);
		billInfo.setPrepayId(prepayId);
		
		payInfoDao.insertPaymentLog(logDaoBean);
		payInfoDao.insertBillInfo(billInfo);
	}
	
}
