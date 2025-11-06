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
package ru.dip.ui.glossary;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.glossary.IGlossaryListener;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.DipTableEditor;

public class GlossaryView extends ViewPart implements IGlossaryListener {

	public final static String ID = Messages.GlossaryView_ID;
	
	private Composite composite;
	private GlossaryComposite fGlossaryComposite;
	private GlossaryFolder fGlossaryFolder;

	@Override
	public void createPartControl(Composite parent) {
		composite = CompositeFactory.full(parent);
		composite.setBackground(ColorProvider.WHITE);
		fGlossaryComposite = new GlossaryComposite(composite);
	}

	@Override
	public void setFocus() {

	}

	//==========================================
	// update
	
	public void update() {
		Thread thread = new Thread(() -> {
		Display.getDefault().asyncExec(() -> {
				DipProject project = WorkbenchUtitlities.getDipProjectFromOpenedEditor();
				setViewerInput(project);
			});
		});
		thread.start();
	}

	public void update(DipTableEditor tableEditor) {
		Thread thread = new Thread(() -> {
			Display.getDefault().asyncExec(() -> {
				DipProject project = tableEditor.model().dipProject();
				setViewerInput(project);				
			});
		});
		thread.start();
	}
	
	private synchronized void setViewerInput(DipProject project) {
		if (project != null && !fGlossaryComposite.isDispose()) {
			fGlossaryFolder = project.getGlossaryFolder();
			fGlossaryComposite.setInput(fGlossaryFolder);
			fGlossaryFolder.addListener(this);
		}
	}
	
	@Override
	public void glossaryChanged() {
		if (fGlossaryComposite != null) {
			fGlossaryComposite.refresh();
		}
	}
	
	@Override
	public void dispose() {
		if (fGlossaryFolder != null) {		
			fGlossaryFolder.removeListener(this);
		}
		super.dispose();
	}

}
