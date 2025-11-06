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

import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.ui.Messages;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.utilities.image.ImageProvider;

public class MarkAction extends DocumentAction  {

	private int fMarkId;
		
	public MarkAction(KTableComposite tableComposite, int markId) {
		super(tableComposite);
		fMarkId = markId;
		setText(Messages.MarkAction_ActionName + (markId + 1));
		setImageDescriptor(ImageProvider.BOOKMARK_DESCS[markId]);
		setChecked(true);
	}
	
	public MarkAction(MarkAction original) {
		super(original.fTableComposite);
		fMarkId = original.fMarkId;
	}
	
	private void updateName(IMarkable selectedDipDocElement) {
		setChecked(selectedDipDocElement.isMark(fMarkId));
	}
	
	@Override
	public void run() {
		IDipDocumentElement req = fTableComposite.selector().getLastSelectDipDocElement().strong();
		if (req instanceof IMarkable) {
			IMarkable markable = (IMarkable) req;
			markable.setMark(fMarkId, !markable.isMark(fMarkId));
			fTableComposite.selector().updateCurrentSelection();
			fTableComposite.editor().fireMarkListener();
		}
	}

	@Override
	public void enableOneSelection(IDipDocumentElement selectedDipDocElement) {
		IDipDocumentElement strong = selectedDipDocElement.strong();
		
		if (strong instanceof IMarkable) {
			setEnabled(true);
			updateName((IMarkable) strong);
		} else {
			setEnabled(false);
		}		
	}

}
