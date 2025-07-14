package com.perforce.team.ui.swarmreview.restapi;

public class APIException extends RuntimeException{

	private int errorCode;
	private String msg="";
	private Throwable ex;


	public APIException(String msg,Throwable ex, int errCode ){
		super(ex);
		this.errorCode = errCode;
		this.ex = ex;
		this.msg = msg + formatMessage(ex.getLocalizedMessage());
		
	}

	public int getErrorCode(){
		return errorCode;
	}
	
	public String getMessage(){
		return this.msg;
	}
	
	public String getLocalisedMessage(){
		return this.msg;
	}
	
	private String formatMessage(String msg){
		StringBuilder msgBuilder = new StringBuilder();
		if(msg != null && msg.indexOf("Exception") > -1){
			String formattedMsg  = msg.substring(msg.indexOf(":") + 1);
			msgBuilder.append(formattedMsg);
		} else{
			msgBuilder.append(" " + msg);
		}
		return msgBuilder.toString();
	}
}