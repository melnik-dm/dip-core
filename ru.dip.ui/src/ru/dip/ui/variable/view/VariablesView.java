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
package ru.dip.ui.variable.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.vars.IVarContainerListener;
import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.table.editor.DipTableEditor;

public class VariablesView extends ViewPart implements IVarContainerListener {

	public final static String ID = "ru.dip.ui.view.variables";

	private Composite fComposite;
	private VariablesComposite fVariablesComposite;
	private VarContainer fVarContainer;

	@Override
	public void createPartControl(Composite parent) {
		fComposite = CompositeFactory.full(parent);
		fComposite.setBackground(ColorProvider.WHITE);
		fVariablesComposite = new VariablesComposite(fComposite);
	}

	@Override
	public void setFocus() {
		fVariablesComposite.setFocus();
	}

	// ==========================================
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
		if (project != null && !fVariablesComposite.isDispose()) {
			fVarContainer = project.getVariablesContainer();
			fVariablesComposite.setInput(fVarContainer);
			fVarContainer.addListener(this);
		}
	}

	@Override
	public void variablesChanged() {
		if (fVariablesComposite != null) {
			fVariablesComposite.refresh();
		}
	}

	@Override
	public void dispose() {
		if (fVarContainer != null) {
			fVarContainer.removeListener(this);
		}
		super.dispose();
	}
}
