package com.perforce.team.ui.swarmreview.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.shelve.Messages;
import com.perforce.team.ui.swarmreview.ReviewManager;
import com.perforce.team.ui.swarmreview.ReviewerUser;
import com.perforce.team.ui.swarmreview.ReviewerUserList;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.views.PendingView;

public abstract class NewReviewDialog<T extends IP4Changelist> extends Dialog {
	
	TableViewer checkboxTableViewer;
	TableViewer shelfTableViewer;
	private TableViewer  reviewerTableViewer;//TableViewer CheckboxTableViewer
	private Table reviewerTable;
	private Text textReviewer;
	TextViewer descriptionTextViewer;
	private Button revertButton;
    private Button removeButton;
    private boolean revertChecked;
    private boolean removeChecked;
    IP4Resource[] checkedFiles;
	
	ReviewManager<T> controller;
	
	private static final Image CHECKED =  PerforceUIPlugin.getPlugin()
            .getImageDescriptor("views/checked.gif")
            .createImage();
    private static final Image UNCHECKED = PerforceUIPlugin.getPlugin()
            .getImageDescriptor("views/unchecked.gif").createImage();

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public NewReviewDialog(Shell parentShell) {
		super(parentShell);
		//setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	public void setController(ReviewManager<T> controller) {
		this.controller = controller;
	}
	
	abstract TableViewer createFilesToBeReviewedGrid(final Composite parent);
	abstract TableViewer createShelvedFilesToBeReviewedGrid(final Composite parent);
	abstract IP4Resource[] getSelectedItems();
	
	
	abstract String getDialogTitle();
	
	/**
     * return checked (or selected) files
     * 
     * @return - object
     */
    public IP4Resource[] getCheckedFiles() {
        return checkedFiles;
    }

    /**
     * check if revert checkbox is selected
     * 
     * @return - boolean
     */
    public boolean isRevertChecked() {
        return revertChecked;
    }

    /**
     * check if remove add files checkbox is selected
     * 
     * @return - boolean
     */
    public boolean isRemoveChecked() {
        return removeChecked;
    }

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		Label lblTheFollowingFiles = new Label(container, SWT.NONE);
		lblTheFollowingFiles.setText(getDialogTitle());

		checkboxTableViewer = createFilesToBeReviewedGrid(container);
		
		Object[] shelffiles = controller.getShelvedReviewFiles();
		if(shelffiles != null && shelffiles.length > 0){
			Label lblShelvedFiles = new Label(container, SWT.NONE);
			lblShelvedFiles.setText("The following shelved files will be part of the review:");
		
			shelfTableViewer = createShelvedFilesToBeReviewedGrid(container);
			shelfTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			shelfTableViewer.setInput(shelffiles);
		}

		Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setText("Review Description:");

		descriptionTextViewer = new TextViewer(container, SWT.BORDER);
		StyledText styledText = descriptionTextViewer.getTextWidget();
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_styledText.heightHint = 281;
		styledText.setLayoutData(gd_styledText);

		Label lblReviewers = new Label(container, SWT.NONE);
		lblReviewers.setText("Reviewers:");

		reviewerTableViewer = createReviewerdGrid(container);
		
		createUserSearchPanel(container);

		checkboxTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		Object[] files = controller.getReviewFiles();
		checkboxTableViewer.setInput(files);
		populateControls();
		revertButton = new Button(container, SWT.CHECK);
        revertButton.setText(Messages.ConfirmShelveDialog_RevertCheckedOutFiles);
        removeButton = new Button(container, SWT.CHECK);
        removeButton.setText(Messages.ConfirmShelveDialog_RemoveFilesOpenedForAdd);

