package com.inno72.payment.service;

import com.inno72.common.Result;
import com.inno72.payment.common.TransException;
import com.inno72.payment.dto.ReqCreateBillBean;
import com.inno72.payment.dto.RspCreateBillBean;
import com.inno72.payment.model.PaySpInfoDaoBean;
import com.inno72.payment.model.ThirdPartnerInfo;

public interface ChannelService {

	public Result<RspCreateBillBean> createBill(long billId, String remoteIp, PaySpInfoDaoBean spInfo, ThirdPartnerInfo thirdPartnerInfo, ReqCreateBillBean reqBean) throws TransException;

}
