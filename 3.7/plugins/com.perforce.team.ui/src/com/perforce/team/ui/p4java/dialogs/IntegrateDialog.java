/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import static com.perforce.team.ui.p4java.dialogs.Messages.getString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.branches.SelectBranchDialog;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.changelists.PendingCombo.PendingComboSelectionAdapter;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.resource.ResourceBrowserDialog;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateDialog extends P4StatusDialog {
	/**
	 * TRY_AUTO_RESOLVE
	 */
	public static final String TRY_AUTO_RESOLVE = IPreferenceConstants.PREFIX + "TRY_AUTO_RESOLVE"; //$NON-NLS-1$

	private Composite displayArea;
	private TabFolder specTabs;

	private TabItem fileTab;
	private Combo sourceText;
	private Combo targetText;

	private Button attemptSafeResolveButton;

	private TabItem branchTab;
	private Combo nameText;
	private Button limitButton;
	private Text limitText;
	private Button sourceButton;
	private Button targetButton;

	private Group limitArea;
	private Button startButton;
	private Button endButton;
	private Combo startCombo;
	private Combo endCombo;
	private Text startText;
	private Text endText;
	private Label startHelp;
	private Label endHelp;

	private PendingCombo changelistCombo;
	private Label progressLabel;
	private ProgressBar progressBar;

	private Button preview;
	private Button integrate;

	private boolean fileIntegration = true;
	private boolean isAddPreviouslyLinkedJobs = false;
	private IP4Resource resource = null;
	private String sourcePath = null;
	private String targetPath = null;
	private String startOption = null;
	private String branchName = null;
	private String endOption = null;

	private Button addPreviouslyLinkedJobButton;
	private Label linkedJobDisabledPrompt;

	private ModifyListener validateListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validate();
		}
	};

	private ModifyListener pathListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updatePaths();
		}
	};

	private IP4SubmittedChangelist defaultLimit = null;
	private P4IntegrationOptions defaultOptions;
	private P4IntegrationOptions integrationOptions;

	private IntegOptionWidget optionWidget = IntegOptionWidget.INTEG3; // default
																		// integ
																		// 3

	/**
	 * Creates an integration dialog
	 *
	 * @param parent
	 * @param resource
	 * @wbp.parser.constructor
	 */
	public IntegrateDialog(Shell parent, IP4Resource resource) {
		super(parent, getString("IntegrateDialog_Integrate"));
		setStatusLineAboveButtons(true);
		setModalResizeStyle();
		this.resource = resource;
		if (this.resource instanceof IP4Branch) {
			this.branchName = this.resource.getName();
		} else {
			this.sourcePath = this.resource.getActionPath();
			this.targetPath = this.resource.getActionPath();
		}
		initOptionWidget(resource);
	}

	/**
	 * Creates an integrate dialog opened against a specified connection with initial source and target path values
	 *
	 * @param parent
	 * @param connection
	 * @param sourcePath
	 * @param targetPath
	 */
	public IntegrateDialog(Shell parent, IP4Connection connection, String sourcePath, String targetPath) {
		super(parent, getString("IntegrateDialog_Integrate"));
		setStatusLineAboveButtons(true);
		setModalResizeStyle();
		this.resource = connection;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;

		initOptionWidget(resource);
	}

	private void initOptionWidget(IP4Resource resource) {
		optionWidget = createWidget(resource);
		integrationOptions = optionWidget.getDefaultIntegrationOptions();
	}

	private IntegOptionWidget createWidget(IP4Resource resource) {
		IntegOptionWidget widget = null;
		IP4Connection connection = resource.getConnection();
		IServer server = connection.getServer();
		String stream = connection.getClient().getStream();
		if (isNotBlank(stream)) {
			widget = IntegOptionWidget.INTEG3;
		} else {
			int serverVer = server.getServerVersionNumber();
			String level = null;
			try {
				IServerInfo info = server.getServerInfo();
				level = info.getIntegEngine();
			} catch (Exception e) {
				PerforceProviderPlugin.logError(e.getLocalizedMessage());
				PerforceUIPlugin.syncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, getString("MessageDialog.error.connection.title"), getString("MessageDialog.error.connection.msg"));
					}
				});
			}
			if (level == null) {
				if (serverVer < IP4ServerConstants.INTEG3_SERVERID_VERSION) {
					widget = IntegOptionWidget.INTEG2; // older server default
														// to integEngine 2
				} else {
					widget = IntegOptionWidget.INTEG3; // newer server default
														// to integEngine 3
				}
			} else {
				widget = IntegOptionWidget.valueOf("INTEG" + level.trim());
			}
		}
		return widget;
	}

	public P4IntegrationOptions getDefaultOptions() {
		return defaultOptions;
	}

	public void setDefaultEnabledOptions(P4IntegrationOptions options) {
		this.defaultOptions = options;
	}

	public P4FileIntegration getCurrentFileIntegrationInfo() {
		P4FileIntegration integration = new P4FileIntegration();
		integration.setEnd(endOption);
		integration.setStart(startOption);
		integration.setSource(sourcePath);
		integration.setTarget(targetPath);
		return integration;
	}

	public boolean isFileIntegration() {
		return fileIntegration;
	}

	public boolean isAddPreviouslyLinkedJobs() {
		return isAddPreviouslyLinkedJobs;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		preview = createButton(parent, IDialogConstants.DETAILS_ID, getString("IntegrateDialog_Preview"), false);
		preview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateHistory();
				runPreview(getChangeListDescription());
			}
		});
		integrate = createButton(parent, IDialogConstants.OK_ID, getString("IntegrateDialog_Integrate"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, getString("IntegrateDialog_Cancel"), false);
	}

	private void runPreview(String description) {
		final int changelist = getPendingChangelistId();
		final IP4Connection connection = resource.getConnection();
		final String name = branchName;
		updateOptions();
		final P4IntegrationOptions options = integrationOptions;
		final P4FileIntegration integration = getCurrentFileIntegrationInfo();
		startPreview();
		P4Runner.schedule(new P4Runnable() {
			@Override
			public String getTitle() {
				return getString("IntegrateDialog_GeneratingIntegPreview");
			}

			@Override
			public void run(IProgressMonitor monitor) {
				final IP4Resource[] previewed = connection.integratePreview(integration, name, changelist, "", true, options, false);
				PerforceUIPlugin.syncExec(new Runnable() {
					public void run() {
						if (!displayArea.isDisposed()) {
							stopPreview();
							new IntegrationPreviewDialog(getShell(), previewed).open();
						}
					}
				});
			}
		});
	}

	public int getPendingChangelistId() {
		return changelistCombo.getSelected();
	}

	private void startPreview() {
		progressBar.setEnabled(true);
		progressBar.setVisible(true);
		progressLabel.setText(getString("IntegrateDialog_GeneratingIntegPreview"));
		preview.setEnabled(false);
	}

	private void stopPreview() {
		progressBar.setEnabled(false);
		progressBar.setVisible(false);
		progressLabel.setText(""); //$NON-NLS-1$
		preview.setEnabled(true);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		displayArea = new Composite(container, SWT.NONE);
		GridLayout daLayout = new GridLayout(1, true);
		displayArea.setLayout(daLayout);
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		specTabs = new TabFolder(displayArea, SWT.TOP);
		specTabs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePaths();
				validate();
			}
		});
		specTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (resource instanceof IP4SubmittedChangelist) {
			setDefaultLimitOnIntegration((IP4SubmittedChangelist) resource);
		}

		createFileArea(specTabs);
		createBranchArea(specTabs);
		createLimitArea(displayArea);

		createOptionsArea(displayArea);
		createChangelistArea(displayArea);

		if (resource instanceof IP4Branch) {
			specTabs.setSelection(branchTab);
		} else {
			if (isNotBlank(sourcePath) && isNotBlank(targetPath)) {
				specTabs.setSelection(fileTab);
			} else {
				specTabs.setSelection(branchTab);
			}
		}

		specTabs.notifyListeners(SWT.Selection, null);

		validate();

		return container;
	}

	public void setDefaultLimitOnIntegration(IP4SubmittedChangelist changelist) {
		this.defaultLimit = changelist;
	}

	private void createFileArea(final TabFolder folder) {
		fileTab = new TabItem(folder, SWT.NONE);
		fileTab.setText(getString("IntegrateDialog_FileSpec"));
		Composite filePathArea = new Composite(folder, SWT.NONE);
		fileTab.setControl(filePathArea);
		GridLayout fpaLayout = new GridLayout(3, false);
		filePathArea.setLayout(fpaLayout);
		filePathArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label sourceLabel = new Label(filePathArea, SWT.LEFT);
		sourceLabel.setText(getString("IntegrateDialog_SourceLabel"));

		sourceText = new Combo(filePathArea, SWT.DROP_DOWN);
		SessionManager.loadComboHistory(sourceText, IPreferenceConstants.SOURCE_FILE_HISTORY);
		sourceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (isNotBlank(sourcePath)) {
			sourceText.setText(sourcePath);
		}
		sourceText.addModifyListener(pathListener);
		sourceText.addModifyListener(validateListener);

		createBrowseForFileButton(filePathArea, sourceText);

		Label targetLabel = new Label(filePathArea, SWT.LEFT);
		targetLabel.setText(getString("IntegrateDialog_TargetLabel"));

		targetText = new Combo(filePathArea, SWT.SINGLE | SWT.BORDER);
		SessionManager.loadComboHistory(targetText, IPreferenceConstants.TARGET_FILE_HISTORY);
		targetText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (isNotBlank(targetPath)) {
			targetText.setText(targetPath);
		}
		targetText.addModifyListener(pathListener);
		targetText.addModifyListener(validateListener);
		createBrowseForFileButton(filePathArea, targetText);
		new Label(filePathArea, SWT.NONE);
	}

	private void createBrowseForFileButton(Composite parent, final Object update) {
		final Button browseSource = new Button(parent, SWT.PUSH);
		browseSource.setText(getString("IntegrateDialog_Browse"));
		browseSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IP4Connection conn = resource.getConnection();
				boolean showClientOnly = conn.showClientOnly();
				try {
					// set the showClientOnly to false, so that we can show all
					// resources.
					conn.setShowClientOnly(false);

					ResourceBrowserDialog dialog = new ResourceBrowserDialog(browseSource.getShell(), conn.members());
					if (dialog.open() == ResourceBrowserDialog.OK) {
						IP4Resource resource = dialog.getSelectedResource();
						if (resource != null) {
							String actionPath = resource.getActionPath(Type.REMOTE);
							if (actionPath != null) {
								if (update instanceof Combo) {
									((Combo) update).setText(actionPath);
								} else if (update instanceof Text) {
									((Text) update).setText(actionPath);
								}
							}
						}
					}
				} finally {
					// restore the showClientOnly
					conn.setShowClientOnly(showClientOnly);
				}
			}
		});
	}

	private void createBranchArea(final TabFolder folder) {
		branchTab = new TabItem(folder, SWT.NONE);
		branchTab.setText(getString("IntegrateDialog_BranchSpec"));
		Composite branchArea = new Composite(folder, SWT.NONE);
		branchTab.setControl(branchArea);
		GridLayout baLayout = new GridLayout(3, false);
		branchArea.setLayout(baLayout);
		branchArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label nameLabel = new Label(branchArea, SWT.LEFT);
		nameLabel.setText(getString("IntegrateDialog_BranchName"));
		nameText = new Combo(branchArea, SWT.DROP_DOWN);
		SessionManager.loadComboHistory(nameText, IPreferenceConstants.BRANCH_HISTORY);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (branchName != null) {
			nameText.setText(branchName);
		}
		nameText.addModifyListener(pathListener);
		nameText.addModifyListener(validateListener);

		final Button browseButton = new Button(branchArea, SWT.PUSH);
		browseButton.setText(getString("IntegrateDialog_Browse"));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectBranchDialog dialog = new SelectBranchDialog(browseButton.getShell(), resource.getConnection());
				if (SelectBranchDialog.OK == dialog.open()) {
					IP4Branch selected = dialog.getSelected();
					if (selected != null && selected.getName() != null) {
						nameText.setText(selected.getName());
						validate();
					}
				}
			}
		});

		Composite limitArea = new Composite(branchArea, SWT.NONE);
		GridData laData = new GridData(SWT.FILL, SWT.FILL, true, false);
		laData.horizontalSpan = 3;
		limitArea.setLayoutData(laData);
		GridLayout laLayout = new GridLayout(4, false);
		limitArea.setLayout(laLayout);

		limitButton = new Button(limitArea, SWT.CHECK);
		limitButton.setText(getString("IntegrateDialog_LimitIntegTo"));
		limitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = limitButton.getSelection();
				limitText.setEnabled(enabled);
				targetButton.setEnabled(enabled);
				sourceButton.setEnabled(enabled);
				optionWidget.enableReverseButton(!enabled);
			}
		});
		limitText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
		limitText.setEnabled(false);
		GridData ltData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		ltData.horizontalSpan = 2;
		limitText.setLayoutData(ltData);

		createBrowseForFileButton(limitArea, limitText);

		Label usePathLabel = new Label(limitArea, SWT.LEFT);
		usePathLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		usePathLabel.setText(getString("IntegrateDialog_UsePathAs"));

		SelectionListener limitListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePaths();
			}
		};

		limitText.addModifyListener(pathListener);
		limitButton.addSelectionListener(limitListener);

		sourceButton = new Button(limitArea, SWT.RADIO);
		sourceButton.setText(getString("IntegrateDialog_Source"));
		sourceButton.setEnabled(false);
		sourceButton.setSelection(true);
		sourceButton.addSelectionListener(limitListener);

		targetButton = new Button(limitArea, SWT.RADIO);
		targetButton.setText(getString("IntegrateDialog_Target"));
		targetButton.setEnabled(false);
		new Label(limitArea, SWT.NONE);
		new Label(limitArea, SWT.NONE);
		targetButton.addSelectionListener(limitListener);
	}

	private void validate() {
		setErrorMessage(findFirstError(), null);
	}

	private String findFirstError() {
		TabItem[] tabs = specTabs.getSelection();
		String errorMessage = null;
		if (tabs.length == 1) {
			if (tabs[0] == fileTab) {
				if (isBlank(sourceText.getText())) {
					errorMessage = getString("IntegrateDialog_MustEnterSourcePath");
				}
				if (isBlank(targetText.getText())) {
					errorMessage = getString("IntegrateDialog_MustEnterTargetPath");
				}
			} else if (tabs[0] == branchTab) {
				if (isBlank(nameText.getText())) {
					errorMessage = getString("IntegrateDialog_MustEnterBranchName");
				}
			}
		}
		return errorMessage;
	}

	/**
	 * Update the source, target, and branch values
	 */
	public void updatePaths() {
		TabItem[] tabs = specTabs.getSelection();
		if (tabs.length == 1) {
			if (tabs[0] == branchTab) {
				branchName = nameText.getText();
				if (limitButton.getSelection()) {
					if (sourceButton.getSelection()) {
						String limitSource = limitText.getText();
						if (isNotEmpty(limitSource)) {
							sourcePath = limitSource;
						} else {
							sourcePath = null;
						}
						targetPath = null;
					} else {
						sourcePath = IP4Connection.ROOT;
						String limitTarget = limitText.getText();
						if (isNotEmpty(limitTarget)) {
							targetPath = limitTarget;
						} else {
							targetPath = null;
						}
					}
				} else {
					targetPath = null;
					sourcePath = null;
				}
			} else if (tabs[0] == fileTab) {
				branchName = null;
				sourcePath = sourceText.getText();
				targetPath = targetText.getText();
			}
		}
	}

	private void createLimitArea(Composite parent) {
		limitArea = new Group(parent, SWT.NONE);
		limitArea.setText(getString("IntegrateDialog_LimitRevRange"));
		limitArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout laLayout = new GridLayout(4, false);
		limitArea.setLayout(laLayout);

		startButton = new Button(limitArea, SWT.CHECK);
		startButton.setText(getString("IntegrateDialog_Start"));
		startButton.setEnabled(false);
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = startButton.getSelection();
				startCombo.setEnabled(enabled);
				startText.setEnabled(enabled);
				startHelp.setVisible(enabled);
				if (!enabled) {
					startOption = null;
				}
			}
		});

		startCombo = new Combo(limitArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		startCombo.add(getString("IntegrateDialog_Revision"));
		startCombo.add(getString("IntegrateDialog_Changelist"));
		startCombo.add(getString("IntegrateDialog_Label"));
		startCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String prefix = getPrefix(startCombo.getSelectionIndex());
				startOption = prefix + startText.getText();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		startCombo.setEnabled(false);

		startText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
		startText.setEnabled(false);
		startText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String prefix = getPrefix(startCombo.getSelectionIndex());
				startOption = prefix + startText.getText();
			}
		});
		startHelp = new Label(limitArea, SWT.LEFT);
		startHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addComboListener(startCombo, startHelp);

		endButton = new Button(limitArea, SWT.CHECK);
		endButton.setText(getString("IntegrateDialog_End"));
		endButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = endButton.getSelection();
				endCombo.setEnabled(enabled);
				endText.setEnabled(enabled);
				startButton.setEnabled(enabled);
				startCombo.setEnabled(enabled);
				startText.setEnabled(enabled);
				if (!enabled) {
					startButton.setSelection(false);
					endOption = null;
					startOption = null;
				}
			}
		});

		endCombo = new Combo(limitArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		endCombo.add(getString("IntegrateDialog_Revision"));
		endCombo.add(getString("IntegrateDialog_Changelist"));
		endCombo.add(getString("IntegrateDialog_Label"));
		endCombo.setEnabled(false);
		endCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String prefix = getPrefix(endCombo.getSelectionIndex());
				endOption = prefix + endText.getText();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		endText = new Text(limitArea, SWT.SINGLE | SWT.BORDER);
		endText.setEnabled(false);
		endText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String prefix = getPrefix(endCombo.getSelectionIndex());
				endOption = prefix + endText.getText();
			}
		});

		endHelp = new Label(limitArea, SWT.LEFT);
		endHelp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addComboListener(endCombo, endHelp);

		if (this.defaultLimit != null) {
			startCombo.select(1);
			endCombo.select(1);
			startHelp.setText(getString("IntegrateDialog_EnterChangelistNumber"));
			endHelp.setText(getString("IntegrateDialog_EnterChangelistNumber"));
			startButton.setEnabled(true);
			startButton.setSelection(true);
			endButton.setSelection(true);
			startCombo.setEnabled(true);
			endCombo.setEnabled(true);
			startText.setEnabled(true);
			endText.setEnabled(true);
			int id = this.defaultLimit.getId();
			startText.setText(Integer.toString(id));
			endText.setText(Integer.toString(id));
		} else {
			startCombo.select(0);
			endCombo.select(0);
			startHelp.setText(getString("IntegrateDialog_EnterRevisionNumber"));
			endHelp.setText(getString("IntegrateDialog_EnterRevisionNumber"));
		}
	}

	private String getPrefix(int index) {
		String prefix = null;
		switch (index) {
		case 0:
			prefix = "#"; //$NON-NLS-1$
			break;
		case 1:
		case 2:
			prefix = "@"; //$NON-NLS-1$
			break;
		default:
			prefix = ""; //$NON-NLS-1$
			break;
		}
		return prefix;
	}

	private void addComboListener(final Combo combo, final Label help) {
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = combo.getText();
				if (text.equals(getString("IntegrateDialog_Revision"))) {
					help.setText(getString("IntegrateDialog_EnterRevisionNumber"));
				} else if (text.equals(getString("IntegrateDialog_Changelist"))) {
					help.setText(getString("IntegrateDialog_EnterChangelistNumber"));
				} else if (text.equals(getString("IntegrateDialog_Label"))) {
					help.setText(getString("IntegrateDialog_EnterLabel"));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

	private void createOptionsArea(Composite parent) {
		optionWidget.createOptionsArea(parent);

		specTabs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem[] tabs = specTabs.getSelection();
				optionWidget.enableReverseButton(tabs.length == 1 && tabs[0] == branchTab);
			}
		});

		attemptSafeResolveButton = new Button(parent, SWT.CHECK);
		attemptSafeResolveButton.setText(getString("IntegrateDialog_PerformSafeAutoresolve"));
		attemptSafeResolveButton.setSelection(PerforceUIPlugin.getPlugin().getPreferenceStore().getBoolean(TRY_AUTO_RESOLVE));

		optionWidget.initControl(defaultOptions);
	}

	private void createChangelistArea(Composite parent) {
		changelistCombo = new PendingCombo(this.resource);
		changelistCombo.createControl(parent, IP4Connection.INTEGRATE_DEFAULT_DESCRIPTION);

		PendingComboSelectionAdapter defaultPendingComboSelectionAdapter = changelistCombo.getPendingComboSelectionAdapter();
		changelistCombo.bindPendingComboSelectionAdapter(changelistCombo.new PendingComboSelectionAdapter(defaultPendingComboSelectionAdapter) {
			@Override
			protected void callbackIfIsNewChangeList() {
				linkedJobDisabledPrompt.setVisible(false);
				addPreviouslyLinkedJobButton.setEnabled(true);
				addPreviouslyLinkedJobButton.setSelection(false);
			}

			@Override
			protected void callbackIfIsDefaultChangeList() {
				linkedJobDisabledPrompt.setVisible(true);
				addPreviouslyLinkedJobButton.setEnabled(false);
				addPreviouslyLinkedJobButton.setSelection(false);
			}

			@Override
			protected void callbackIfIsNormalChangeList() {
				linkedJobDisabledPrompt.setVisible(false);
				addPreviouslyLinkedJobButton.setEnabled(true);
				addPreviouslyLinkedJobButton.setSelection(false);
			}
		});

		createAddPreviouslyLinkedJobArea(displayArea);

		progressLabel = new Label(displayArea, SWT.LEFT);
		progressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		progressBar = new ProgressBar(displayArea, SWT.INDETERMINATE | SWT.SMOOTH | SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		progressBar.setEnabled(false);
		progressBar.setVisible(false);
	}

	private void createAddPreviouslyLinkedJobArea(Composite parent) {
		Composite panel = new Composite(parent, SWT.BOTTOM);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		panel.setLayout(new GridLayout(1, true));
		addPreviouslyLinkedJobButton = new Button(panel, SWT.CHECK);
		addPreviouslyLinkedJobButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		addPreviouslyLinkedJobButton.setText(getString("IntegrateDialog_AddPreviouseLinkeJob"));
		addPreviouslyLinkedJobButton.setEnabled(false);
		addPreviouslyLinkedJobButton.setSelection(false);
		addPreviouslyLinkedJobButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				linkedJobDisabledPrompt.setVisible(!addPreviouslyLinkedJobButton.getSelection());
				isAddPreviouslyLinkedJobs = addPreviouslyLinkedJobButton.getSelection();
			}
		});

		linkedJobDisabledPrompt = new Label(panel, SWT.BOTTOM);
		linkedJobDisabledPrompt.setText(getString("IntegrateDialog_AddPreviouseLinkeJob_disabled"));
		linkedJobDisabledPrompt.setVisible(true);
	}

	public String getBranchName() {
		return branchName;
	}

	public String getChangeListDescription() {
		return changelistCombo.getDescription();
	}

	public String getEndRevisionOption() {
		return endOption;
	}

	public P4IntegrationOptions getSelectedIntegrationOptions() {
		return integrationOptions;
	}

	public String getSourcePath() {
		return this.sourcePath;
	}

	public String getStartRevisionOption() {
		return this.startOption;
	}

	public String getTargetPath() {
		return this.targetPath;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		updatePaths();
		updateOptions();
		updateHistory();
		updatePreferences();
		super.okPressed();
	}

	/**
	 * Update the options object with the latest state of the option buttons
	 */
	public void updateOptions() {
		optionWidget.updateOptions(integrationOptions);
		integrationOptions.setTrySafeResolve(attemptSafeResolveButton.getSelection());
	}

	private void updateHistory() {
		String branch = nameText.getText().trim();
		if (branch.length() > 0) {
			SessionManager.saveComboHistory(nameText, 10, IPreferenceConstants.BRANCH_HISTORY);
		}
		String source = sourceText.getText().trim();
		if (source.length() > 0) {
			SessionManager.saveComboHistory(sourceText, 10, IPreferenceConstants.SOURCE_FILE_HISTORY);
		}

		String target = targetText.getText().trim();
		if (target.length() > 0) {
			SessionManager.saveComboHistory(targetText, 10, IPreferenceConstants.TARGET_FILE_HISTORY);
		}
	}

	private void updatePreferences() {
		boolean tryResolve = attemptSafeResolveButton.getSelection();
		PerforceUIPlugin.getPlugin().getPreferenceStore().setValue(TRY_AUTO_RESOLVE, tryResolve);
	}

	public void setBaselessMerge(boolean merge) {
		optionWidget.setBaselessMerge(merge);
	}

	public void setDisregardHistory(boolean disregard) {
		optionWidget.setDisregardHistory(disregard);
	}

	public void setDoNotCopy(boolean doNotCopy) {
		optionWidget.setDoNotCopy(doNotCopy);
	}

	public void setDoNotGetLatestRevision(boolean latest) {
		optionWidget.setDoNotGetLatest(latest);
	}

	public void setIntegrateAroundDeleted(boolean integrate) {
		optionWidget.setIntegrateAroundDeleted(integrate);
	}

	public void setPropogateFiletypes(boolean propogate) {
		optionWidget.setPropogateFiletypes(propogate);
	}

	public void setReverseMappings(boolean reverse) {
		optionWidget.setReverseMappings(reverse);
	}

	/**
	 *
	 * @see org.eclipse.jface.dialogs.StatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	protected void updateButtonsEnableState(IStatus status) {
		if (integrate != null && !integrate.isDisposed()) {
			integrate.setEnabled(!status.matches(IStatus.ERROR));
		}
	}
}
