package com.perforce.team.ui.connection.mfa;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;

import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;


public class MFAuthenticator  {

	public String DEFAULT_APP_LOC = "/Users/tpethiyagoda/Downloads/HelixMFA.app/Contents/MacOS/HelixMFA";
	public String DEFAULT_APP_NAME = "HelixMFA";
	public static final int ERR_APP_NOT_FOUND_CODE = 404;
	private String port;
	private String user;
	private String mfaAppName;
	private String mfaAppLocation;
	private IP4Connection conn;
	private boolean forTesting;
	private IPreferenceStore prefStore = PerforceUIPlugin.getPlugin().getPreferenceStore();
	
	public MFAuthenticator(){
		DEFAULT_APP_LOC = prefStore.getString(IPreferenceConstants.P4MFA_APP_PATH);
	}
	
	
	public MFAuthenticator withPort(String port){
		this.port = port;
		return this;
	}
	
	public MFAuthenticator withUser(String user){
		this.user = user;
		return this;
	}
	
	public MFAuthenticator with2FAApp(String mfaAppName){
		this.mfaAppName = mfaAppName;
		if(this.mfaAppName == null || this.mfaAppName.isEmpty()){
			this.mfaAppName = DEFAULT_APP_NAME;
		}
		return this;
	}
	
	public MFAuthenticator with2FAAppLocatedAt(String mfaAppLocation){
		this.mfaAppLocation = mfaAppLocation;
		if(this.mfaAppLocation == null || this.mfaAppLocation.isEmpty()){
			this.mfaAppLocation = DEFAULT_APP_LOC;
		}
		return this;
	}
	
	public MFAuthenticator withConnection(IP4Connection conn){
		this.conn = conn;
		return this;
	}
	
	private boolean isAppExistsAtLocation(){
		if(this.mfaAppLocation == null || mfaAppLocation.trim().isEmpty()){
			return false;
		}
		return Files.exists(Paths.get(mfaAppLocation));
	}
	
	private AuthResult<Integer> validate(){
		AuthResult<Integer> result = new AuthResult<Integer>(true, 0);
		//MFA Not required does not mean its unsuccessful. it means login can still go ahead with just UN/PW authentication alone
		boolean mfaRequired =  isMFARequired();
		boolean appFound = isAppExistsAtLocation();
		
		if(!mfaRequired){
			return result.withSuccess("MFA not required", MFARunStatus.MFANOTREQUIRED.getStatus());
		}
				
		if(!appFound){
			return result.withFailure("Helix MFA Authenticator could not be found.", MFARunStatus.APPNOTFOUND.getStatus());
		}
		
		return result;
	}
	
	
	MFAuthenticator forTesting(){
		forTesting = true;
		return this;
	}
	
	/**
	 * If the server is MFA primed then this would return true
	 * If the server is not primed for MFA then this would return false
	 * If the server is MFA primed and the user is already validated this would return false
	 * @return
	 */
	private boolean isMFARequired(){
		try{
			//return getMFARequired();
			String isRequired = conn.getServer().getLogin2Status();
			if(isRequired.contains("required")){
				return true;
			} 
			if(isRequired.contains("validated")){
				return false;
			}
			
		}catch(Exception ex){
			return false;
		}		
		return false;
	}
	@Deprecated
	private boolean getMFARequired(){
		List<Map<String, Object>> resultMaps = null;
		try{
			Map<String, Object> pwdMap = new HashMap();
			pwdMap.put("password", conn.getParameters().getPassword() + "\n");
			resultMaps = conn.getServer().execMapCmdList("login2", new String[] {"-s"}, pwdMap);
			if(pwdMap != null){
				java.util.Iterator<Map<String, Object>> values = resultMaps.iterator();
				while(values.hasNext()){
					Map<String, Object> code = values.next();
					if(code.values().contains("required")){
						return true;
					}
					if(code.values().contains("validated")){
						return false;
					}
				}
			}
		}catch(Exception ex){
			return false;
		}		
		return false;
	}
	
	public AuthResult<Integer> authenticate() {
		int val = 0;
		AuthResult<Integer> result = new AuthResult<Integer>(true, val);
		AuthResult<Integer> validatedResult = validate();
		result = result.getClone(validatedResult);
		if(!forTesting && result.isSuccessful() && result.getResultData() != MFARunStatus.MFANOTREQUIRED.getStatus()){
			Process p = null;
			try{
				ProcessBuilder pb = new ProcessBuilder(mfaAppLocation, port, user);
				pb.inheritIO();
				p = pb.start();
				//p.waitFor(7000, TimeUnit.MILLISECONDS);
				p.waitFor();
				if(p != null){
					p.destroy();
				}
				val = p.exitValue();
				if(val == 0){
					result = result.withSuccess("Authentication successful", val);
				} else {
					result = result.withFailure("Authentication failed", val);
				}
			}catch(Exception ex){
				return result.withFailure(ex.getMessage(), 1);
			} 
		}
		return result;
	}
}
