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
package ru.dip.ui.wizard.paste;

import java.util.stream.Stream;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;

public class PasteWizard extends Wizard {
	
	private IDipParent fTargetParent;
	private PasteElement[] fPasteElements;
	private IDipDocumentElement fSelectedElement;  // выделенный ресурс (для вставки после него)
		
	public PasteWizard(IDipParent targetParent, IResource[] resources, IDipDocumentElement selectedElement) {
		fTargetParent = targetParent;
		fSelectedElement = selectedElement;
		fPasteElements = Stream.of(resources).map(r -> new PasteResourceElement(fTargetParent, r)).toArray(PasteElement[]::new);		
		checkStatusAllElements();
		setWindowTitle("Paste Wizard");
	}
	
	
	
	public PasteWizard(IDipParent targetParent, String[] files, IDipDocumentElement selectedElement) {
		fTargetParent = targetParent;
		fSelectedElement = selectedElement;
		fPasteElements = Stream.of(files).map(f -> new PasteFileElement(fTargetParent, f)).toArray(PasteElement[]::new);		
		checkStatusAllElements();
		setWindowTitle("Paste Wizard");	
	}

	public void checkStatusAllElements() {
		String[] names = getAllNames(); 
		Stream.of(fPasteElements).forEach(e -> e.checkStatus(names));
	}
	
	public String[] getAllNames() {
		return Stream.of(fPasteElements).map(PasteElement::getNewName).toArray(String[]::new);
	}
	
	
	@Override
	public void addPages() {		
		addPage(new PastePage(this));
	}
	
	@Override
	public boolean performFinish() {
		return true;		
	}
	
	public IDipParent getTargetParent() {
		return fTargetParent;
	}
	
	public PasteElement[] getPasteElements() {
		return fPasteElements;
	}
	
	public PasteElement[] getCheckedPasteElements() {
		return Stream.of(fPasteElements).filter(PasteElement::isEnable).toArray(PasteElement[]::new);
	}

	public void setTargetParent(IDipParent dipParent) {
		fTargetParent = dipParent;
		fSelectedElement = null;
		for (PasteElement element: fPasteElements) {
			element.setTargetParent(fTargetParent);
		}
		checkStatusAllElements();
	}

	public IDipDocumentElement getSelectedElement() {
		return fSelectedElement;
	}

}
