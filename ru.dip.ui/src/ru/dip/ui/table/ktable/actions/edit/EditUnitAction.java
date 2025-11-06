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
package ru.dip.ui.table.ktable.actions.edit;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;

import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.undo.CancelledDocumentAction;
import ru.dip.ui.table.ktable.model.IDipTableElement;

public class EditUnitAction extends DocumentAction implements CancelledDocumentAction {

	private final String fOldContent;
	private final String fNewContent;
	private String fUnitPresentationId;
	private IDipUnit fUnit;
	
	
	public EditUnitAction(KTableComposite tableComposite, String oldContent, String newContent, UnitPresentation unitPresentation) {
		this(tableComposite, oldContent, newContent, unitPresentation.getDipUnit());
	}
	
	public EditUnitAction(KTableComposite tableComposite, String oldContent, String newContent, IDipUnit unit) {
		super(tableComposite);
		fOldContent = oldContent;
		fNewContent = newContent;
		fUnit = unit;
		fUnitPresentationId = DipUtilities.relativeProjectID(fUnit);
	}
	
	@Override
	public void run() {
		try {
			setContent(fUnit, fNewContent);
			fTableComposite.actionStack().pushUndoAction(this);												
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}

	@Override
	public void undo() throws Exception {
		setContent(fOldContent);	
	}

	@Override
	public void redo() throws Exception {
		setContent(fNewContent);
	}
	
	private void setContent(String content) throws IOException {
		Optional<IDipElement> newElement = DipUtilities.findDipElementInProject(fUnitPresentationId,
				fTableComposite.dipProject());		
		if (newElement.isPresent()) {
			IDipTableElement element = setContent((IDipUnit) newElement.get(), content);
			FormPresentation formPresentation = (FormPresentation) ((IDipUnit) newElement.get()).getUnitPresentation().getPresentation();
			formPresentation.updateFieldsFromFile();
			fTableComposite.selectElement(element);
		}
	}


	private IDipTableElement setContent(IDipUnit dipUnit, String content) throws IOException {
		IFile file = dipUnit.resource();		
		FileUtilities.writeFile(file, content);
		Optional<IDipTableElement> optElement = fTableComposite.tableModel().findElementByName(dipUnit);
		if (optElement.isPresent()) {
			IDipTableElement tableElement = optElement.get();
			updateElement(tableElement);
			return tableElement;
		}
		return null;
	}
	
	private void updateElement(IDipTableElement element) {		
		if (!element.isAbstractField()) {
			fTableComposite.tableModel().updateElements(List.of(element));
		}
	}
	
	
}
