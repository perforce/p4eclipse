/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ExternalToolsPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.preferences.ExternalToolsPreferencePage"; //$NON-NLS-1$

    private static final String P4MERGE_APP = "p4merge.app"; //$NON-NLS-1$

    private static final String P4MERGE_APPENDED = "/Contents/MacOS/p4merge"; //$NON-NLS-1$

    private static final String P4V_APP = "p4v.app"; //$NON-NLS-1$

    private static final String P4V_APPENDED = "/Contents/MacOS/p4v"; //$NON-NLS-1$
    
    private static final String P4V_LINK = "https://www.perforce.com/downloads/helix-visual-client-p4v"; //$NON-NLS-1$

    private static final String P4MERGE_LINK = "https://www.perforce.com/downloads/visual-merge-tool"; //$NON-NLS-1$
    
    private static final String P4MFA_APP_LINK = "https://www.perforce.com/downloads/helix-mfa-authenticator";
    private Composite displayArea;

    private Group p4MergeGroup;
    private Label p4MergeInfo;
    private Link p4MergeLink;
    private Label p4MergeLabel;
    private Text p4MergeText;
    private Button browseButton;

    private Group p4VGroup;
    private Label p4VInfo;
    private Link p4VLink;
    private Label p4VLabel;
    private Text p4VText;
    private Button browseVButton;
    
    private Group p4MFAAppGroup;
    private Label p4MFAAppInfo;
    private Link p4MFAAppLink;
    private Label p4MFAAppLabel;
    private Text p4MFAAppText;
    private Button browseMFAAppButton;

    
    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout(3, false);
        layout.marginBottom = 10;
        
        createP4MergeGroup(layout);
        createP4VGroup(layout);
        createP4MFAAppConfigGroup(layout);
        return displayArea;
    }

    private void createP4VGroup(GridLayout layout) {
        p4VGroup = new Group(displayArea, SWT.NONE);
        p4VGroup.setLayout(layout);
        p4VGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        p4VGroup.setText(Messages.ExternalToolsPreferencePage_PerforceVisualClient);
        p4VInfo = new Label(p4VGroup, SWT.LEFT);
        p4VInfo.setText(Messages.ExternalToolsPreferencePage_UseP4VForTimelapseAndRevgraph);

        GridData pvlData = new GridData(SWT.FILL, SWT.FILL, true, false);
        pvlData.horizontalSpan = 3;
        p4VInfo.setLayoutData(pvlData);

        p4VLabel = new Label(p4VGroup, SWT.LEFT);
        p4VLabel.setText(Messages.ExternalToolsPreferencePage_P4VPath);
        p4VText = new Text(p4VGroup, SWT.SINGLE | SWT.BORDER);
        p4VText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        p4VText.setText(getPreferenceStore().getString(
                IPreferenceConstants.P4VC_PATH));
        p4VText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
        browseVButton = new Button(p4VGroup, SWT.PUSH);
        browseVButton.setText(Messages.ExternalToolsPreferencePage_Browse);
        browseVButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(browseVButton.getShell(),
                        SWT.OPEN);
                String path = dialog.open();
                if (path != null) {
                    if (P4CoreUtils.isMac() && path.endsWith(P4V_APP)) {
                        path += P4V_APPENDED;
                    }
                    p4VText.setText(path);
                }
            }

        });

        p4VLink = new Link(p4VGroup, SWT.NONE);
        p4VLink.setText(Messages.ExternalToolsPreferencePage_DownloadP4V);
        p4VLink.setLayoutData(pvlData);
        p4VLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openURL(P4V_LINK);
            }

        });
    }
    
    private void createP4MFAAppConfigGroup(GridLayout layout) {
        p4MFAAppGroup = new Group(displayArea, SWT.NONE);
        p4MFAAppGroup.setLayout(layout);
        p4MFAAppGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        p4MFAAppGroup.setText(Messages.ExternalToolsPreferencePage_PerforceMFAAppConfig);
        p4MFAAppInfo = new Label(p4MFAAppGroup, SWT.LEFT);
        p4MFAAppInfo.setText(Messages.ExternalToolsPreferencePage_UseP4MFAAppConfig);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 3;
        p4MFAAppInfo.setLayoutData(data);

        p4MFAAppLabel = new Label(p4MFAAppGroup, SWT.LEFT);
        p4MFAAppLabel.setText(Messages.ExternalToolsPreferencePage_P4MFAAppConfigPath);
        p4MFAAppText = new Text(p4MFAAppGroup, SWT.SINGLE | SWT.BORDER);
        p4MFAAppText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        p4MFAAppText.setText(getPreferenceStore().getString(
                IPreferenceConstants.P4MFA_APP_PATH));
        p4MFAAppText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
        browseMFAAppButton = new Button(p4MFAAppGroup, SWT.PUSH);
        browseMFAAppButton.setText(Messages.ExternalToolsPreferencePage_Browse);
        browseMFAAppButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(browseMFAAppButton.getShell(),
                        SWT.OPEN);
                String path = dialog.open();
                if (path != null) {
                    if (P4CoreUtils.isMac() && path.endsWith("HelixMFA.app")) {
                        path += "/Contents/MacOS/HelixMFA";
                    }
                    p4MFAAppText.setText(path);
                }
            }

        });

        p4MFAAppLink = new Link(p4MFAAppGroup, SWT.NONE);
        p4MFAAppLink.setText(Messages.ExternalToolsPreferencePage_DownloadP4MFAAppConfig);
        p4MFAAppLink.setLayoutData(data);
        p4MFAAppLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openURL(P4MFA_APP_LINK);
            }

        });
    }
    


    private void createP4MergeGroup(GridLayout layout) {
        p4MergeGroup = new Group(displayArea, SWT.NONE);
        p4MergeGroup.setLayout(layout);
        p4MergeGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        p4MergeGroup
                .setText(Messages.ExternalToolsPreferencePage_PerforceMerge);

        p4MergeInfo = new Label(p4MergeGroup, SWT.LEFT);
        p4MergeInfo
                .setText(Messages.ExternalToolsPreferencePage_UseP4MergeForResolving);

        GridData pmlData = new GridData(SWT.FILL, SWT.FILL, true, false);
        pmlData.horizontalSpan = 3;
        p4MergeInfo.setLayoutData(pmlData);

        p4MergeLabel = new Label(p4MergeGroup, SWT.LEFT);
        p4MergeLabel.setText(Messages.ExternalToolsPreferencePage_P4MergePath);
        p4MergeText = new Text(p4MergeGroup, SWT.SINGLE | SWT.BORDER);
        p4MergeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        p4MergeText.setText(getPreferenceStore().getString(
                IPreferenceConstants.P4MERGE_PATH));
        p4MergeText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
        browseButton = new Button(p4MergeGroup, SWT.PUSH);
        browseButton.setText(Messages.ExternalToolsPreferencePage_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(browseButton.getShell(),
                        SWT.OPEN);
                String path = dialog.open();
                if (path != null) {
                    if (P4CoreUtils.isMac() && path.endsWith(P4MERGE_APP)) {
                        path += P4MERGE_APPENDED;
                    }
                    p4MergeText.setText(path);
                }
            }

        });

        p4MergeLink = new Link(p4MergeGroup, SWT.NONE);
        p4MergeLink
                .setText(Messages.ExternalToolsPreferencePage_DownloadP4Merge);
        p4MergeLink.setLayoutData(pmlData);
        p4MergeLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openURL(P4MERGE_LINK);
            }

        });
    }

    private void openURL(String url) {
        IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
                .getBrowserSupport();
        boolean opened = false;
        if (support != null) {
            try {
                IWebBrowser browser = support.getExternalBrowser();
                if (browser != null) {
                    browser.openURL(new URL(url));
                    opened = true;
                }
            } catch (PartInitException e) {
                PerforceProviderPlugin.logError(e);
            } catch (MalformedURLException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        if (!opened) {
            P4ConnectionManager
                    .getManager()
                    .openInformation(
                            P4UIUtils.getShell(),
                            Messages.ExternalToolsPreferencePage_BrowserLaunchErrorTitle,
                            MessageFormat
                                    .format(Messages.ExternalToolsPreferencePage_BrowserLaunchErrorMessage,
                                            url));
        }
    }

    private void validate() {
        String path = p4MergeText.getText().trim();
        if (path.length() > 0) {
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                setErrorMessage(Messages.ExternalToolsPreferencePage_MustSpecifyP4MergePath);
                setValid(false);
                return;
            }
        }

        path = p4VText.getText().trim();
        if (path.length() > 0) {
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                setErrorMessage(Messages.ExternalToolsPreferencePage_MustSpecifyP4VPath);
                setValid(false);
                return;
            }
        }

        setValid(true);
        setErrorMessage(null);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUIPlugin.getPlugin().getPreferenceStore());
    }

    /**
     * Set p4 merge path
     * 
     * @param path
     */
    public void setP4MergeText(String path) {
        if (path != null) {
            this.p4MergeText.setText(path);
        }
    }

    /**
     * Get p4 merge path
     * 
     * @return - path
     */
    public String getP4MergeText() {
        return this.p4MergeText.getText();
    }

    /**
     * Set p4v path
     * 
     * @param path
     */
    public void setP4VText(String path) {
        if (path != null) {
            this.p4VText.setText(path);
        }
    }

    /**
     * Get p4v path
     * 
     * @return - path
     */
    public String getP4VText() {
        return this.p4VText.getText();
    }
    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        getPreferenceStore().setValue(IPreferenceConstants.P4MERGE_PATH,
                p4MergeText.getText().trim());
        getPreferenceStore().setValue(IPreferenceConstants.P4VC_PATH,
                p4VText.getText().trim());
        getPreferenceStore().setValue(IPreferenceConstants.P4MFA_APP_PATH,
                p4MFAAppText.getText().trim());
        return super.performOk();
    }
}
