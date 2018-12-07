package com.inno72.payment.service;

import com.inno72.common.Result;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.ReqRefundBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.dto.RspRefundBillBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfoDaoBean;

public interface ChannelService {

	public Result<RspCreateBillBean> createBill(long billId, String remoteIp, PaySpInfoDaoBean spInfo, ThirdPartnerInfoDaoBean thirdPartnerInfo, ReqCreateBillBean reqBean) throws TransException;
	
	
	public Result<RspRefundBillBean> refundBill(ReqRefundBillBean reqBean, PaySpInfoDaoBean spInfo, ThirdPartnerInfoDaoBean thirdPartnerInfo, String remoteIp) throws TransException;

}
