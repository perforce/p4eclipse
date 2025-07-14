/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.AuthTicketsHelper;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.mfa.AuthResult;
import com.perforce.team.ui.connection.mfa.FactoredAuthFactory;
import com.perforce.team.ui.connection.mfa.MFAAppRunner;
import com.perforce.team.ui.connection.mfa.MFARunStatus;
import com.perforce.team.ui.connection.mfa.MFAUtil;
import com.perforce.team.ui.preferences.ExternalToolsPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerWizardPage extends BaseConnectionWizardPage {

    /**
     * RECENT_SERVER_LENGTH
     */
    public static final int RECENT_SERVER_LENGTH = 10;

    /**
     * SERVERS_SECTION
     */
    public static final String SERVERS_SECTION = "Servers"; //$NON-NLS-1$

    /**
     * RECENT_KEY
     */
    public static final String RECENT_KEY = "recent"; //$NON-NLS-1$
    
    public static final String IGNORE_SSL_KEY = "inoressl"; //$NON-NLS-1$

    /**
     * LEGACY_SECTION
     */
    public static final String LEGACY_SECTION = "NewConnection"; //$NON-NLS-1$

    private Composite displayArea;

    private Group locationGroup;
    private Label previousServersLabel;
    private Combo previousServersCombo;
    private Label serverLabel;
    private Text serverText;
    private Label charsetLabel;
    private Combo charsetCombo;

    private Group authGroup;
    private Label userNameLabel;
    private Text userNameText;
    private Label passwordLabel;
    private Text passwordText;
    private Button savePasswordButton;
    private Button ignoreSSLValidationButton;

    private String server = null;
    private String user = null;
    private String password = null;
    private boolean savePassword = false;
    private boolean ignoreSSLValidation=false;
    private String charset = null;
    
    


    private ModifyListener modify = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            validatePage();
        }
    };

    /**
     * @param pageName
     */
    public ServerWizardPage(String pageName) {
        super(pageName);
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_SHARE_WIZARD));
        setTitle(Messages.ServerWizardPage_AddConnectionTitle);
        setDescription(Messages.ServerWizardPage_AddConnectionMessage);
    }

    private void loadServerHistory() {
        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        String[] serverHistory = null;
        if (settings != null) {
            IDialogSettings section = settings.getSection(SERVERS_SECTION);
            if (section != null) {
                serverHistory = section.getArray(RECENT_KEY);
            } else {
                // Support for legacy connection dialog
                IDialogSettings oldSection = settings
                        .getSection(LEGACY_SECTION);
                if (oldSection != null) {
                    String[] oldHistory = oldSection.getArray(RECENT_KEY);
                    if (oldHistory != null) {
                        List<String> migratedHistory = new ArrayList<String>();
                        for (String old : oldHistory) {
                            ConnectionParameters params = new ConnectionParameters(
                                    old);
                            String port = params.getPort();
                            if (port != null && !migratedHistory.contains(port)) {
                                migratedHistory.add(port);
                            }
                        }
                        serverHistory = migratedHistory.toArray(new String[0]);
                    }
                }
            }
        }
        if (serverHistory != null && serverHistory.length > 0) {
            for (String entry : serverHistory) {
                if (entry != null && entry.trim().length() > 0) {
                    previousServersCombo.add(entry);
                }
            }
        }
    }

    /**
     * Save server history
     */
    public void saveServerHistory() {
        String currentServer = serverText.getText().trim();
        if (currentServer.isEmpty()) {
            return;
        }

        List<String> history = new ArrayList<String>(
                Arrays.asList(previousServersCombo.getItems()));
        for(Iterator<String> iter=history.iterator();iter.hasNext();){
        	if(iter.next().equals(currentServer))
        		iter.remove();
        }
        history.add(0, currentServer);
        if (history.size() > RECENT_SERVER_LENGTH) {
            history.remove(RECENT_SERVER_LENGTH);
        }

        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        if (settings != null) {
            IDialogSettings section = settings.getSection(SERVERS_SECTION);
            if (section == null) {
                section = settings.addNewSection(SERVERS_SECTION);
            }
            section.put(RECENT_KEY, history.toArray(new String[0]));
            section.put(IGNORE_SSL_KEY, this.ignoreSSLValidation);
            PerforceUIPlugin.getPlugin().saveSettings();
        }
    }

    private void createCharsetArea(Composite parent) {
        charsetLabel = new Label(parent, SWT.LEFT);
        charsetLabel.setText(Messages.ServerWizardPage_Charset);

        charsetCombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
        charsetCombo.setItems(P4UIUtils.getDisplayCharsets());
        charsetCombo.select(charsetCombo.indexOf("none")); //$NON-NLS-1$
        charsetCombo
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        charset = charsetCombo.getText();
        charsetCombo.addModifyListener(modify);
    }

    private void createLocationGroup(Composite parent) {
        locationGroup = new Group(parent, SWT.NONE);
        locationGroup.setText(Messages.ServerWizardPage_Location);
        locationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        locationGroup.setLayout(new GridLayout(2, false));

        previousServersLabel = new Label(locationGroup, SWT.LEFT);
        previousServersLabel.setText(Messages.ServerWizardPage_RecentServers);
        previousServersCombo = new Combo(locationGroup, SWT.READ_ONLY
                | SWT.DROP_DOWN);
        previousServersCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        loadServerHistory();
        previousServersCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                serverText.setText(previousServersCombo.getText());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        serverLabel = new Label(locationGroup, SWT.LEFT);
        serverLabel.setText(Messages.ServerWizardPage_Server);
        serverText = new Text(locationGroup, SWT.SINGLE | SWT.BORDER);
        serverText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        serverText.addModifyListener(modify);

        createCharsetArea(locationGroup);

    }

    private void createAuthGroup(Composite parent) {
    	System.setProperty(PerforceUIPlugin.IGNORE_SSL, Boolean.toString(ignoreSSLValidation));
        authGroup = new Group(parent, SWT.NONE);
        authGroup.setText(Messages.ServerWizardPage_Authentication);
        authGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        authGroup.setLayout(new GridLayout(2, false));

        userNameLabel = new Label(authGroup, SWT.NONE);
        userNameLabel.setText(Messages.ServerWizardPage_User);
        userNameText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
        userNameText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        userNameText.addModifyListener(modify);

        passwordLabel = new Label(authGroup, SWT.NONE);
        passwordLabel.setText(Messages.ServerWizardPage_Password);
        passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER
                | SWT.PASSWORD);
        passwordText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        passwordText.addModifyListener(modify);

        savePasswordButton = new Button(authGroup, SWT.CHECK);
        ignoreSSLValidationButton = new Button(authGroup, SWT.CHECK);
        ignoreSSLValidationButton.setText(Messages.ServerWizardPage_IgnoreSSLValidation);
        savePasswordButton.setText(Messages.ServerWizardPage_SavePassword);
        GridData spbData = new GridData(SWT.FILL, SWT.FILL, true, false);
        spbData.horizontalSpan = 2;
        savePasswordButton.setLayoutData(spbData);
        savePasswordButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                savePassword = savePasswordButton.getSelection();
                validatePage();
            }

        });
        
        ignoreSSLValidationButton.setLayoutData(spbData);
        ignoreSSLValidationButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	ignoreSSLValidation = ignoreSSLValidationButton.getSelection();
            	System.setProperty(PerforceUIPlugin.IGNORE_SSL, Boolean.toString(ignoreSSLValidation));
                validatePage();
            }

        });
    }
    
    @Override
	public IWizardPage getNextPage() {
    	boolean doit = preNextPage();
    	
    	if(!doit){
    		return super.getPreviousPage();
    	}
    	return super.getNextPage();
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
	public void createControl(Composite parent) {
		displayArea = new Composite(parent, SWT.NONE);
		displayArea.setLayout(new GridLayout(1, true));
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createLocationGroup(displayArea);
		createAuthGroup(displayArea);

		setControl(displayArea);
		setPageComplete(false);
    }
	
	private boolean preNextPage(){
		final AtomicBoolean doit = new AtomicBoolean(false);
		if (getContainer() instanceof WizardDialog) {
			WizardDialog wd = (WizardDialog) getContainer();
			
			final String[] messages=new String[1];
			
			try {
				doit.set(canDoNext(null));
			} catch (Exception e) {
				e.printStackTrace();
				doit.set(false);
			}
			if(doit.get() == false){
				setErrorMessage(messages[0]);
				getContainer().updateButtons();
			}
		}
		return doit.get();
	}

	
	public boolean canDoNext(IProgressMonitor monitor1) throws InvocationTargetException, InterruptedException {
		String[] messages=new String[1];
		//monitor.beginTask(Messages.ServerWizardPage_TestingConnection, IProgressMonitor.UNKNOWN);
		boolean doit=false;
		IP4Connection conn =null;
		try {
			ConnectionParameters params = new ConnectionParameters();
			params.setPort(getPort());
			params.setClient(getClient());
			params.setUser(getUser());
			params.setCharset(getCharset());
			params.setPassword(getPassword());
			params.setSavePassword(savePassword());
			params.setIgnoreSSLValidation(ignoreSSLValidation);
			IErrorHandler handler = new ErrorHandler() {
				@Override
				public boolean shouldRetry(IP4Connection connection,
						final P4JavaException exception) {
					boolean retry=P4ConnectionManager.getManager().displayException(
							connection, exception, false, false);
					return retry;
				}
			};
			conn = P4Workspace.getWorkspace().tryToConnect(params,handler);
			
			IServerInfo info = conn.getServer().getServerInfo();
			String ticket = null;
			try {
				ticket = AuthTicketsHelper.getTicketValue(ConnectionParameters.getTicketUser(params, conn.getServer()),info.getServerAddress(),
						P4Connection.getP4TicketsOSLocation());
			} catch (Throwable t) {
				PerforceProviderPlugin.logWarning(t);
			}
			
	        // Attempt to login with HAS
			if (conn.getServerInfo().isSSOAuthRequired()) {
	        	boolean loginOK = conn.login("");
	        	return loginOK;
	        }
			
			if(ticket!=null && params.getPasswordNoNull().isEmpty()){
				// try to use authticket
				conn.getServer().setAuthTicket(ticket);
				doit=true;
			}else{
				// try login with password
				try {
					conn.getServer().login(params.getPassword());
					doit=true;
				} catch(Throwable t) {
					//Fix for Jira ticket #P4E-2200
					String[] options =  new String[] { IDialogConstants.OK_LABEL };
					MessageDialog errdlg = new MessageDialog(getShell(), Messages.ServerWizardPage_ErrorAuthenticatingUserTitle, null,
							 			Messages.ServerWizardPage_ErrorAuthenticatingUserMessage, MessageDialog.ERROR, options, 0);
					/*
					 * Note: The following condition is used in testing mode so that the error dialog may not get blocked and 
					 * the subsequent checks in the test can execute.
					 */
					if(System.getProperty("isTestMode") != null)
						errdlg.setBlockOnOpen(false);
					
					errdlg.open();	
					throw t;
				}
			}
			doit = MFAUtil.doMFA(ServerWizardPage.this.getShell(), doit, conn);
			if(doit){
				setWizardConnection(conn);
			}
			
		} catch (Throwable t) {
			messages[0]=t.getLocalizedMessage();    
			PerforceProviderPlugin.logError(t);
			if(conn!=null){
				conn.dispose();
			}
			doit=false;
		}
		//monitor.done();
		return doit;
	}
	

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getPort()
     */
    @Override
    public String getPort() {
    	if(this.server != null && this.server.startsWith("rsh") && !this.server.endsWith(" --java")){
    		this.server= this.server + " --java";
    	}
        return this.server;
    }

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getUser()
     */
    @Override
    public String getUser() {
        return this.user;
    }

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getPassword()
     */
    @Override
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public boolean isIgnoreSSLValidation() {
        return this.ignoreSSLValidation;
    }

    /**
     * Set the port field
     * 
     * @param port
     */
    public void setPort(String port) {
        if (port != null) {
            this.serverText.setText(port);
        }
    }

    /**
     * Set the user field
     * 
     * @param user
     */
    public void setUser(String user) {
        if (user != null) {
            this.userNameText.setText(user);
        }
    }
    
    public void setPassword(String pw){
    	if (pw != null) {
            this.passwordText.setText(pw);
        }
    }

    private void validatePage() {
        this.server = serverText.getText().trim();
        this.user = userNameText.getText().trim();
        this.charset = P4UIUtils.getP4Charset(charsetCombo.getText());
        this.password = passwordText.getText();
        if (password.isEmpty()) {
            this.password = null;
        }
        
        String message = null;
        if (!(this.server.matches("(ssl:)?[^:]+:[1-9]\\d*") || this.server.matches("((rsh)?(:{1})[^:]+[a-zA-Z:]?.*)"))) { //$NON-NLS-1$
            message = Messages.ServerWizardPage_MustSpecifyServer;
        }
        else if (this.user.isEmpty()) {
            message = Messages.ServerWizardPage_MustSpecifyUser;
        }

        setPageComplete(message == null);
        setErrorMessage(message);
        if(getContainer().getCurrentPage()!=null)
        	getContainer().updateButtons();
    }

    /**
     * Is the password configured to be saved?
     * 
     * @return - true to save password
     */
    public boolean savePassword() {
        return this.savePassword;
    }

    /**
     * Set the charset name. The specified charset should a p4 charset name from
     * {@link CharSetApi}. This method will convert it to a displayable charset
     * label before setting the text of the charset combo.
     * 
     * @param charset
     */
    public void setCharset(String charset) {
        if (charset != null) {
            this.charsetCombo.setText(P4UIUtils.getDisplayCharset(charset));
            validatePage();
        }
    }

    /**
     * Get selected charset
     * 
     * @return - charset chosen
     */
    @Override
    public String getCharset() {
        return this.charset;
    }

    @Override
    public boolean canFlipToNextPage() {
    	return super.canFlipToNextPage() && getErrorMessage()==null;
    }
}
