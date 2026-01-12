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
package ru.dip.ui.table.ktable.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IUnitPresentation;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.Messages;
import ru.dip.ui.glossary.GlossaryDialog;
import ru.dip.ui.table.ktable.KTableComposite;

public class OpenAction extends DocumentAction {

	public OpenAction(KTableComposite tableComposite) {
		super(tableComposite);
		setOpenFileText();
	}
	
	public void setOpenFileText() {
		setText(Messages.OpenAction_Name);
	}

	public void setOpenSectionText() {
		setText(Messages.OpenAction_Name2);
	}
	
	@Override
	public void run() {
		fTableComposite.openFile();
	}
	
	@Override
	public void enableNullSelection() {
		setEnabled(false);
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		if (fTableComposite.model().isParentHeader(selectedDipDocElement)){
			setEnabled(false);
			return;
		}
		if (selectedDipDocElement instanceof DipUnit) {
			if (((DipUnit) selectedDipDocElement).getUnitType() == UnitType.PAGEBREAK) {
				return;
			}			
			setOpenFileText();
			setEnabled(!selectedDipDocElement.isReadOnly());
		} else if (selectedDipDocElement instanceof IDipParent){					
			setOpenSectionText();
			setEnabled(true);		}
	}

	@Override
	public void enableSeveralSelection() {
		setEnabled(false);
	}
	
	//==============================
	// open utils
	
	public static void openFile(IDipUnit unit, Shell shell) {
		IFile file = (IFile) unit.resource();
		IUnitPresentation presentation = unit.getUnitPresentation();
		if (presentation != null && presentation.getUnitType() == UnitType.REPROT_REF) {
			ReportRefPresentation reportPresentation = (ReportRefPresentation) presentation.getPresentation();
			if (reportPresentation != null) {
				file = reportPresentation.getReportFile();
			}
		}
		if (presentation != null && presentation.getUnitType() == UnitType.GLOS_REF) {
			//doOpenGlossaryDialog();
			GlossaryDialog.openGlossaryDialog(unit.dipProject(), shell);
			return;
		}
		if (presentation.getUnitType() == UnitType.PAGEBREAK) {
			return;
		}
		
		if (unit.isReadOnly()) {
			WorkbenchUtitlities.openReadOnlyErrorMessage(shell, unit);
			return;
		}

		WorkbenchUtitlities.openFile(file);
	}
	

}
