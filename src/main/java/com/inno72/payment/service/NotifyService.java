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
import com.inno72.payment.dto.ReqRefundNotifyBean;
import com.inno72.payment.mapper.PayInfoDao;
import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaymentLogDaoBean;
import com.inno72.payment.model.RefundInfoDaoBean;
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

		if (reqBean.getTotalFee() != billInfoBean.getTotalFee()) {
			logger.error("notify money not equal orignal:" + reqBean.toString());
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_WRONG_PARAS));
		}
		long currentTime = System.currentTimeMillis();
		if (billInfoBean.getStatus() == Constants.PAYSTATUS_TRADE_SUCCESS) {

			if (billInfoBean.getNotifyStatus() == Constants.COMMON_STATUS_NO) {
				if (notifyCient(billInfoBean, reqBean)) {
					handleDbNotificationStatus(billInfoBean, reqBean, Constants.COMMON_STATUS_YES, currentTime);
				} else {
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
						currentTime, reqBean);
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

				if (notifyCient(billInfoBean, reqBean)) {
					handleDbPaySuccess(reqBean, billInfoBean, Constants.COMMON_STATUS_YES, currentTime);
					return Results.success();
				} else {
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
			long fee, long updateTime, ReqNotifyBean reqBean) {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(billID);
		logDaoBean.setBuyerId(reqBean.getBuyerId());
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

	protected void handleDbPaySuccess(ReqNotifyBean repBean, BillInfoDaoBean billInfoBean, int notifyStatus,
			long updateTime) throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(repBean.getBillId());
		logDaoBean.setBuyerId(billInfoBean.getBuyerId());
		logDaoBean.setIp(repBean.getClientIp());
		logDaoBean.setMessage(repBean.getNotifyParam());
		logDaoBean.setOutTradeNo(billInfoBean.getOutTradeNo());
		logDaoBean.setTerminalType(billInfoBean.getTerminalType());
		logDaoBean.setSpId(billInfoBean.getSpId());
		logDaoBean.setIsRefund(0);
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
		logDaoBean.setTotalFee(billInfoBean.getTotalFee());
		logDaoBean.setType(billInfoBean.getType());
		logDaoBean.setUpdateTime(updateTime);
		payInfoDao.insertPaymentLog(logDaoBean);
		if (payInfoDao.updatePaySuccess(repBean.getBillId(), repBean.getTradeNo(), repBean.getBuyerId(),
				repBean.getNotifyId(), updateTime, Constants.PAYSTATUS_TRADE_SUCCESS, notifyStatus, updateTime,
				billInfoBean.getUpdateTime()) == 0) {
			logger.warn("data conflict" + billInfoBean.toString());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}

	}

	protected void handleDbNotificationStatus(BillInfoDaoBean billInfoBean, ReqNotifyBean reqBean, int notifyStatus,
			long updateTime) throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(billInfoBean.getId());
		logDaoBean.setBuyerId(reqBean.getBuyerId());
		logDaoBean.setIp(reqBean.getClientIp());
		logDaoBean.setMessage("notify status:" + notifyStatus);
		logDaoBean.setOutTradeNo(billInfoBean.getOutTradeNo());
		logDaoBean.setSpId(billInfoBean.getSpId());
		logDaoBean.setIsRefund(0);
		logDaoBean.setStatus(Constants.PAYSTATUS_TRADE_SUCCESS);
		logDaoBean.setTotalFee(billInfoBean.getTotalFee());
		logDaoBean.setType(billInfoBean.getType());
		logDaoBean.setUpdateTime(updateTime);

		payInfoDao.insertPaymentLog(logDaoBean);
		if (payInfoDao.updateNotifyStatus(billInfoBean.getId(), notifyStatus, updateTime,
				billInfoBean.getUpdateTime()) == 0) {
			logger.warn("data conflict billId:" + billInfoBean.getId());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}
	}


	protected boolean notifyCient(BillInfoDaoBean billInfoBean, ReqNotifyBean reqBean) {

		if (StringUtils.isBlank(billInfoBean.getNotifyUrl())) {
			return true;
		}

		Map<String, String> form = new HashMap<String, String>();

		form.put("retCode", "0");
		form.put("spId", billInfoBean.getSpId());
		form.put("billId", billInfoBean.getId().toString());
		form.put("outTradeNo", billInfoBean.getOutTradeNo());
		form.put("buyerId", reqBean.getBuyerId());
		form.put("type", billInfoBean.getType().toString());
		form.put("terminalType", billInfoBean.getTerminalType().toString());
		form.put("fee", Long.toString(billInfoBean.getTotalFee()));
		form.put("extra", billInfoBean.getNotifyParam());

		try {
			HttpFormConnector.doPost(billInfoBean.getNotifyUrl(), form, 1000);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}


	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Result<Void> handleRefundNotification(ReqRefundNotifyBean reqBean) throws TransException {

		RefundInfoDaoBean refundInfo = payInfoDao.getRefundInfo(reqBean.getOutRefundNo());
		
		BillInfoDaoBean billInfo = payInfoDao.getBillInfoByBillId(refundInfo.getBillId());

		if (refundInfo == null || billInfo == null) {
			throw new TransException(ErrorCode.ERR_BILL_NOT_FOUND, Message.getMessage(ErrorCode.ERR_BILL_NOT_FOUND));
		}

		if (reqBean.getRefundFee() != refundInfo.getRefundFee()) {
			logger.error("notify money not equal orignal:" + reqBean.toString());
			throw new TransException(ErrorCode.ERR_WRONG_PARAS, Message.getMessage(ErrorCode.ERR_WRONG_PARAS));
		}

		long currentTime = System.currentTimeMillis();
		if (refundInfo.getStatus() == Constants.REFUNDSTATUS_SUCCESS) {

			if (refundInfo.getNotifyStatus() == Constants.COMMON_STATUS_NO) {
				if (notifyRefundCient(reqBean.getRefundStatus(), refundInfo)) {
					payInfoDao.updateRefundNotifyStatus(refundInfo.getId(), Constants.COMMON_STATUS_YES, currentTime,
							refundInfo.getUpdateTime());
				} else {
					return Results.failure("notify fail");
				}
			} else {
				return Results.success();
			}
		}
		
		if (notifyRefundCient(reqBean.getRefundStatus(), refundInfo)) {
			handleRefundInfo(refundInfo, billInfo, reqBean, reqBean.getRefundStatus(), Constants.COMMON_STATUS_YES, currentTime);
			return Results.success();
		}else {
			handleRefundInfo(refundInfo, billInfo, reqBean, reqBean.getRefundStatus(), Constants.COMMON_STATUS_NO, currentTime);
			return Results.failure("notify fail");
		}
		
	}

	protected void handleDbRefundNotificationStatus(RefundInfoDaoBean refundInfo, ReqRefundNotifyBean reqBean,
			int notifyStatus, long updateTime) throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(refundInfo.getId());
		logDaoBean.setIp(reqBean.getClientIp());
		logDaoBean.setMessage("notify status:" + notifyStatus);
		logDaoBean.setOutTradeNo(refundInfo.getOutRefundNo());
		logDaoBean.setSpId(refundInfo.getSpId());
		logDaoBean.setIsRefund(1);
		logDaoBean.setStatus(reqBean.getRefundStatus());
		logDaoBean.setTotalFee(refundInfo.getRefundFee());
		logDaoBean.setType(refundInfo.getType());
		logDaoBean.setUpdateTime(updateTime);

		payInfoDao.insertPaymentLog(logDaoBean);

		if (payInfoDao.updateRefundNotifyStatus(refundInfo.getId(), notifyStatus, updateTime,
				refundInfo.getUpdateTime()) == 0) {
			logger.warn("data conflict billId:" + refundInfo.getId());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}
	}


	protected void handleRefundInfo(RefundInfoDaoBean refundInfo, BillInfoDaoBean billInfo, ReqRefundNotifyBean reqBean, int status,
			int notifyStatus, long updateTime) throws TransException {

		PaymentLogDaoBean logDaoBean = new PaymentLogDaoBean();
		logDaoBean.setBillId(refundInfo.getId());
		logDaoBean.setIp(reqBean.getClientIp());
		logDaoBean.setMessage(reqBean.getMessage());
		logDaoBean.setOutTradeNo(refundInfo.getOutRefundNo());
		logDaoBean.setSpId(refundInfo.getSpId());
		logDaoBean.setIsRefund(1);
		logDaoBean.setStatus(status);
		logDaoBean.setTotalFee(refundInfo.getRefundFee());
		logDaoBean.setTerminalType(billInfo.getTerminalType());
		logDaoBean.setType(refundInfo.getType());
		logDaoBean.setUpdateTime(updateTime);
		payInfoDao.insertPaymentLog(logDaoBean);

		if (payInfoDao.updateRefundStatus(refundInfo.getId(), reqBean.getRefundId(), status, notifyStatus,
				reqBean.getMessage(), updateTime, refundInfo.getUpdateTime()) == 0) {
			logger.warn("data conflict" + refundInfo.toString());
			throw new TransException(ErrorCode.ERR_DATA_CONFLICT, Message.getMessage(ErrorCode.ERR_DATA_CONFLICT));
		}


	}

	protected boolean notifyRefundCient(int ret, RefundInfoDaoBean refundInfo) {


		if (StringUtils.isBlank(refundInfo.getNotifyUrl())) {
			return true;
		}

		Map<String, String> form = new HashMap<String, String>();


		form.put("retCode", ret == Constants.REFUNDSTATUS_SUCCESS ? "0" : "-1");
		form.put("fundId", refundInfo.getId().toString());
		form.put("spId", refundInfo.getSpId());
		form.put("outTradeNo", refundInfo.getOutTradeNo());
		form.put("outRefundNo", refundInfo.getOutRefundNo());
		form.put("fee", Long.toString(refundInfo.getRefundFee()));

		try {
			HttpFormConnector.doPost(refundInfo.getNotifyUrl(), form, 1000);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

}
