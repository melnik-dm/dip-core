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
package ru.dip.editors.formeditor.model;

import ru.dip.core.form.model.CorePositionModel;
import ru.dip.core.form.model.CoreFormModel;
import ru.dip.core.form.model.Tag;
import ru.dip.editors.formeditor.FormsEditor;

public class FormModel extends CoreFormModel {
	
	private FormsEditor fEditor;
		
	public FormModel(FormsEditor editor) {
		fEditor = editor;
	}
	
	public FormModel(){
		
	}
	
	public Tag findTagByOffset(int offset, int endOffset){
		return fEditor.getTextEditor().getPositionModel().findTagByOffset(offset, endOffset);
	}
	
	@Override
	public CorePositionModel getPositionModel() {
		return getFormEditor().getTextEditor().getPositionModel();
	}
	
	//===========================
	// getters
	
	public FormsEditor getFormEditor(){
		return fEditor;
	}
	
}
