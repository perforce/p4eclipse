package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.perforce.team.core.p4java.IP4Connection;

public class ResolveWizardAutoPage extends WizardPage implements IResolveControlContainer{

	private ResolveAutoControl control;
	private int changeId;
	private IP4Connection conn;

	protected ResolveWizardAutoPage(String pageName) {
        super(pageName);
        setTitle(Messages.ResolveWizardAutoPage_PageTitle);
        setDescription(Messages.ResolveWizardAutoPage_PageDescription);
    }
	
	public void setChangeId(int id){
		this.changeId = id;
	}
	
	public void setConn(IP4Connection conn){
		this.conn = conn;
	}

    public void createControl(Composite parent) {
        control = new ResolveAutoControl(parent, SWT.NONE, this, changeId, conn);
        setControl(control);
    }


    @Override
    public void setVisible(boolean b) {
    	control.init();
        super.setVisible(b);
    }

	public ResolveWizard getResolveWizard() {
		return (ResolveWizard) getWizard();
	}
}
