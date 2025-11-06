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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.ui.Messages;

public class FindAbreviationDialog extends Dialog {

	private static final String TITLE = Messages.FindAbreviationDialog_Title;
	private static final String ADD_WORD_MSG_INSTR = Messages.FindAbreviationDialog_AddWordInstruction;
	
	private final List<String> fWords;
	private final GlossaryFolder fGlossFolder;
	
	public FindAbreviationDialog(Shell parentShell, GlossaryFolder glossFolder,
			Collection<String> words) {
		super(parentShell);
		fWords = new ArrayList<>(words);
		Collections.sort(fWords);
		fGlossFolder = glossFolder;
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(TITLE);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.y > 700) {
			p.y = 700;
		}
		if (p.x < 400) {
			p.x = 400;
		}
		return p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeFactory.full(parent);
		
		ControlFactory.label(composite, ADD_WORD_MSG_INSTR);
		
		TreeViewer fViewer = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public boolean hasChildren(Object element) {
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return fWords.toArray();
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {							
				Object obj = fViewer.getStructuredSelection().getFirstElement();
				String term = obj.toString();
				NewGlossFieldDialog dialog = new NewGlossFieldDialog(getShell(), fGlossFolder, term);
				if (dialog.open() == OK) {
					fWords.remove(term);
				}
				fViewer.refresh();
			}
		});
		
		fViewer.setInput(fWords);		
		return composite;
	}

}
