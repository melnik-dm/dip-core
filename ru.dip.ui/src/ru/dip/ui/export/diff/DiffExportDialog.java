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
package ru.dip.ui.export.diff;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.ui.export.ExportDialog;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class DiffExportDialog extends ExportDialog {
	
	private final String fHash1;
	private final String fHash2;
	private EntryTextComposite fHashEntry1;
	private EntryTextComposite fHashEntry2;
		
	public DiffExportDialog(Shell shell, DipProject project, String hash1, String hash2) {
		super(shell, project);
		fHash1 = hash1;
		fHash2 = hash2;
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("Diff Export");
	}
	
	@Override
	protected void createExtensionComposite(Composite parent) {
		createDiffComposite(parent);
	}
		
	private void createDiffComposite(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).horizontal().build();
		fHashEntry1 = new EntryTextComposite(composite, "Commit 1: ");
		fHashEntry1.setValue(fHash1);
		fHashEntry1.setEnabled(false);
		fHashEntry2 = new EntryTextComposite(composite, "Commit 2: ");
		fHashEntry2.setValue(fHash2);
		fHashEntry2.setEnabled(false);		
	}
}
