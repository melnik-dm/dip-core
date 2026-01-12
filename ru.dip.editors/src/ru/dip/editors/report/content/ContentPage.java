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
package ru.dip.editors.report.content;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.report.ReportEditor;

public class ContentPage extends FormPage {
	
	private Composite fParentComposite;
	private ReportContentComposite fReportContentComposite;
	
	public ContentPage(FormEditor editor, String title) {
		super(editor, "Content", title);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		fParentComposite = managedForm.getForm().getBody();
		fParentComposite.setLayout(new GridLayout());	
		fParentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fParentComposite.setBackground(ColorProvider.WHITE);
		
		fReportContentComposite = new ReportContentComposite(fParentComposite, (ReportEditor) getEditor());
		fReportContentComposite.initialize();
	}

	public void update() {
		if (fReportContentComposite != null) {
			fReportContentComposite.updateTable();
		}
	}
		
}
