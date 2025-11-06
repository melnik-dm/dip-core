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
package ru.dip.editors.merge.form;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.utilities.ui.CompositeFactory;

public class FormMergeViewer extends ContentViewer {

	// controls
	private Composite fParent;
	private Composite fMainComposite;	
	private Composite fLeftComposite;
	private Composite fRightComposite;	
	private ScrolledComposite fScroll;
	private Label fLeftLabel;
	private Label fRightLabel;	
	// model	
	private CompareConfiguration fConfiguration;
	private CompareEditorInput fCompareContainer;	
	private MergeFormElement fLeftElement;
	private MergeFormElement fRightElement;
	private Object fInput;

		
	public FormMergeViewer(Composite parent, CompareConfiguration config) {
		fParent = parent;
		fConfiguration = config;		
		fCompareContainer = (CompareEditorInput) fConfiguration.getContainer();	
		buildControl();
	}
	
	private void buildControl() {
		fScroll = new ScrolledComposite(fParent, SWT.V_SCROLL);
		fScroll.setExpandVertical(true);
		// composite
		Composite child = new Composite(fScroll, SWT.NONE);
		child.setLayout(new FillLayout());
		fMainComposite = new Composite(child, SWT.BORDER);
		fMainComposite.setLayout(new GridLayout(3, false));
		// labels
		Composite leftLabel = new Composite(fMainComposite, SWT.NONE);
		leftLabel.setLayout(new GridLayout());
		fLeftLabel = new Label(leftLabel, SWT.NONE);
		Composite middleLabel = new Composite(fMainComposite, SWT.NONE);
		middleLabel.setLayout(new GridLayout());
		Composite rightLabel = new Composite(fMainComposite, SWT.NONE);
		rightLabel.setLayout(new GridLayout());
		fRightLabel = new Label(rightLabel, SWT.NONE);
		// content
		fLeftComposite = CompositeFactory.fullBorder(fMainComposite);
		Composite middle = new Composite(fMainComposite, SWT.NONE);
		middle.setLayout(new GridLayout());
		fRightComposite = CompositeFactory.fullBorder(fMainComposite);

		fScroll.setMinSize(500, 500);
		fScroll.setExpandVertical(true);
		fScroll.setExpandHorizontal(true);
		fScroll.setContent(child);
	}
			
	@Override
	public Control getControl() {
		return fScroll;
	}
	
	@Override
	public void setInput(Object input) {		
		fInput = input;
		if (input == null) {
			fScroll.dispose();			
			return;			
		}
				
		if (input instanceof ICompareInput) {
			ICompareInput compareInput = (ICompareInput) input;	
			// read
			fLeftElement = new MergeFormElement(fCompareContainer, compareInput.getLeft(), 
					!fConfiguration.isLeftEditable());
			fLeftLabel.setText(fConfiguration.getLeftLabel(input));		
			fRightElement = new MergeFormElement(fCompareContainer, compareInput.getRight(), 
					!fConfiguration.isRightEditable());
			fRightLabel.setText(fConfiguration.getRightLabel(input));						
			
			fLeftElement.setCorrespondingElement(fRightElement);
			fRightElement.setCorrespondingElement(fLeftElement);
			// controls
			fLeftElement.createControls(fLeftComposite);
			fRightElement.createControls(fRightComposite);			
		}			
		updateComposite();
				
		if (fCompareContainer == null || input == null) {
			return;
		}
		// save listener
		fCompareContainer.addCompareInputChangeListener((ICompareInput) input, new ICompareInputChangeListener() {
			
			@Override
			public void compareInputChanged(ICompareInput source) {								
				if ((!fLeftElement.isReadOnly() || !fRightElement.isReadOnly())
						&& (fLeftElement.isDirty() || fRightElement.isDirty())) {
					save();
				}
			}
		});	
	}
	
	
	private void updateComposite() {	
		fMainComposite.pack();
		fScroll.setMinHeight(fMainComposite.getClientArea().height);
	}
	
	private void save() {
		if (fLeftElement.isDirty()) {
			fLeftElement.setDirty(false);
			fLeftElement.save();
		}
		if (fRightElement.isDirty()) {
			fRightElement.setDirty(false);
			fRightElement.save();
		}
		fLeftElement.updateAllControlStatus();
		fRightElement.updateAllControlStatus();
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
