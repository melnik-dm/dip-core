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
package ru.dip.ui.action.open;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.ui.Messages;
import ru.dip.ui.action.hyperlink.ReqLink;

public class OpenInDocumentAction extends SelectionDispatchAction {
			
	protected OpenInDocumentAction(IWorkbenchSite site) {
		super(site);
		setText(Messages.OpenInDocumentAction_Action_name);		}

	
	@Override
	public void run(IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof DnfoTable) {
			DnfoTable table = (DnfoTable) selection.getFirstElement();				
			ReqLink.openElementInTable((IDipDocumentElement) table.parent());
		}
	}
	
}

