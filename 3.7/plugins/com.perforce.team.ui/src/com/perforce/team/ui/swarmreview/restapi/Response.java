package com.perforce.team.ui.swarmreview.restapi;

import javax.net.ssl.HttpsURLConnection;


/**
 * 
 * Generic Class that specifies T as the response body
 *  
 *
 * @param <T>
 */
public class Response<T> {

	private int responseCode;
	private T responseBody;
	private String responseMessage;


	public Response(int responseCode, String responseMessage, T responseBody){
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.responseBody = responseBody;
	}

	public boolean isNotAuthorized(){
		return responseCode == HttpsURLConnection.HTTP_NOT_AUTHORITATIVE;
	}

	public boolean isNotFound(){
		return responseCode == HttpsURLConnection.HTTP_NOT_FOUND;
	}

	public boolean isOK(){
		return responseCode == HttpsURLConnection.HTTP_OK;
	}

	public boolean isSuccessful() {
		return isOK();
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public T getResponseBody() {
		return responseBody;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String toString(){
		return responseCode
				+ ":"
				+ responseMessage
				+ ":"
				+ (getResponseBody() != null ? getResponseBody().toString() : "");
	}
}
