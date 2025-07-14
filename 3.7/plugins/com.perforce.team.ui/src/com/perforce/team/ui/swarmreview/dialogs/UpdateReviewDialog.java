package com.perforce.team.ui.swarmreview.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.shelve.Messages;
import com.perforce.team.ui.swarmreview.ReviewManager;

public abstract class UpdateReviewDialog<T extends IP4Changelist> extends Dialog {

	TableViewer checkboxTableViewer;
	TableViewer shelfTableViewer;
	ReviewManager<T> controller;
	private Text textReviewId;
	private TextViewer textViewerReviewDescription;
	private Composite reviewIdSearchComposite;
	private Button revertButton;
	private Button removeButton;
	private boolean revertChecked;
	private boolean removeChecked;
	IP4Resource[] checkedFiles;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public UpdateReviewDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
	}

	public void setController(ReviewManager<T> controller) {
		this.controller = controller;
	}

	abstract TableViewer createFilesToBeReviewedGrid(final Composite parent);

	abstract TableViewer createShelvedFilesToBeReviewedGrid(final Composite parent);

	abstract Object[] getSelectedItems();

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
		lblTheFollowingFiles.setText("The following files will be sent for review:");

		checkboxTableViewer = createFilesToBeReviewedGrid(container);

		Object[] shelffiles = controller.getShelvedReviewFiles();
		if (shelffiles != null && shelffiles.length > 0) {
			Label lblShelvedFiles = new Label(container, SWT.NONE);
			lblShelvedFiles.setText("The following shelved files will be part of the review:");
			shelfTableViewer = createShelvedFilesToBeReviewedGrid(container);
			shelfTableViewer.setContentProvider(ArrayContentProvider.getInstance());
			shelfTableViewer.setInput(shelffiles);
		}

		createUserSearchPanel(container);

		checkboxTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Object[] files = controller.getReviewFiles();

		// if(files.length == 0){
		// MessageDialog.openInformation(UpdateReviewDialog.this.getShell(), "Update
		// review", "No files to review");
		// this.close();
		// }//else {
		checkboxTableViewer.setInput(files);

		if (checkboxTableViewer instanceof CheckboxTableViewer) {
			((CheckboxTableViewer) checkboxTableViewer).setAllChecked(true);
		}
		reviewIdSearchComposite.setEnabled(!controller.isUpdateReview());

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

	private void createUserSearchPanel(Composite parent) {
		// create user search
		reviewIdSearchComposite = new Composite(parent, SWT.NONE);
		reviewIdSearchComposite.setLayout(new GridLayout(3, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_composite.heightHint = 281;
		// gd_composite.widthHint = 938;
		reviewIdSearchComposite.setLayoutData(gd_composite);

		Label lblDescription = new Label(reviewIdSearchComposite, SWT.NONE);
		lblDescription.setText("Update Review:");

		textReviewId = new Text(reviewIdSearchComposite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 116;
		textReviewId.setLayoutData(gd_text);

		Button btnViewReviewDescription = new Button(reviewIdSearchComposite, SWT.NONE);
		btnViewReviewDescription.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!textReviewId.getText().isEmpty()) {
					String desc = controller.getChangeListDescriptionFor(textReviewId.getText());
					// textViewerReviewDescription.setTextColor(org.eclipse.swt.graphics.Color.RED);
					textViewerReviewDescription.getTextWidget().setText(desc);
					updateOKButtonState(controller.isUpdateReview());
				}
			}
		});
		btnViewReviewDescription.setText("View Review Description");

		Label lblReviewDescription = new Label(reviewIdSearchComposite, SWT.NONE);
		lblReviewDescription.setText("Review Description:");

		textViewerReviewDescription = new TextViewer(reviewIdSearchComposite, SWT.BORDER);

		StyledText styledText = textViewerReviewDescription.getTextWidget();
		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_styledText.heightHint = 281;
		gd_styledText.widthHint = 448;
		styledText.setLayoutData(gd_styledText);

		textViewerReviewDescription.setEditable(false);
		boolean isUpdate = controller.isUpdateReview();
		if (isUpdate) {
			textViewerReviewDescription.getTextWidget().setVisible(!isUpdate);
			gd_composite.heightHint = 0;
		}

		lblReviewDescription.setVisible(!controller.isUpdateReview());
		btnViewReviewDescription.setVisible(!controller.isUpdateReview());
		textReviewId.setVisible(!controller.isUpdateReview());
		lblDescription.setVisible(!controller.isUpdateReview());
	}

	private void updateOKButtonState(boolean enabled) {
		UpdateReviewDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(enabled);
		UpdateReviewDialog.this.getButton(IDialogConstants.OK_ID).setText("Update Files");
	}

	// @Override
	// protected void okPressed() {
	// Object[] checkedFiles = getSelectedItems();
	// if (checkedFiles == null || checkedFiles.length == 0) {
	// MessageDialog.openInformation(this.getShell(), "Review", "No files selected
	// for review");
	// } else {
	// Result<Boolean> result = controller.requestReview(checkedFiles, null);
	// MessageDialog.openError(this.getShell(), "Progress", result.getMessage());
	// super.okPressed();
	// }
	// }

	@Override
	protected void cancelPressed() {
		controller.clearSelectedChangelistData();
		super.cancelPressed();
	}

	TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
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
		updateOKButtonState(controller.isUpdateReview());
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1167, 692);
	}

}
