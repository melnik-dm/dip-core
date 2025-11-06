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
package ru.dip.editors;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ru.dip.editors.editview.EditViewPartListener;
import ru.dip.editors.incorrectview.IncorrectViewPartListener;
import ru.dip.editors.merge.CompareWorkbenchListener;
import ru.dip.editors.spellcheck.SpellCheckViewListener;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {		
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.addPartListener(new ReqWorkbenchListener());
				page.addPartListener(new EditViewPartListener());
				page.addPartListener(new IncorrectViewPartListener());	
				page.addPartListener(new SpellCheckViewListener());
				page.addPartListener(new CompareWorkbenchListener());
			}
		});					
	}

}
