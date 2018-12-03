package com.inno72.payment.model;

import java.util.Date;

public class PaySpInfoDaoBean {
	
	private String id;
	
	private String name;
	
	private String thirdpartnerGroupId;
	
	private String signKey;
	
	private int status;
	
	private Date createTime;
	
	private Date updateTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThirdpartnerGroupId() {
		return thirdpartnerGroupId;
	}

	public void setThirdpartnerGroupId(String thirdpartnerGroupId) {
		this.thirdpartnerGroupId = thirdpartnerGroupId;
	}

	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
}
