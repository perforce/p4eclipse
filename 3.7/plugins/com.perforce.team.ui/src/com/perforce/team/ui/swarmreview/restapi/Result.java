package com.perforce.team.ui.swarmreview.restapi;

public class Result<T> {
	
	private boolean successful;
	private String message;
	private T resultData;
	private boolean error;
	private boolean reportInSilence;
	
	public Result(boolean successful) {
		super();
		this.successful = successful;
	}
	
	public Result(boolean successful, String message) {
		super();
		this.successful = successful;
		this.message = message;
		
	}
	
	public Result(Result<T> res) {
		this.successful = res.successful;
		this.message = res.message;
		this.error = res.error;
		this.successful = res.successful;
		this.reportInSilence = res.reportInSilence;
		this.resultData = (T) res.resultData;
	}
	
	public Result(Result<?> res, T data) {
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
	
	public Result<T> withSuccess(String msg, T data){
		this.message = msg;
		this.error = false;
		this.successful = true;
		this.resultData = data;
		this.error = false;
		return this;
	}
	
	public Result<T> withNoSuccess(String msg, boolean error, T data){
		this.message = msg;
		this.error = false;
		this.successful = false;
		this.resultData = data;
		this.error=error;
		return this;
	}
	
	public Result<T> withNoSuccess(String msg, T data){
		this.message = msg;
		this.error = false;
		this.successful = false;
		this.resultData = data;
		this.error=false;
		return this;
	}
	
	public Result<T> withNoSuccess(boolean reportInSilence, String msg, boolean error, T data){
		this.message = msg;
		this.successful = false;
		this.resultData = data;
		this.error=error;
		this.reportInSilence = reportInSilence;
		return this;
	}
	
	public Result<T> withValues(boolean successful, boolean reportInSilence, String msg, boolean error, T data){
		this.message = msg;
		this.successful = successful;
		this.resultData = data;
		this.error=error;
		this.reportInSilence = reportInSilence;
		return this;
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
}
