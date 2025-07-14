/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.builder.ClientBuilder;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;
import com.perforce.team.ui.project.ImportProjectsWizardPage;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class AbstractConnectionWizard extends Wizard implements IConnectionWizard,
        INewWizard {

    /**
     * Server page
     */
    protected ServerWizardPage serverPage;

    /**
     * Client page
     */
    protected ClientWizardPage clientPage;

    /**
     * Import page
     */
    protected ImportProjectsWizardPage importPage;

    /**
     * Initial server port
     */
    protected String initialPort;

    /**
     * Initial user
     */
    protected String initialUser;

    /**
     * Initial client
     */
    protected String initialClient;

    /**
     * Initial charset
     */
    protected String initialCharset;

    /**
     * Current auth ticket
     */
    protected String authTicket;

    /**
     * Connection shared among pages.
     */
    private IP4Connection connection;
    
    private WizardDialog parentDialog;
    
    /**
     * Connection wizard
     */
    public AbstractConnectionWizard() {
        setNeedsProgressMonitor(true);
    }

    /**
     * Connection wizard
     * 
     * @param initial
     */
    public AbstractConnectionWizard(IP4Connection initial) {
        this();
        if (initial != null) {
            this.initialPort = initial.getParameters().getPort();
            this.initialUser = initial.getParameters().getUser();
//            this.initialClient = initial.getParameters().getClient();
            this.initialCharset = initial.getParameters().getCharset();
        }
    }

    public AbstractConnectionWizard(String initialPort, String initialUser,
            String initialClient, String initialCharset) {
        this();
        this.initialPort = initialPort;
        this.initialUser = initialUser;
        this.initialClient = initialClient;
        this.initialCharset = initialCharset;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        setWindowTitle(Messages.ConnectionWizard_PerforceConnectionWizard);
        if (this.initialPort != null && !this.initialPort.isEmpty()) {
            serverPage.setPort(this.initialPort);
        }
        if (this.initialUser != null && !this.initialUser.isEmpty()) {
            serverPage.setUser(this.initialUser);
        }
        if (this.initialClient != null && !this.initialClient.isEmpty()) {
            clientPage.setClient(this.initialClient);
        }
        if (this.initialCharset != null && !this.initialCharset.isEmpty()) {
            serverPage.setCharset(this.initialCharset);
        }
    }

//    /**
//     * @see org.eclipse.jface.wizard.Wizard#addPages()
//     */
//    @Override
//    public void addPages() {
//        serverPage = new ServerWizardPage("serverPage"); //$NON-NLS-1$
//        addPage(serverPage);
//        clientPage = new ClientWizardPage("clientPage"); //$NON-NLS-1$
//        addPage(clientPage);
//        importPage = new ImportProjectsWizardPage("importPage"); //$NON-NLS-1$
//        addPage(importPage);
//    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPort()
     */
    public String getPort() {
        return serverPage.getPort();
    }

    /**
     * Get the user for the server
     * 
     * @return - P4 user
     */
    public String getUser() {
        return serverPage.getUser();
    }

    /**
     * Get the client for the server
     * 
     * @return - P4 client workspace name
     */
    public String getClient() {
        return clientPage.getClient();
    }

    /**
     * Get the depot folders to import as eclipse projectsx
     * 
     * @return - p4 folders array
     */
    public IP4Folder[] getImportedFolders() {
    	if(importPage!=null)
    		return importPage.getImportedFolders();
    	return new IP4Folder[0];
    }

    /**
     * Import folder returned from {@link #getImportedFolders()} as projects
     * associated with the specified connection
     * 
     * @param connection
     */
    protected void importProjects(IP4Connection connection, final IProgressMonitor monitor) {

        IP4Folder[] imports = getImportedFolders();

        monitor.beginTask(Messages.AbstractConnectionWizard_ImportingProjects, imports.length+1);

        final List<IP4Container> retrievedFolders = new ArrayList<IP4Container>();
        for (IP4Folder folder : imports) {
        	monitor.subTask(MessageFormat.format(Messages.AbstractConnectionWizard_CheckoutFolder,folder.getRemotePath()));
            IP4Folder updated = connection.getFolder(folder.getRemotePath());
            if (updated != null) {
                retrievedFolders.add(updated);
            }
            monitor.worked(1);
        }

        if (retrievedFolders.size() > 0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    ImportProjectAction checkout = new ImportProjectAction();
                    checkout.selectionChanged(null, new StructuredSelection(
                            retrievedFolders));
                    checkout.runAction(monitor);
                }
            });
        }
        monitor.done();
    }

    private ConnectionParameters createParameters() {
        ConnectionParameters params = new ConnectionParameters();
        params.setPort(getPort());
        params.setClient(getClient());
        params.setUser(getUser());
        params.setCharset(getCharset());
        params.setPassword(getPassword());
        params.setSavePassword(serverPage.savePassword());
        params.setAuthTicket(getAuthTicket());
        return params;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final ConnectionParameters params = createParameters();
        PerforceUIPlugin.storePasswordInUI(params);

        final boolean newClient = isNewClient();
        final boolean[] finished = new boolean[] { true };
        final String stream=clientPage.getStream();

        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    monitor.beginTask(MessageFormat.format(
                            Messages.ConnectionWizard_AddingConnection,
                            params.getPort()), 40);

                    // Create client if needed
                    if (newClient) {
                        finished[0] = createClient(params,stream,monitor);
                        if (!finished[0]) {
                            monitor.done();
                            return;
                        }
                    } else {
                        monitor.worked(10);
                    }
                    saveConnection(params, monitor);

                    importProjects(params, monitor);
                    saveServerHistory(monitor);

                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            PerforceProviderPlugin.logError(e);
        } catch (InterruptedException e) {
            PerforceProviderPlugin.logError(e);
        }

        return finished[0];
    }

    private boolean createClient(ConnectionParameters params,String stream,
            IProgressMonitor monitor) {
        // Create client if needed
        monitor.subTask(NLS.bind(Messages.ConnectionWizard_CreatingClient,
                params.getClient()));
        ClientBuilder builder = new ClientBuilder(params,
                clientPage.getLocation(), stream);
        IErrorHandler handler = new ErrorHandler() {

            @Override
            public boolean shouldRetry(IP4Connection connection,
                    final P4JavaException exception) {
                boolean retry = false;
                if (P4ConnectionManager.isLoginError(exception.getMessage())) {
                    retry = P4ConnectionManager.getManager().displayException(
                            connection, exception, true, false);
                } else {
                    P4ConnectionManager.getManager().displayException(
                            connection, exception, false, false);
                }
                return retry;
            }

        };
        if (!builder.build(handler)) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    getContainer().showPage(clientPage);
                    P4ConnectionManager
                            .getManager()
                            .openError(
                                    getShell(),
                                    Messages.ConnectionWizard_ClientExistsTitle,
                                    NLS.bind(
                                            Messages.ConnectionWizard_ClientExistsMessage,
                                            getClient()));
                }
            });
            return false;
        }
        monitor.worked(1);
        return true;
    }

    private void saveConnection(ConnectionParameters params,
            IProgressMonitor monitor) {
        monitor.subTask(Messages.ConnectionWizard_SavingConnection);
        // Add connection
        P4ConnectionManager.getManager().add(params);
        monitor.worked(10);
    }

    private void importProjects(ConnectionParameters params,
            IProgressMonitor monitor) {
        // Import projects from new connection
        if (importPage!=null && importPage.isImportSelected()
                && P4ConnectionManager.getManager().containsConnection(params)) {
            monitor.subTask(Messages.ConnectionWizard_SettingUpProjectImport);
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(params);
            importProjects(connection,new SubProgressMonitor(monitor, 10));
        }else
        	monitor.worked(10);
    }

    private void saveServerHistory(IProgressMonitor monitor) {
        monitor.subTask(Messages.ConnectionWizard_SavingServerHistory);
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                serverPage.saveServerHistory();
            }
        });
        monitor.worked(10);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getCharset()
     */
    public String getCharset() {
        return serverPage.getCharset();
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPassword()
     */
    public String getPassword() {
        return serverPage.getPassword();
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getAuthTicket()
     */
    public String getAuthTicket() {
        return this.authTicket;
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#setAuthTicket(java.lang.String)
     */
    public void setAuthTicket(String authTicket) {
        this.authTicket = authTicket;
    }
    
    public String getStream(){
        if(isNewClient()){
            return clientPage.getStream();
        }else{
            IP4Connection conn = new P4Connection(createParameters());
            return conn.getClient().getStream();
        }
    }

    public boolean isNewClient() {
        boolean newClient = clientPage.isNewClientSelected();
        return newClient;
    }

	public IP4Connection getConnection() {
		return this.connection;
	}

	public void setConnection(IP4Connection conn) {
		this.connection=conn;
	}
	
	public void setParentDialog(WizardDialog dialog){
		this.parentDialog = dialog;
	}
	
	public void close(){
		if(this.parentDialog != null){
			this.parentDialog.close();
		}
	}
}
