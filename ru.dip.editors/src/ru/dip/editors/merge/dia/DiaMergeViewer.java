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
package ru.dip.editors.merge.dia;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.utilities.ui.CompositeFactory;

public class DiaMergeViewer extends ContentViewer {

	// controls
	private Composite fParent;
	private Composite fMainComposite;	
	private Composite fLeftComposite;
	private Composite fRightComposite;	
	private Label fLeftLabel;
	private Label fRightLabel;	
	// model	
	private CompareConfiguration fConfiguration;
	private CompareEditorInput fCompareContainer;	
	private MergeDiaElement fLeftElement;
	private MergeDiaElement fRightElement;
	private Object fInput;

		
	public DiaMergeViewer(Composite parent, CompareConfiguration config) {
		fParent = parent;
		fConfiguration = config;		
		fCompareContainer = (CompareEditorInput) fConfiguration.getContainer();	
		buildControl();
	}
	
	private void buildControl() {	
		fParent.setLayout(new GridLayout());		
		fMainComposite = new Composite(fParent, SWT.BORDER);
		fMainComposite.setLayout(new GridLayout(2, true));

		// labels
		Composite leftLabel = new Composite(fMainComposite, SWT.NONE);
		leftLabel.setLayout(new GridLayout());
		fLeftLabel = new Label(leftLabel, SWT.NONE);

		Composite rightLabel = new Composite(fMainComposite, SWT.NONE);
		rightLabel.setLayout(new GridLayout());
		fRightLabel = new Label(rightLabel, SWT.NONE);
		// content

		fLeftComposite =CompositeFactory.fullBorder(fMainComposite);
		fLeftComposite.setLayout(new FillLayout());
				
		fRightComposite = CompositeFactory.fullBorder(fMainComposite);
		fRightComposite.setLayout(new FillLayout());
	}
			
	@Override
	public Control getControl() {
		return fMainComposite;
	}
	
	@Override
	public void setInput(Object input) {		
		fInput = input;
		if (input == null) {
			return;			
		}
				
		if (input instanceof ICompareInput) {
			ICompareInput compareInput = (ICompareInput) input;	
			// read
			fLeftElement = new MergeDiaElement(fCompareContainer, compareInput.getLeft());
			fLeftLabel.setText(fConfiguration.getLeftLabel(input));		
			fRightElement = new MergeDiaElement(fCompareContainer, compareInput.getRight());
			fRightLabel.setText(fConfiguration.getRightLabel(input));									
			// controls
			fLeftElement.createControls(fLeftComposite);
			fRightElement.createControls(fRightComposite);	
		}			
	}
	
	
	//====================================

	@Override
	public Object getInput() {		
		return fInput;
	}

	@Override
	public ISelection getSelection() {		
		return null;
	}

	@Override
	public void refresh() {}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {}
	
}
