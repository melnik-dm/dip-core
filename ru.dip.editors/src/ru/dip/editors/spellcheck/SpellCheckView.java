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
package ru.dip.editors.spellcheck;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.spellcheck.Dictionary;
import ru.dip.core.utilities.spellcheck.SpellCheckError;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.utilities.image.ImageProvider;

public class SpellCheckView extends ViewPart implements IPropertyListener {

	public static final String ID = Messages.SpellCheckView_ID;
	private TreeViewer fViewer;
	
	public SpellCheckView() {
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new CheckSpellAction());
	}
	
	class CheckSpellAction extends Action {
		
		public CheckSpellAction() {
			setImageDescriptor(ImageProvider.SPELL_CHECK_DESCRIPTOR);
			setToolTipText(Messages.SpellCheckView_CpellCheckActionToolTip);
		}
		
		@Override
		public void run() {			
			DipProject dipProject = WorkbenchUtitlities.getDipProjectFromOpenedEditor();	
			fViewer.setInput(dipProject);			
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = CompositeFactory.full(parent);
		
		TreeColumnLayout fTreeColumnlayout = new TreeColumnLayout(true);
		composite.setLayout(fTreeColumnlayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);

		Tree tree= new Tree(composite, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL);
		fViewer = new TreeViewer(tree);		
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		GridData treeLayoutData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		treeLayoutData.grabExcessHorizontalSpace = true;
		tree.setLayoutData(treeLayoutData);
		
        TreeViewerColumn viewerColumn = new TreeViewerColumn(fViewer, SWT.CENTER);		
		TreeColumn fIDColumn = viewerColumn.getColumn();
		fIDColumn.setText(Messages.SpellCheckView_ErrorColumnName);
		fIDColumn.setAlignment(SWT.LEFT);	
		fIDColumn.setResizable(true);
		fTreeColumnlayout.setColumnData(fIDColumn, new ColumnWeightData(30, 0, true));
		
        TreeViewerColumn viewerColumn2 = new TreeViewerColumn(fViewer, SWT.CENTER);		
		TreeColumn fProjectColumn = viewerColumn2.getColumn();
		fProjectColumn.setText(Messages.SpellCheckView_IDColumnName);
		fProjectColumn.setAlignment(SWT.LEFT);	
		fProjectColumn.setResizable(true);
		fTreeColumnlayout.setColumnData(fProjectColumn, new ColumnWeightData(60, 0, true));
		
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
				if (inputElement instanceof DipProject) {
					DipProject dipProject = (DipProject) inputElement;					
					List<SpellCheckError> errors = Dictionary.instance().checkDipParent(dipProject);					
					return errors.toArray();
				}
				return new Object[0];
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		});
		
		fViewer.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				int index = cell.getColumnIndex();
				Object obj = cell.getElement();
				if (obj instanceof SpellCheckError) {
					SpellCheckError error = (SpellCheckError) obj;
					if (index == 0) {
						cell.setText(error.error());
						cell.setImage(ImageProvider.ERROR);
					} if (index == 1) {
						cell.setText(error.source().id());
					}
				}
			}
		});
				
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = fViewer.getStructuredSelection();
				if (selection == null || selection.isEmpty()) {
					return;
				}
				Object obj = selection.getFirstElement();
				if (obj instanceof SpellCheckError) {
					openFile((SpellCheckError) obj);
				}
			}
		});
		
	}
	
	public void openFile(SpellCheckError error) {
		IFile file = (IFile) error.source().resource();
		IEditorPart editorPart = WorkbenchUtitlities.openFile(file);
		if (editorPart instanceof TextEditor) {
			((TextEditor)editorPart).selectAndReveal(error.location().x, error.location().y);
		} else if (editorPart instanceof FormsEditor) {
			FormsEditor dipEditor = (FormsEditor) editorPart;
			dipEditor.selectText(error.error());
		}
	}	

	@Override
	public void setFocus() {
		
	}

	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId != DipTableEditor.UPDATE_VIEWER_EVENT) {
			return;
		}
		boolean visible = getSite().getPage().isPartVisible(this);
		if (!visible) {
			return;
		}
		if (source instanceof DipTableEditor) {
			DipTableEditor editor = (DipTableEditor) source;
			if (editor.kTable().isCheckSpellingEnable()) {
				DipProject dipProject = WorkbenchUtitlities.getDipProjectFromOpenedEditor();
				fViewer.setInput(dipProject);	
				return;
			}
		}		
		fViewer.setInput(""); //$NON-NLS-1$
	}

}
