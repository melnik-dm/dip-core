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
package ru.dip.ui.table.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;

/**
 * Singlton
 * 
 * Регистр открытых редакторов
 */
public class DipEditorRegister {
	
	public  static final DipEditorRegister instance = new DipEditorRegister();
	
	private final Map<IDipParent, DipTableEditor> fOpenEditors = new HashMap<>();
	
	private DipEditorRegister() {}
		
	public void registerEditor(DipTableEditor editor) {
		fOpenEditors.put(editor.model().getContainer(), editor);
	}
	
	public void unregisterEditor(DipTableEditor editor) {
		fOpenEditors.remove(editor.model().getContainer());
	}
	
	public List<DipTableEditor> findEditors(IDipDocumentElement dde) {
		List<IDipParent> parentChain = DipUtilities.getParentsChain(dde);		
		return parentChain.stream()
			.map(fOpenEditors::get)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	
	public Map<DipTableEditor, List<IDipDocumentElement>> findEditorsAndGroup(IDipElement[] dipElements) {
		Map<DipTableEditor, List<IDipDocumentElement>> ddeByEditor = new HashMap<DipTableEditor, List<IDipDocumentElement>>();
		for (IDipElement dipElement: dipElements) {
			if (dipElement instanceof IDipDocumentElement) {
				IDipDocumentElement dde = (IDipDocumentElement) dipElement;
				List<DipTableEditor> editors = findEditors(dde);
				for (DipTableEditor editor: editors) {
					ddeByEditor.computeIfAbsent(editor, k -> new ArrayList<>()).add(dde);
				}				
			}		
		}
		return ddeByEditor;
	}

}
