package com.perforce.team.ui.connection.mfa;



public class AuthResult<T> {
	private boolean successful;
	private String message;
	private T resultData;
	private boolean error;
	private boolean reportInSilence;
	private Exception ex;
	
	public AuthResult(boolean successful, T resultData) {
		super();
		this.successful = successful;
		this. resultData = resultData;
	}
	
	public AuthResult(boolean successful, String message, T resultData) {
		super();
		this.successful = successful;
		this.message = message;
		this.resultData = resultData;
		
	}
	
	public AuthResult(AuthResult<T> res) {
		this.successful = res.successful;
		this.message = res.message;
		this.error = res.error;
		this.successful = res.successful;
		this.reportInSilence = res.reportInSilence;
		this.resultData = (T) res.resultData;
		
	}
	
	public AuthResult(AuthResult<?> res, T data) {
		this.successful = res.successful;
		this.message = res.message;
		this.error = res.error;
		this.successful = res.successful;
		this.reportInSilence = res.reportInSilence;
		this.resultData = data;
		
	}
	
	public boolean isSuccessful() {
		return successful;
	}
	
	public String getMessage() {
		return message;
	}
	public boolean isError(){
		return this.error;
	}

	public T getResultData() {
		return resultData;
	}

	public boolean isReportInSilence() {
		return reportInSilence;
	}
	
	public AuthResult<T> withFailure(String msg, T resultData){
		this.message = msg;
		this.error = true;
		this.successful = false;
		this.resultData = resultData;
		
		return this;
	}
	
	public AuthResult<T> withSuccess(String msg, T resultData){
		this.message = msg;
		this.error = false;
		this.successful = true;
		this.resultData = resultData;
		return this;
	}
	
	public AuthResult<T> getClone(AuthResult<T> resultToClone){
		this.message = resultToClone.message;
		this.error = resultToClone.error;
		this.successful = resultToClone.successful;
		this.resultData = resultToClone.resultData;
		
		return this;
	}
}
