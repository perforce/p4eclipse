package com.perforce.team.ui.swarmreview.restapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import com.perforce.team.ui.swarmreview.SSLValidator;

/**
 * A server connector that makes a HttpURLConnection available for a given URL 
 */
public final class APIConnector {
	private boolean isConnected;
	private HttpURLConnection connection;
	private boolean ignoreSSL;
	
	private APIConnector(){

	}

	static APIConnector getConnector(){
		return new APIConnector();
	}

	void connect(String serviceURL) throws MalformedURLException, IOException{
		URL url = new URL(serviceURL);
		connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10000);
		
		{
	      try {
	        if(this.connection instanceof HttpsURLConnection){
	          if(ignoreSSL){
	            this.connection = (HttpsURLConnection) connection;
	            SSLValidator.disableSSL((HttpsURLConnection)this.connection);
	          }
	        }
	      } catch (KeyManagementException ex) {
	        
	      } catch (NoSuchAlgorithmException ex) {
	        
	      } catch (KeyStoreException ex) {
	        
	      }
	    }
	}
	
	void setIgnoreSSLValidation(boolean ignoreSSL){
		this.ignoreSSL = ignoreSSL;
	}

    boolean isServiceAvailable() {
		return isConnected;
	}

	HttpURLConnection getConnection(){
		return connection;
	}
}
