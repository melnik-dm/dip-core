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
package ru.dip.ui.variable.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.viewer.ListContentProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.variable.VarManager;

public class UndefineVariablesDialog extends Dialog {

	private static final String TITLE = Messages.UndefineVariablesDialog_DialogTitle;
	private static final String ADD_VAR_MSG_INSTR = Messages.UndefineVariablesDialog_DevineVariableInstruction;
	
	private static final int MIN_WIDTH = 400;
	private static final int MIN_HEIGHT = 400;
	private static final int MAX_HEIGHT = 700;
	
	private final List<String> fVars;
	private final IVarContainer fVarContainer;
	
	public UndefineVariablesDialog(Shell parentShell, IVarContainer varContainer,
			Collection<String> words) {
		super(parentShell);
		fVars = new ArrayList<>(words);
		Collections.sort(fVars);
		fVarContainer = varContainer;
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(TITLE);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.y > MAX_HEIGHT) {
			p.y = MAX_HEIGHT;
		}
		if (p.y < MIN_HEIGHT) {
			p.y = MIN_HEIGHT;
		}
		
		if (p.x < MIN_WIDTH) {
			p.x = MIN_WIDTH;
		}
		return p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeFactory.full(parent);		
		ControlFactory.label(composite, ADD_VAR_MSG_INSTR);
	
		TreeViewer fViewer = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL);
		fViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer.setContentProvider(new ListContentProvider(fVars));						
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {							
				Object obj = fViewer.getStructuredSelection().getFirstElement();
				String term = obj.toString();
				if (VarManager.openNewVariableDialog(getShell(), fVarContainer, term)) {
					fVars.remove(term);
				}
				fViewer.refresh();
			}
		});
		
		fViewer.setInput(fVars);		
		return composite;
	}

}
