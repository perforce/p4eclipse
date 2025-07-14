package com.perforce.team.ui.swarmreview.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.ui.swarmreview.ReviewManager;
import com.perforce.team.ui.swarmreview.ReviewerUser;
import com.perforce.team.ui.swarmreview.ReviewerUserList;

public class SelectUsersDialog<T extends IP4Changelist> extends Dialog {
	private Table reviewerTable;
	
	private ReviewManager<T> controller;
	
	private NewReviewDialog<T> parent;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SelectUsersDialog(NewReviewDialog<T> parent) {
		super(parent.getShell());
		this.parent = parent;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		createUsersGrid(container);
		//createOutlineDataComposite(container);
		
		return container;
	}
	
	public void setSwarmReviewDialogController(ReviewManager<T> controller){
		this.controller = controller;
		controller.initReviewers();
	}
	
	
	private void createOutlineDataComposite(final Composite parent){
		Composite outlineDataComposite = new Composite(parent, SWT.NONE);
		outlineDataComposite.setLayout(new GridLayout(4, false));
		GridData gd_outlineDataComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_outlineDataComposite.widthHint = 831;
		gd_outlineDataComposite.heightHint = 168;
		outlineDataComposite.setLayoutData(gd_outlineDataComposite);
		
		Label lblUser = new Label(outlineDataComposite, SWT.NONE);
		lblUser.setText("User:");
		
		Label lblNewLabel = new Label(outlineDataComposite, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 228;
		gd_lblNewLabel.minimumWidth = 500;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		
		Label lblFullBame = new Label(outlineDataComposite, SWT.NONE);
		lblFullBame.setText("Full name:");
		
		Label lblNewLabel_1 = new Label(outlineDataComposite, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 145;
		lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
		
		Label lblEmail = new Label(outlineDataComposite, SWT.NONE);
		lblEmail.setText("Email:");
		
		Label lblNewLabel_5 = new Label(outlineDataComposite, SWT.NONE);
		
		Label lblAuthMethod = new Label(outlineDataComposite, SWT.NONE);
		lblAuthMethod.setText("Auth method:");
		
		Label lblNewLabel_7 = new Label(outlineDataComposite, SWT.NONE);
		
		Label lblDateModified = new Label(outlineDataComposite, SWT.NONE);
		lblDateModified.setText("Date modified:");
		
		Label lblNewLabel_2 = new Label(outlineDataComposite, SWT.NONE);
		
		Label lblType = new Label(outlineDataComposite, SWT.NONE);
		lblType.setText("Type:");
		
		Label lblNewLabel_3 = new Label(outlineDataComposite, SWT.NONE);
		
		Label lblDateAccessd = new Label(outlineDataComposite, SWT.NONE);
		lblDateAccessd.setText("Date Accessd:");
		
		Label lblNewLabel_6 = new Label(outlineDataComposite, SWT.NONE);
		
		Label lblNewLabel_4 = new Label(outlineDataComposite, SWT.NONE);
		lblNewLabel_4.setText("New Label");
		
		Label lblNewLabel_8 = new Label(outlineDataComposite, SWT.NONE);
	}
	
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}
	
	private void createUsersGrid(final Composite parent) {
		String[] titles = { "User", "Email", "Full Name" };
		int[] bounds = { 100, 300, 300 };
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 828;
		composite.setLayoutData(gd_composite);
		
		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		reviewerTable = tableViewer.getTable();
		reviewerTable.setHeaderVisible(true);
		
		
		
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_table.heightHint = 230;
		gd_table.widthHint = 800;
		reviewerTable.setLayoutData(gd_table);

		
		TableViewerColumn col = createTableViewerColumn(tableViewer, titles[0], bounds[0], 0);

		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return ((ReviewerUser) element).getLogin();
				}
				return null;
			}
		});
		
		col = createTableViewerColumn(tableViewer, titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return ((ReviewerUser) element).getEmail();
				}
				return null;
			}
		});
		

		
		col = createTableViewerColumn(tableViewer, titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ReviewerUser) {
					return ((ReviewerUser) element).getFullName();
				}
				return null;
			}
		});

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(controller.getReviewerUsers().toArray());
	}
	
	public void okPressed(){
		TableItem[] selectedItems = reviewerTable.getSelection();
		if(selectedItems != null && selectedItems.length > 0){
			ReviewerUserList reviewers = new ReviewerUserList();
			if(selectedItems != null && selectedItems.length > 0){
				for(TableItem tableItem :  selectedItems){
					ReviewerUser userSummary = (ReviewerUser) tableItem.getData();
					reviewers.addReviewer(userSummary);
				}
			}
			controller.addReviewerUsers(reviewers);
			((NewReviewDialog<IP4PendingChangelist>)parent).updateReviewerGrid(controller.getSelectedReviewerUsers().toArray());
		}
		super.okPressed();
		this.close();
	}

	/**
	 * Create contents of the button bar.
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
		return new Point(838, 331);
	}
	
}
