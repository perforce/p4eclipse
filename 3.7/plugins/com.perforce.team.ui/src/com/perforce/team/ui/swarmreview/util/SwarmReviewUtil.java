package com.perforce.team.ui.swarmreview.util;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReview;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReviewList;

public class SwarmReviewUtil {

	public static SwarmReviewList generateSwarmReviewListFromJSON(String jasonString){
		Gson gson = new Gson();
		SwarmReviewList swarmReview = gson.fromJson(jasonString, SwarmReviewList.class);
		
		return swarmReview;
	}
	
	public static SwarmReview generateSwarmReviewFromJSON(String jasonString){
		Gson gson = new Gson();
		SwarmReview swarmReview = gson.fromJson(jasonString, SwarmReview.class);
		
		return swarmReview;
	}
	
	public static void setStatus(String msg, boolean error){
    	IWorkbench wb = PlatformUI.getWorkbench();
    	IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

    	if(win != null){
	    	IWorkbenchPage page = win.getActivePage();
	
	    	IWorkbenchPart part = page.getActivePart();
	    	IWorkbenchPartSite site = part.getSite();
	
	    	IViewSite vSite = ( IViewSite ) site;
	
	    	IActionBars actionBars =  vSite.getActionBars();
	
	    	if( actionBars != null ){
	    		IStatusLineManager statusLineManager =actionBars.getStatusLineManager();
		    	if( statusLineManager != null ){
		    		statusLineManager.setMessage("");
		    		statusLineManager.setErrorMessage("");
		    		
		    		if(!error){
		    			statusLineManager.setMessage(msg == null ? "" : msg);
		    		}else{
		    			statusLineManager.setErrorMessage(msg == null ? "" : msg);
		    		}
		    	}
	    	}
    	}
    }
}
