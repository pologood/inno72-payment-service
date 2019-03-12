package com.inno72.payment.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.inno72.common.Result;
import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqQueryBillBean;
import com.inno72.payment.dto.RspQueryBillBean;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;



@Service
public class BillService {
	
	private Logger logger = LoggerFactory.getLogger(BillService.class);
	
	@Resource
	protected PayInfoDao payInfoDao;
	
	
	public PaySpInfoDaoBean querySpInfo(String spId) {
		return payInfoDao.getSpInfo(spId);
	}
	
	public ThirdPartnerInfoDaoBean getThirdPartnerInfo(String groupId, int type, int terminalType) {
		
		if(type == Constants.PAY_CHANNEL_ALIPAY || type == Constants.PAY_CHANNEL_ALIPAY_SCAN) {
			terminalType = Constants.SOURCE_FLAG_IGNORE_PLATFORM;
		}
		
		return payInfoDao.getThirdPartnerInfo(groupId, type, terminalType);
	}
	
	public BillInfoDaoBean getBillInfoByOutTradeNo (String spId, String outTradeNo) {
		return payInfoDao.getBillInfoByOutTradeNo(spId, outTradeNo);
	}
	
	public Result<RspQueryBillBean> queryBill(ReqQueryBillBean reqBean) throws TransException {
		
		if(StringUtils.isEmpty(reqBean.getSpId())){
			logger.warn("spId or outTradeNo is empty");
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_WRONG_PARAS));  

		}
		
		BillInfoDaoBean billInfoDaoBean = null;
		long billId;
		if(!StringUtils.isEmpty(reqBean.getSpId()) && !StringUtils.isEmpty(reqBean.getBillId())){
			billId = Long.parseLong(reqBean.getBillId());
			billInfoDaoBean = payInfoDao.getBillInfoByBillId(billId);
			if(billInfoDaoBean != null){
				if(!reqBean.getSpId().equals(billInfoDaoBean.getSpId())){
					logger.warn("the bill:" + reqBean.getBillId() + " not own spid:"+ reqBean.getSpId());
					billInfoDaoBean = null;
				}
			}
		}else if(!StringUtils.isEmpty(reqBean.getSpId()) && !StringUtils.isEmpty(reqBean.getOutTradeNo())){
			billInfoDaoBean = payInfoDao.getBillInfoByOutTradeNo(reqBean.getSpId(), reqBean.getOutTradeNo());
		}else{
			logger.warn("spId or OutTradeNo and BillId is empty");
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_WRONG_PARAS));  
		}
		
		RspQueryBillBean rspQueryBillBean = null;
		if(billInfoDaoBean != null){
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			rspQueryBillBean = new RspQueryBillBean();
			rspQueryBillBean.setBuyerId(billInfoDaoBean.getBuyerId());
			rspQueryBillBean.setCreateTime(df.format(new Date(billInfoDaoBean.getCreateTime())));
			rspQueryBillBean.setId(billInfoDaoBean.getId().toString());
			rspQueryBillBean.setIsRefund(billInfoDaoBean.getIsRefund());
			rspQueryBillBean.setTerminalType(billInfoDaoBean.getTerminalType());
			rspQueryBillBean.setNotifyId(billInfoDaoBean.getNotifyId());
			rspQueryBillBean.setNotifyStatus(billInfoDaoBean.getNotifyStatus());
			if(billInfoDaoBean.getNotifyTime() != null) {
				rspQueryBillBean.setNotifyTime(df.format(new Date(billInfoDaoBean.getNotifyTime())));
			}
			rspQueryBillBean.setNotifyUrl(billInfoDaoBean.getNotifyUrl());
			rspQueryBillBean.setOutTradeNo(billInfoDaoBean.getOutTradeNo());
			rspQueryBillBean.setPrice(billInfoDaoBean.getPrice());
			rspQueryBillBean.setQuantity(billInfoDaoBean.getQuantity());
			rspQueryBillBean.setRefundAmount(billInfoDaoBean.getRefundAmount());
			rspQueryBillBean.setReturnUrl(billInfoDaoBean.getReturnUrl());
			rspQueryBillBean.setSellerId(billInfoDaoBean.getSellerId());
			rspQueryBillBean.setShowUrl(billInfoDaoBean.getShowUrl());
			rspQueryBillBean.setSpId(billInfoDaoBean.getSpId());
			rspQueryBillBean.setStatus(billInfoDaoBean.getStatus());
			rspQueryBillBean.setSubject(billInfoDaoBean.getSubject());
			rspQueryBillBean.setTotalFee(billInfoDaoBean.getTotalFee());
			rspQueryBillBean.setType(billInfoDaoBean.getType());
			rspQueryBillBean.setUpdateTime(df.format(new Date(billInfoDaoBean.getUpdateTime())));
		}
		
		Result<RspQueryBillBean> rsqBean = new Result<RspQueryBillBean>();
		rsqBean.setData(rspQueryBillBean);

		return rsqBean;
	}
	

	
}
