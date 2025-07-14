/**
 * Copyright (c) 2016 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_Broker_Server;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_Cancel;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_Client;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_Connection;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_EnterPassword;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_EnterPasswordLabel;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_RememberPassword;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_Server;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_User;
import static com.perforce.team.ui.p4java.dialogs.Messages.PasswordDialog_WorkOffline;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.jface.dialogs.IDialogConstants.ABORT_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.CANCEL_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_ID;
import static org.eclipse.jface.dialogs.IDialogConstants.OK_LABEL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.PerforceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PasswordDialog extends PerforceDialog {
	private Text passwordControl;
	private Button saveCheck;
	private boolean showOffline = true;
	private IP4Connection connection;
	private String password = null;

	/**
	 * @wbp.parser.constructor
	 */
	public PasswordDialog(Shell parent, IP4Connection connection) {
		this(parent, connection, true);
	}

	public PasswordDialog(Shell parent, IP4Connection connection, boolean showOffline) {
		super(parent, PasswordDialog_EnterPassword);
		this.connection = connection;
		this.showOffline = showOffline;
		setModalResizeStyle();
	}

	public String getPassword() {
		return password;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, OK_ID, OK_LABEL, true);
		createButton(parent, CANCEL_ID, PasswordDialog_Cancel, false);
		if (showOffline) {
			Button goOffline = createButton(parent, ABORT_ID, PasswordDialog_WorkOffline, false);
			goOffline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					connection.setOffline(true);
					P4Workspace.getWorkspace().notifyListeners(new P4Event(EventType.CHANGED, connection));
					close();
				}
			});
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Group conSettings = createGroup(composite, PasswordDialog_Connection, 1);
		createLabel(conSettings, format(PasswordDialog_Server, connection.getAddress()));

		createBrokerServerLabelIfExist(conSettings);

		createLabel(conSettings, format(PasswordDialog_User, connection.getUser()));
		createLabel(conSettings, format(PasswordDialog_Client, connection.getClientName()));
		createLabel(composite, PasswordDialog_EnterPasswordLabel);

		passwordControl = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		passwordControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		passwordControl.setEchoChar('*');
		passwordControl.setFocus();

		saveCheck = createCheck(composite, PasswordDialog_RememberPassword);
		saveCheck.setSelection(connection.getParameters().savePassword());
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchHelpSystem helpSystem = workbench.getHelpSystem();
			if (helpSystem != null) {
				helpSystem.setHelp(saveCheck, IHelpContextIds.AUTH_REMEMBER_PASSWORD);
			}
		}

		return composite;
	}

	private void createBrokerServerLabelIfExist(Group parent) {
		IServerInfo serverInfo = connection.getServerInfo();
		if (serverInfo != null) {
			String brokerAddress = serverInfo.getBrokerAddress();
			if (isNotBlank(brokerAddress)) {
				createLabel(parent, format(PasswordDialog_Broker_Server, brokerAddress));
			}
		}
	}

	public Text getPasswordText() {
		return passwordControl;
	}

	@Override
	protected void okPressed() {
		updatePassword();
		boolean save = saveCheck.getSelection();
		connection.getParameters().setSavePassword(save);
		super.okPressed();
	}

	public void updatePassword() {
		password = passwordControl.getText();
	}
}
