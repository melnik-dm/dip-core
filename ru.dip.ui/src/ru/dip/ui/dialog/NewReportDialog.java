/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.ui.dialog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.dip.core.model.reports.IReportContainer;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ReportUtils;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.status.StatusUtils;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.ui.Messages;

public class NewReportDialog extends Dialog {

	private final IReportContainer fReportFolder;
	private Composite fComposite;
	private Text fNameText;
	private Label fErrorLabel;
	
	public NewReportDialog(Shell parentShell, IReportContainer reportFolder) {
		super(parentShell);
		fReportFolder = reportFolder;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(500, 190);
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(Messages.NewReportDialog_Shell_title);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		ControlFactory.emptyLabel(parent);
		fComposite = CompositeBuilder.instance(parent).columns(3, false).horizontal().build();
	
		ControlFactory.label(fComposite, Messages.NewReportDialog_Report_label);
		fNameText = new Text(fComposite, SWT.BORDER);
		fNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fNameText.addModifyListener(e -> validate());		
		ControlFactory.label(fComposite, ".report       ");
		
		fErrorLabel = new Label(fComposite, SWT.WRAP);
		fErrorLabel.setText("\n");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fErrorLabel.setLayoutData(gd);
 		return super.createDialogArea(parent);
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		getButton(OK).setEnabled(false);
		return control;
	}
	
	
	private void validate(){
		IStatus validStatus = isValid();
		if (validStatus.isOK()) {
			fErrorLabel.setText("");
			getButton(OK).setEnabled(true);
		} else {
			fErrorLabel.setText(validStatus.getMessage());
			getButton(OK).setEnabled(false);
		}
		fComposite.layout();
	}
	
	private IStatus isValid() {
		String fileName = fNameText.getText().trim();
		if (fileName.isEmpty()) {
			return StatusUtils.NO_NAME;
		}
		
		IStatus status = DipUtilities.checkReqName(fileName);
		if (!status.isOK()) {
			return status;
		}
		
		Path path = Paths.get(fReportFolder.resource().getLocation().toOSString(), fileName + ".report");
		if (Files.exists(path)) {
			return StatusUtils.FILE_ALREADY_EXISTS;
		}
		return Status.OK_STATUS;
	}
	
	@Override
	protected void okPressed() {
		String fileName = fNameText.getText().trim() + ".report"; //$NON-NLS-1$
		try {
			ReportUtils.createNewReport(fileName, fReportFolder, getShell());
		} catch (CoreException e) {
			e.printStackTrace();
			WorkbenchUtitlities.openError("Create Report Error", e.getMessage());
		}
		super.okPressed();
	}

}