        SelectionListener listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!revertButton.getSelection()) {
                    removeButton.setEnabled(false);
                    removeButton.setSelection(false);
                    return;
                }
                removeButton.setEnabled(true);
                revertChecked = revertButton.getSelection();
                removeChecked = removeButton.getSelection();
            }
        };
        revertButton.addSelectionListener(listener);
        removeButton.addSelectionListener(listener);
        listener.widgetSelected(null);
		return container;
	}
	
	private void populateControls(){
		descriptionTextViewer.getTextWidget().setText(controller.getChangelistDescription());
	}

	private void createUserSearchPanel(Composite parent) {
		// create user search
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		
		gd_composite.widthHint = 552;
		composite.setLayoutData(gd_composite);
		
		Label lblUser = new Label(composite, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText("User:");

		textReviewer = new Text(composite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 239;
		textReviewer.setLayoutData(gd_text);

		Button btnAddReviewer = new Button(composite, SWT.NONE);
		btnAddReviewer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!textReviewer.getText().isEmpty()) {
					Result result = controller.addReviewerByName(textReviewer.getText());
					if(result.isSuccessful()){
						updateReviewerGrid(controller.getSelectedReviewerUsers().toArray());
						textReviewer.setText("");
					}else{
						MessageDialog.openError(NewReviewDialog.this.getShell(), "Add reviewer", result.getMessage());
					}
				}
			}
		});
		btnAddReviewer.setText("Add");

		Button btnBrowse = new Button(composite, SWT.NONE);
		btnBrowse.setText("Browse");

		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectUsersDialog<T> usersdialog = new SelectUsersDialog<T>(NewReviewDialog.this);
				usersdialog.setSwarmReviewDialogController(controller);
				usersdialog.open();
			}
		});

		Button btnRemoveReviewer = new Button(composite, SWT.NONE);
		btnRemoveReviewer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem [] items = reviewerTable.getSelection();
				if(items != null && items.length > 0){
					if(MessageDialog.openConfirm(NewReviewDialog.this.getShell(), "Remove reviewer", "Are you sure you want to remove selected reviewer?")){
						
						ReviewerUserList userList = new ReviewerUserList();
						for(TableItem item : items){
							ReviewerUser user = (ReviewerUser) item.getData();
							userList.addReviewer(user);
						}
		   				controller.removeReviewer(userList);
		   				updateReviewerGrid(controller.getSelectedReviewerUsers().toArray());
					}
				}
			}
		});
		btnRemoveReviewer.setText("Remove");
	}
	
	
	
	@Override
	protected void cancelPressed(){
		controller.clearSelectedChangelistData();         
		controller.clearReviewers();
		super.cancelPressed();
	}

	

	void updateReviewerGrid(Object[] data) {
		reviewerTableViewer.setInput(data);
	}

	private TableViewer createReviewerdGrid(final Composite parent) {
		// first column is for the first name
		String[] titles = { "User", "Required", "Full Name" };
		int[] bounds = { 100, 100, 100 };

		reviewerTableViewer = new TableViewer(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		//reviewerTableViewer = CheckboxTableViewer.newCheckList(parent,
				//SWT.BORDER | SWT.FULL_SELECTION);
		reviewerTable = reviewerTableViewer.getTable();

		GridData gd_table_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_table_1.heightHint = 278;
		reviewerTable.setLayoutData(gd_table_1);

		TableViewerColumn col = createTableViewerColumn(reviewerTableViewer, titles[0], bounds[0], 0, SWT.LEAD);
		
	
		col.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return ((ReviewerUser) element).getLogin();
				}
				return null;
			}
		});

		col = createTableViewerColumn(reviewerTableViewer, titles[1], bounds[1], 1);
		final ChkBoxSupport chkEditor = getCheckBoxEditor(reviewerTableViewer);
		
		
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return null;
				}
				return null;
			}
			@Override
            public Image getImage(Object element) {
				if(element instanceof ReviewerUser){
	                if (((ReviewerUser) element).isRequired()) {
	                    return CHECKED;
	                } else {
	                    return UNCHECKED;
	                }
				}
				return UNCHECKED;
            }
			
		});
		col.setEditingSupport(chkEditor);
		
		// second column is for the last name
		col = createTableViewerColumn(reviewerTableViewer, titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return ((ReviewerUser) element).getFullName();
				}
				return null;
			}
		});
		reviewerTable.setHeaderVisible(true);
		reviewerTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		return reviewerTableViewer;
	}
	
	private ChkBoxSupport getCheckBoxEditor(final TableViewer viewer){
		
		
		return new ChkBoxSupport(viewer);
	}

   TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
	
		return viewerColumn;
	}
	
	TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound,
			final int colNumber, int style) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
	
		return viewerColumn;
	}

	public void setFocus(TableViewer viewer) {
		viewer.getControl().setFocus();
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1367, 692);
	}

}

class ChkBoxSupport extends EditingSupport{
	private TableViewer viewer;
	private CheckboxCellEditor editor;
	
	public ChkBoxSupport(TableViewer viewer){
		super(viewer);
		this.viewer=viewer;
	}
	@Override
	protected CellEditor getCellEditor(Object element) {
		return (editor = new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY));
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof ReviewerUser){
			((ReviewerUser)element).setRequired(!(((ReviewerUser)element).isRequired()));
			return Boolean.valueOf(((ReviewerUser)element).isRequired());
		}
		return Boolean.FALSE;
	}

	@Override
	protected void setValue(Object element, Object value) {
		viewer.update(element, null);
	}
}
