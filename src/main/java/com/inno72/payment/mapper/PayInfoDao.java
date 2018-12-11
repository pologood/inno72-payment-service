package com.inno72.payment.mapper;

import org.apache.ibatis.annotations.Param;

import com.inno72.payment.model.BillInfoDaoBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.PaymentLogDaoBean;
import com.inno72.payment.model.RefundInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;

public interface PayInfoDao {

	public PaySpInfoDaoBean getSpInfo(@Param("spId") String spId);
	
	public String getSpSecureKey(@Param("spId")String spId, @Param("type")int type, @Param("terminalType")int terminalType);
	
	public String getSpThirdPartnerPublicKey(@Param("spId")String spId, @Param("type")int type, @Param("terminalType")int terminalType);

	public BillInfoDaoBean getBillInfoByBillId(@Param("billId") Long billId);

	public BillInfoDaoBean getBillInfoByOutTradeNo(@Param("spId") String spId, @Param("outTradeNo") String outTradeNo);

	public ThirdPartnerInfoDaoBean getThirdPartnerInfo(@Param("groupId")String groupId, @Param("type")int type, @Param("terminalType") int terminalType);
	
	public int insertBillInfo(BillInfoDaoBean bean);

	public int insertPaymentLog(PaymentLogDaoBean bean);

	public int updatePaySuccess(@Param("billId") Long billId, @Param("tradeNo") String tradeNo, @Param("buyerId")String buyerId,
			@Param("notifyId") String notifyId, @Param("notifyTime") long notifyTime, @Param("status") int status, @Param("notifyStatus")int notifyStatus,
			@Param("updateTime") long updateTime, @Param("srcUpdateTime") long srcUpdateTime);

	public int updateNotifyStatus(@Param("billId") Long billId, @Param("notifyStatus") int notifyStatus,
			@Param("updateTime") long updateTime, @Param("srcUpdateTime") long srcUpdateTime);

	public int updateStatus(@Param("billId") Long billId, @Param("status") int status,
			@Param("updateTime") long updateTime, @Param("srcUpdateTime") long srcUpdateTime);

	public int updatePayRefundInfo(@Param("billId") Long billId, @Param("isRefund") int isRefund,
			@Param("refundAmount") Long refundAmount,
			@Param("updateTime") long updateTime, @Param("srcUpdateTime") long srcUpdateTime);
	
	
	public int insertRefundInfo(RefundInfoDaoBean refundInfoDaoBean);
	
	
	public RefundInfoDaoBean getRefundInfo(@Param("id")long id);
	
	public int updateRefundNotifyStatus(@Param("id")long id, @Param("notifyStatus")int notifyStatus, @Param("updateTime")long updateTime, @Param("srcUpdateTime")long srcUpdateTime);
	
	public int updateRefundStatus(@Param("id")long id, @Param("refundTradeNo")String refundTradeNo, 
			@Param("status")int status, @Param("notifyStatus")int notifyStatus, @Param("message")String message,
			@Param("updateTime")long updateTime, @Param("srcUpdateTime")long srcUpdateTime);

}