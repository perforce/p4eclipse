package com.perforce.team.ui.connection.mfa;

public enum MFARunStatus {

	APPFOUND(0), APPNOTFOUND(9999), MFANOTREQUIRED(1);
	
	MFARunStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return this.status;
	}
	private int status;
}
