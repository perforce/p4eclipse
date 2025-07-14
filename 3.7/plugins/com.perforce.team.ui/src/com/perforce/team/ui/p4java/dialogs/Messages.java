package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.p4java.dialogs.messages"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    public static String BrowseChangeListDialog_InformationText;
    public static String BrowseChangeListDialog_NoChangeListSelectedError;
    public static String BrowseChangeListDialog_Title;
    public static String BrowseLabelDialog_InformationText;
    public static String BrowseLabelDialog_SelectLabelErrorPrompt;
    public static String BrowseLabelDialog_Title;
    public static String BrowseStreamDialog_NoStreamSelectedError;
    public static String BrowseStreamDialog_Title;
    public static String ChangelistEditorPreferencePage_Dashed;
    public static String ChangelistEditorPreferencePage_Dotted;
    public static String ChangelistEditorPreferencePage_EnableAutoActivation;
    public static String ChangelistEditorPreferencePage_LeaveJobStatusUnchanged;
    public static String ChangelistEditorPreferencePage_ShowWrappingGuide;
    public static String ChangelistEditorPreferencePage_Solid;
    public static String ChangelistEditorPreferencePage_UseTextEditorFont;
    public static String ChangelistEditorPreferencePage_WrappingGuideColor;
    public static String ChangelistEditorPreferencePage_WrappingGuideColumn;
    public static String ChangelistEditorPreferencePage_WrappingGuideLineStyle;
    public static String ChangeSpecDialog_AddJobs;
    public static String ChangeSpecDialog_Changelist;
    public static String ChangeSpecDialog_ChooseATemplate;
    public static String ChangeSpecDialog_Configure;
    public static String ChangeSpecDialog_Default;
    public static String ChangeSpecDialog_Description;
    public static String ChangeSpecDialog_DeselectAll;
    public static String ChangeSpecDialog_Jobs;
    public static String ChangeSpecDialog_JobStatus;
    public static String ChangeSpecDialog_New;
    public static String ChangeSpecDialog_OpenEditorPrefs;
    public static String ChangeSpecDialog_PerforceChangeSpecification;
    public static String ChangeSpecDialog_RemoveJobs;
    public static String ChangeSpecDialog_ReopenFiles;
    public static String ChangeSpecDialog_SelectAll;
    public static String ChangeSpecDialog_Submit;
    public static String ChangeSpecDialog_Template;
    public static String CheckConsistencyDialog_ConsistencyCheckResults;
    public static String CheckConsistencyDialog_FilesNotUnderPerforce;
    public static String CheckConsistencyDialog_OpenInChangelist;
    public static String CheckConsistencyDialog_UnopenedFilesMissing;
    public static String CheckConsistencyDialog_UnopenedFilesThatDiffer;
    public static String CopyToStreamDialog_CopyButtonText;
    public static String CopyToStreamDialog_LimitRevisionRange;
    public static String CopyToStreamDialog_SourceTargetTitle;
    public static String CopyToStreamDialog_TaskStreamCannotCopyToNonParent;
	public static String CopyToStreamDialog_Title;
    public static String DepotFileChooser_Browse;
    public static String DepotFileChooser_DepotFolderNotStartWithDepotError;
    public static String DepotFileChooser_MergeFolderPathEmptyError;
    public static String DepotFileChooser_MergeOnlyFileFolders;
    public static String DescriptionTemplatesPreferencePage_AddChangelistDescriptionTemplate;
    public static String DescriptionTemplatesPreferencePage_AddNewChangelistDescriptionTemplate;
    public static String DescriptionTemplatesPreferencePage_ChangelistDescriptionCantBeEmpty;
    public static String DescriptionTemplatesPreferencePage_ChangelistDescriptionTemplate;
    public static String DescriptionTemplatesPreferencePage_CreateEditRemoveChangelistDescriptionTemplates;
    public static String DescriptionTemplatesPreferencePage_EditChangelistDescriptionTemplate;
    public static String DescriptionTemplatesPreferencePage_EditSelectedChangelistDescriptionTemplate;
    public static String DescriptionTemplatesPreferencePage_MaxNumberOfChangelistsInHistory;
    public static String DescriptionTemplatesPreferencePage_Preview;
    public static String DescriptionTemplatesPreferencePage_RemoveSelectedChangelistDescriptionTemplate;
    public static String IntegOptionWidget_BranchResolves;
	public static String IntegOptionWidget_DeleteResolves;
	public static String IntegOptionWidget_EnableIntegrationsAroundDeletedRev;
	public static String IntegOptionWidget_SkipIntegratedRevs;
	public static String IntegrateDialog_AdvancedOptions;
    public static String IntegrateDialog_BranchName;
    public static String IntegrateDialog_BranchSpec;
    public static String IntegrateDialog_Browse;
    public static String IntegrateDialog_Cancel;
    public static String IntegrateDialog_Changelist;
    public static String IntegrateDialog_DisregardIntegHistory;
    public static String IntegrateDialog_DontCopyTargetFiles;
    public static String IntegrateDialog_DontGetLatestRev;
    public static String IntegrateDialog_EnableBaselessMerges;
    public static String IntegrateDialog_EnableIntegAroundDeletedRevs;
    public static String IntegrateDialog_End;
    public static String IntegrateDialog_EnterChangelistNumber;
    public static String IntegrateDialog_EnterLabel;
    public static String IntegrateDialog_EnterRevisionNumber;
    public static String IntegrateDialog_FileSpec;
    public static String IntegrateDialog_GeneratingIntegPreview;
    public static String IntegrateDialog_Integrate;
    public static String IntegrateDialog_Label;
	public static String IntegrateDialog_Datetime;
    public static String IntegrateDialog_LimitIntegTo;
    public static String IntegrateDialog_LimitRevRange;
    public static String IntegrateDialog_MustEnterBranchName;
    public static String IntegrateDialog_MustEnterSourcePath;
    public static String IntegrateDialog_MustEnterTargetPath;
    public static String IntegrateDialog_PerformSafeAutoresolve;
    public static String IntegrateDialog_Preview;
    public static String IntegrateDialog_PropagateSourceFiletypes;
    public static String IntegrateDialog_ReverseBranchMappings;
    public static String IntegrateDialog_Revision;
    public static String IntegrateDialog_Source;
    public static String IntegrateDialog_SourceLabel;
    public static String IntegrateDialog_Start;
    public static String IntegrateDialog_Target;
    public static String IntegrateDialog_TargetLabel;
    public static String IntegrateDialog_UsePathAs;
    public static String IntegrateToStreamDialog_InitializeDialog;
    public static String IntegrationPreviewDialog_Close;
    public static String IntegrationPreviewDialog_From;
    public static String IntegrationPreviewDialog_IntegrationPreview;
    public static String MergeToStreamDialog_InformationText;
    public static String MergeToStreamDialog_LimitRevisionRange;
    public static String MergeToStreamDialog_Merge;
    public static String MergeToStreamDialog_MergeToStream;
	public static String MergeToStreamDialog_TaskStreamCannotMergeFromNonParent;
    public static String OpenDialog_FilesNumSelected;
    public static String OpenDialog_MustSelectAtLeastOneFile;
    public static String OpenDialog_UseSelectedChangelist;
    public static String PasswordDialog_Cancel;
    public static String PasswordDialog_Client;
    public static String PasswordDialog_Connection;
    public static String PasswordDialog_EnterPassword;
    public static String PasswordDialog_EnterPasswordLabel;
    public static String PasswordDialog_RememberPassword;
    public static String PasswordDialog_Broker_Server;
    public static String PasswordDialog_Server;
    public static String PasswordDialog_User;
    public static String PasswordDialog_WorkOffline;
    public static String PopulateDialog_Add;
	public static String PopulateDialog_Base_path;
	public static String PopulateDialog_Base_path_unavailable;
	public static String PopulateDialog_Branch_method;
	public static String PopulateDialog_Branching;
	public static String PopulateDialog_Browse;
	public static String PopulateDialog_Choose_target_file_folders;
	public static String PopulateDialog_Description;
	public static String PopulateDialog_Edit;
	public static String PopulateDialog_Path;
	public static String PopulateDialog_Populate;
    public static String PopulateDialog_PopulatePreview;
    public static String PopulateDialog_GeneratingPopulatePreview;
	public static String PopulateDialog_Must_select_file_or_folder;
    public static String PopulateDialog_No_common_source_path;
	public static String PopulateDialog_Not_implemented_yet;
	public static String PopulateDialog_Remove;
	public static String PopulateDialog_Revision;
	public static String PopulateDialog_Source_cannot_empty;
	public static String PopulateDialog_Source_files_folders;
	public static String PopulateDialog_Specify_branch;
	public static String PopulateDialog_Specify_source_and_target;
	public static String PopulateDialog_Target_cannot_empty;
	public static String PerforceErrorDialog_ErrorsLabel;
    public static String PerforceErrorDialog_PerforceError;
    public static String ResolveWizard_Resolve;
    public static String ResolveWizardAutoPage_AcceptSource;
    public static String ResolveWizardAutoPage_AcceptTarget;
    public static String ResolveWizardAutoPage_All;
    public static String ResolveWizardAutoPage_AllowMergeNoConflicts;
    public static String ResolveWizardAutoPage_AllowMergeWithConflicts;
    public static String ResolveWizardAutoPage_AttributeResolves;
    public static String ResolveWizardAutoPage_PageDescription;
    public static String ResolveWizardAutoPage_PageTitle;
    public static String ResolveWizardAutoPage_AutoResolvingFiles;
    public static String ResolveWizardAutoPage_BinaryFiles;
    public static String ResolveWizardAutoPage_BranchResolves;
    public static String ResolveWizardAutoPage_ContentResolves;
    public static String ResolveWizardAutoPage_DeleteResolves;
    public static String ResolveWizardAutoPage_FilesToResolve;
    public static String ResolveWizardAutoPage_FiletypeResolves;
    public static String ResolveWizardAutoPage_MergeBinary;
    public static String ResolveWizardAutoPage_MoveResolves;
    public static String ResolveWizardAutoPage_None;
    public static String ResolveWizardAutoPage_Resolve;
    public static String ResolveWizardAutoPage_ResolveFailedMessage;
    public static String ResolveWizardAutoPage_ResolveFailedTitle;
    public static String ResolveWizardAutoPage_ResolveOptions;
    public static String ResolveWizardAutoPage_ResolveSelected;
    public static String ResolveWizardAutoPage_ResolveType;
    public static String ResolveWizardAutoPage_ResolveWith;
    public static String ResolveWizardAutoPage_SafeAutoResolve;
    public static String ResolveWizardAutoPage_SelectFiles;
    public static String ResolveWizardAutoPage_TextFiles;
    public static String ResolveWizardAutoPage_UpdatingDialog;
    public static String ResolveWizardInteractivePage_ActionResolveMustBeAutoresolved;
    public static String ResolveWizardInteractivePage_FilesToResolve;
    public static String ResolveWizardInteractivePage_PageDescription;
    public static String ResolveWizardInteractivePage_PageTitle;
    public static String ResolveWizardInteractivePage_MustResolveEarlierStepsFirst;
    public static String ResolveWizardInteractivePage_Resolve;
    public static String ResolveWizardInteractivePage_ResolveFile;
    public static String ResolveWizardInteractivePage_ResolveOptions;
    public static String ResolveWizardInteractivePage_ResolveType;
    public static String ResolveWizardInteractivePage_ResolveWith;
    public static String ResolveWizardInteractivePage_UpdatingDialog;
    public static String ResolveWizardInteractivePage_UseEclipse;
    public static String ResolveWizardInteractivePage_UseP4Merge;
    public static String ResolveWizardMethodPage_AutoResolve;
    public static String ResolveWizardMethodPage_InteractiveResolve;
	public static String ResolveWizardMethodPage_RememberChoice;
    public static String ResolveWizardMethodPage_PageDescription;
    public static String ResolveWizardMethodPage_PageTitle;
    public static String RevisionRangeWidget_EndVersionEmptyError;
    public static String RevisionRangeWidget_SelectVersionTypeAndVersion;
    public static String RevisionRangeWidget_StartVersionEmptyError;
    public static String RevisionRangeWidget2_Browse;
    public static String RevisionRangeWidget2_End;
    public static String RevisionRangeWidget2_EndTypeEmptyError;
    public static String RevisionRangeWidget2_EndVersionEmptyError;
    public static String RevisionRangeWidget2_Start;
    public static String RevisionRangeWidget2_StartTypeEmptyError;
    public static String RevisionRangeWidget2_StartVersionEmptyError;
    public static String RevisionUptoWidget_RevisionUpto;
    public static String RevisionUptoWidget_SelectVersionTypeAndInputVersion;
    public static String RevisionUptoWidget_VersionNotSetError;
    public static String RevisionUptoWidget_VersionTypeNotSetError;
    public static String RevisionUptoWidget2_Browse;
    public static String RevisionUptoWidget2_RevisionsUpTo;
    public static String RevisionUptoWidget2_VersionNotSetError;
    public static String RevisionUptoWidget2_VersionTypeNotSetError;
    public static String SourceTargetStreamWidget_Browse;
    public static String SourceTargetStreamWidget_InfornationText;
    public static String SourceTargetStreamWidget2_Browse;
    public static String SourceTargetStreamWidget2_InformationText;
    public static String SourceTargetStreamWidget2_SourceTargetSameError;
	
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
    
    /**
	 * Gets common translation for current local
	 * 
	 * @param key
	 *            the key
	 * @return translated value string
	 */

	public static String getString(String key) {

		try {
			String result = RESOURCE_BUNDLE.getString(key);
			return result;
		} catch (Exception e) {
			assert false;
			return key;
		}
	}

	/**
	 * Gets formatted translation for current local
	 * 
	 * @param key
	 *            the key
	 * @return translated value string
	 */
	public static String getFormattedString(String key, Object[] arguments) {
		return MessageFormat.format(getString(key), arguments);
	}

	/**
	 * In meta xml file we use %keyName% as externalized key instead of value We
	 * use this method to translate the %keyName% into value from resource
	 * bundle.
	 * 
	 * @param key
	 *            the externalized key like %keyName%
	 * @return value the %keyName% represent
	 */

	public static String getXMLKey(String key) {
		return key;
	}
}
