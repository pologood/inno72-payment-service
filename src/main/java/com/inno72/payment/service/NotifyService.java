package com.inno72.payment.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inno72.common.Result;
import com.inno72.common.Results;
import com.inno72.payment.common.Constants;
import com.inno72.payment.common.ErrorCode;
import com.inno72.payment.common.Message;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqNotifyBean;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaymentLogDaoBean;
import com.inno72.payment.utils.HttpFormConnector;

@Service
public class NotifyService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Resource
	private PayInfoDao payInfoDao;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<Void> handleNotification(ReqNotifyBean reqBean) throws TransException {
		
		BillInfoDaoBean billInfoBean = payInfoDao.getBillInfoByBillId(reqBean.getBillId());
		if (billInfoBean == null) {
			throw new TransException(ErrorCode.ERR_BILL_NOT_FOUND, Message.getMessage(ErrorCode.ERR_BILL_NOT_FOUND));
		}
		
		if(reqBean.getTotalFee() != billInfoBean.getTotalFee()){
			logger.error("notify money not equal orignal:" + reqBean.toString());
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_WRONG_PARAS));
		}
		long currentTime = System.currentTimeMillis();
		if (billInfoBean.getStatus() == Constants.PAYSTATUS_TRADE_SUCCESS) {
			
			if (billInfoBean.getNotifyStatus() == Constants.COMMON_STATUS_NO) {
				if(notifyCient(billInfoBean)) {
					handleDbNotificationStatus(billInfoBean, reqBean, Constants.COMMON_STATUS_YES, currentTime);
				}else {
					return Results.failure("notify fail");
				}
			} else {
				return Results.success();
			}
		}
		
		if (!Constants.NOTIFY_RET_CODE_OK.equals(reqBean.getRspCode())) {
			try {
				handleDbErrorLog(reqBean.getBillId(), billInfoBean.getType(), billInfoBean.getSpId(),
						reqBean.getClientIp(), reqBean.getRspMsg(), billInfoBean.getOutTradeNo(), reqBean.getTotalFee(),
						currentTime);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TransException(ErrorCode.ERR_DB, Message.getMessage(ErrorCode.ERR_DB));
			}
			return Results.success();
		} else {

			if (Constants.PAYSTATUS_TRADE_SUCCESS != reqBean.getStatus()) {
				logger.info("the orde is not success:" + reqBean.toString());
				return Results.success();
			}
			try {
				
				if(notifyCient(billInfoBean)) {
					handleDbPaySuccess(reqBean, billInfoBean, Constants.COMMON_STATUS_YES, currentTime);
					return Results.success();
				}else {
					handleDbPaySuccess(reqBean, billInfoBean, Constants.COMMON_STATUS_NO, currentTime);
					return Results.failure("notify fail");
				}
			} catch (TransException e) {
				throw e;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TransException(ErrorCode.ERR_DB, Message.getMessage(ErrorCode.ERR_DB));
			}
		}
	}
	
	protected void handleDbErrorLog(long billID, int type, String spId, String ip, String msg, String outTradeNo,
			long fee, long updateTime) {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(billID);
		logDaoBean.setBuyerId("");
		logDaoBean.setIp(ip);
		logDaoBean.setMessage(msg);
		logDaoBean.setOutTradeNo(outTradeNo);
		logDaoBean.setSpId(spId);
		logDaoBean.setIsRefund(0);
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_ERROR);
		logDaoBean.setTotalFee(fee);
		logDaoBean.setType(type);
		logDaoBean.setUpdateTime(updateTime);
		payInfoDao.insertPaymentLog(logDaoBean);

	}
	
	protected void handleDbPaySuccess(ReqNotifyBean repBean, BillInfoDaoBean billInfoBean, int notifyStatus ,long updateTime)
			throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(repBean.getBillId());
		logDaoBean.setBuyerId("");
		logDaoBean.setIp(repBean.getClientIp());
		logDaoBean.setMessage(repBean.getRspMsg());
		logDaoBean.setOutTradeNo(billInfoBean.getOutTradeNo());
		logDaoBean.setSpId(billInfoBean.getSpId());
		logDaoBean.setIsRefund(0);
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
		logDaoBean.setTotalFee(billInfoBean.getTotalFee());
		logDaoBean.setType(billInfoBean.getType());
		logDaoBean.setUpdateTime(updateTime);
		payInfoDao.insertPaymentLog(logDaoBean);
		if (payInfoDao.updatePaySuccess(repBean.getBillId(), repBean.getTradeNo(), repBean.getNotifyId(), updateTime,
				Constants.PAYSTATUS_TRADE_SUCCESS,  notifyStatus, updateTime, billInfoBean.getUpdateTime()) == 0) {
			logger.warn("data conflict" + billInfoBean.toString());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}

	}
	
	protected void handleDbNotificationStatus(BillInfoDaoBean billInfoBean, ReqNotifyBean reqBean, int notifyStatus, long updateTime) throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(billInfoBean.getId());
		logDaoBean.setBuyerId("");
		logDaoBean.setIp(reqBean.getClientIp());
		logDaoBean.setMessage("notify status:" + notifyStatus);
		logDaoBean.setOutTradeNo(billInfoBean.getOutTradeNo());
		logDaoBean.setSpId(billInfoBean.getSpId());
		logDaoBean.setIsRefund(0);;
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
		logDaoBean.setTotalFee(billInfoBean.getTotalFee());
		logDaoBean.setType(billInfoBean.getType());
		logDaoBean.setUpdateTime(updateTime);

		payInfoDao.insertPaymentLog(logDaoBean);
		if (payInfoDao.updateNotifyStatus(billInfoBean.getId(), notifyStatus, updateTime) == 0) {
			logger.warn("data conflict billId:" + billInfoBean.getId());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}
	}
	
	
	protected boolean notifyCient(BillInfoDaoBean billInfoBean) {
		
		if(StringUtils.isBlank(billInfoBean.getNotifyUrl())) {
			return true;
		}
		
		Map<String, String> form = new HashMap<String, String>();
		
		form.put("billId", billInfoBean.getId().toString());
		form.put("outTradeNo", billInfoBean.getOutTradeNo());
		form.put("retCode", "0");
		form.put("extra", billInfoBean.getNotifyParam());
		form.put("fee", Long.toString(billInfoBean.getTotalFee()));
		
		try {
			HttpFormConnector.doPost(billInfoBean.getNotifyUrl(), form, 1000);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}
}
