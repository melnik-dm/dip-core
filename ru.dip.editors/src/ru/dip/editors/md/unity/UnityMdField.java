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
package ru.dip.editors.md.unity;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.editors.md.field.MdField;

public class UnityMdField extends MdField {
	
	private MdTextField fMdTextField;
	
	public UnityMdField(MdTextField mdTextField, IDipUnit unit, String content) {
		super(unit, content);
		fMdTextField = mdTextField;
	}

	@Override
	protected void addAdditionalFeatures() {
		styledText().setText(content());
		fMdTextField.getCommentManager().addCommentDecorationInSourceViewer();
		fMdTextField.getCommentManager().setCommentModel(document());
	}

}
