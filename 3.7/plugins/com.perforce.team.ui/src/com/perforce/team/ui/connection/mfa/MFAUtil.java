package com.perforce.team.ui.connection.mfa;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.preferences.ExternalToolsPreferencePage;

public class MFAUtil {

	
	public static boolean doMFA(Shell shell, boolean canDoIt, IP4Connection conn){
		if(canDoIt){
			MFAAppRunner runner = new MFAAppRunner();
			final AuthResult<Integer> res = runner.run(conn, conn.getParameters().getPort(), conn.getParameters().getUser(), "", "");
				
			if(!res.isSuccessful()){
			
				if(res.getResultData() == MFARunStatus.APPNOTFOUND.getStatus()){
					if(MessageDialog.openConfirm(shell, "MFA App not installed", 
												res.getMessage() + "\nPress OK to configure it.")){
						PreferenceDialog pd = P4UIUtils.openPreferencePage(ExternalToolsPreferencePage.ID, true);
					    if(pd.getReturnCode() > -1){
					    	return res.isSuccessful();
					    }
					}
				} else{
					MessageDialog.openConfirm(shell, "Authentication failure", 
							res.getMessage());
							return res.isSuccessful();
				}
			}
				
		}
		return canDoIt;
	}
}

