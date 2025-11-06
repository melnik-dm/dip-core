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
package ru.dip.editors.editview;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.md.MDEditor;
import ru.dip.editors.report.ReportEditor;
import ru.dip.table.editor.MultiPageTableEditor;
import ru.dip.ui.table.editor.DipTableEditor;

public class EditViewPart extends ViewPart implements IPropertyListener, IFolderDirtyListener {

	public static final String ID = Messages.EditViewPart_ID;
	public static final String VIEW_NAME = Messages.EditViewPart_Name;
	
	private IEditorPart fCurrentEditor;
	private Composite fMaincomposite;
	private Composite fParentComposite;
	
	private ViewEditPartSite fViewEditSite; 
	private IFile fCurrentFile;	
	private DipTableEditor fCurrentTable;
	private DipUnit fCurrentDipUnit;
	private DipFolder fCuurentFolder;
	
	public EditViewPart() {
	}

	@Override
	public void createPartControl(Composite parent) {
		fParentComposite = parent;
		fMaincomposite = new Composite(parent, SWT.NONE);
		fMaincomposite.setLayout(new FillLayout());
	}

	@Override
	public void setFocus() {

	}
	
	private IEditorSite getEditorSite(IEditorPart editor) {	
		if (fViewEditSite == null) {
			fViewEditSite = new ViewEditPartSite((PartSite) getSite(), editor);
		}	
		return fViewEditSite;
	}
	
	@Override
	public void propertyChanged(Object source, int propId) {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				if (propId == DipTableEditor.SELECTION_EVENT || propId == DipTableEditor.VISIBLE_EVENT) {
					if (getSite().getPage() != null) {
						boolean visible = getSite().getPage().isPartVisible(EditViewPart.this);
						if (visible && source instanceof DipTableEditor) {
							updateWithEditor((DipTableEditor) source);
						}
					}
				}
			}
		});

	}
	
	private void updateWithEditor(DipTableEditor editor) {	
		fCurrentTable = editor;
		Object select = fCurrentTable.kTable().selector().getSelectedOneDipDocElement();
		updateSelection(select);
	}
	
	public void updateSelection(Object select) {
		if (fCurrentDipUnit != select) {
			checkSave();
		}
		if (select instanceof IDipDocumentElement) {
			IDipDocumentElement dipDocElement = (IDipDocumentElement) select;
			if ( (dipDocElement instanceof IncludeFolder && dipDocElement.parent().isReadOnly())
					|| dipDocElement.isReadOnly()) {
				clearComposite();
				Label label = new Label(fMaincomposite, SWT.NULL);
				label.setText(DipUtilities.readOnlyOpenMessage(dipDocElement));
				fMaincomposite.getLayout();
				fParentComposite.layout();
				setPartName(dipDocElement.name());
				return;
			}			
		}
				
		if (select instanceof DipUnit) {
			fCuurentFolder = null;
			fCurrentDipUnit = (DipUnit) select;
			IResource res = fCurrentDipUnit.resource();
			if (res instanceof IFile && fCurrentFile != res) {
				fCurrentFile = (IFile) res;
				setNewEditor(fCurrentFile);
			}			
		} else if (select instanceof DipFolder) {
			fCurrentFile = null;
			fCurrentDipUnit = null;
			fCuurentFolder = null;
			setNewEditor(null);
			createFolderPropertiesComposite((DipFolder) select);
		} else {
			fCurrentFile = null;
			fCurrentDipUnit = null;
			fCuurentFolder = null;
			setNewEditor(null);
		}
	}
	
	public void checkSave() {
		if (fCurrentEditor != null && fCurrentEditor.isDirty()) {
			if (fCurrentEditor instanceof FormsEditor 
					&& !checkSaveReqEditor((FormsEditor) fCurrentEditor)) {			
					return;
			}			
			askSave();
		}
	}
	
	private boolean checkSaveReqEditor(FormsEditor dipEditor) {
		return  dipEditor.isFieldsPageDirty();
	}
	
	private void askSave() {
		boolean save = MessageDialog.openQuestion(getSite().getShell(), Messages.EditViewPart_SaveDialogTitle, fCurrentFile.getName() + Messages.EditViewPart_QuestionSave);
		if (save) {
			fCurrentEditor.doSave(null);
			if (fCurrentTable.kTable() != null && !fCurrentTable.kTable().isDisposed()) {
				fCurrentTable.kTable().update();  //ddd
			}
		}
	}
	
	private void setNewEditor(IFile file) {
		if (file == null) {
			setPartName(VIEW_NAME);
			clearComposite();
		} else {
			setPartName(file.getName());
			clearComposite();
			fCurrentEditor = createEditor(file);
			if (fCurrentEditor != null) {
				initEditor();
			}				
			if (fCurrentEditor != null && fCurrentEditor instanceof TextEditor) {
				TextEditor textEditor = (TextEditor) fCurrentEditor;
				textEditor.setWordWrap(true);
			}
			
			if (fCurrentEditor instanceof FormsEditor) {
				IEditorSite site = getEditorSite(fCurrentEditor);
				if (site instanceof ViewEditPartSite) {
					ViewEditPartSite viewEditPartSite = (ViewEditPartSite) site;
					//((FormsEditor) fCurrentEditor).setActionBarContributor(viewEditPartSite.getReqEditorContributor(fCurrentEditor));
					viewEditPartSite.initReqEditorContributor(fCurrentEditor);	

				}
			} else if (fCurrentEditor instanceof MDEditor) {		
				IEditorSite site = getEditorSite(fCurrentEditor);			
				if (site instanceof ViewEditPartSite) {					
					ViewEditPartSite viewEditPartSite = (ViewEditPartSite) site;
					viewEditPartSite.initMDEditorContributor(fCurrentEditor);	
				}			
			}
			addSaveAction();
		}
	}
	
	private void createFolderPropertiesComposite(DipFolder folder) {
		fCuurentFolder = folder;
		setPartName(fCuurentFolder.name());	
		DipFolderPropertiesComposite propertiesComposite = new DipFolderPropertiesComposite(fMaincomposite, folder, fCurrentTable.kTable());
		propertiesComposite.setDirtyListener(this);
		fParentComposite.layout();
	}
			
	@Override
	public void setFolderDirty(boolean dirty) {
		if (fCuurentFolder == null){
			return;
		}		
		if (dirty){
			setPartName("*" + fCuurentFolder.name());		 //$NON-NLS-1$
		} else {		
			setPartName(fCuurentFolder.name());
		}		
	}
	
	private void clearComposite() {
		if (fMaincomposite != null) {
			if (fCurrentEditor != null) {
				 getEditorSite(null).getPage().closeEditor(fCurrentEditor, false);			
			}
			if (!fMaincomposite.isDisposed()) {
				fMaincomposite.dispose();
			}
		}
		fCurrentEditor = null;
		fMaincomposite = new Composite(fParentComposite, SWT.NONE);
		fMaincomposite.setLayout(new FillLayout());
		if (fViewEditSite != null) {
			fViewEditSite.disposeContributor();
		}	
	}
	
	private IEditorPart createEditor(IFile file) {			
			UnitType unitType = fCurrentDipUnit.getUnitType();
			if (unitType == UnitType.FORM) {
				return new FormsEditor();
			} else if (unitType == UnitType.TEXT) {
				return new TextEditor();
			} else if (unitType == UnitType.MARKDOWN) {
				return new MDEditor();
			} else if (unitType == UnitType.TABLE) {
				return new MultiPageTableEditor();
			} else if (unitType == UnitType.REPROT_REF) {
				ReportRefPresentation presentation =  (ReportRefPresentation) fCurrentDipUnit.getUnitPresentation().getPresentation();
				fCurrentFile = presentation.getReportFile();
				setPartName(fCurrentFile.getName());
				return new ReportEditor();
			}				
		return null;
	}
	
	private synchronized void initEditor() {
		try {
			IFileEditorInput input = new FileEditorInput(fCurrentFile);		

			fCurrentEditor.init(getEditorSite(fCurrentEditor), input);
			fCurrentEditor.addPropertyListener(new IPropertyListener() {
				
				@Override
				public void propertyChanged(Object source, int propId) {
					if (IEditorPart.PROP_DIRTY == propId) {
						if (fCurrentEditor != null && fCurrentEditor.isDirty()) {
							setPartName("*" + fCurrentFile.getName()); //$NON-NLS-1$
						}
					}
				}
			});
			
			Composite parent2 = new Composite(fMaincomposite, SWT.NONE);
			parent2.setLayout(new FillLayout());
			fCurrentEditor.createPartControl(parent2);			
			//save();
			setPartName(fCurrentFile.getName());
			fMaincomposite.layout();
			fParentComposite.layout();			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	private void addSaveAction() {
		if (fCurrentEditor == null) {
			return;
		}
		IEditorSite site = fCurrentEditor.getEditorSite();		
		site.getActionBars().setGlobalActionHandler(ActionFactory.SAVE.getId(),
			new Action() {
				@Override
				public void run() {
					if (fCurrentEditor != null && fCurrentEditor.isDirty()) {
						save();
				}
			}
		});
	}
	
	public void save() {
		if (fCurrentEditor != null) {
			if (fCurrentEditor instanceof FormsEditor) {
				((FormsEditor) fCurrentEditor).getFieldsPage().updateTagValues();
			}			
			fCurrentEditor.doSave(null);
			fCurrentTable.kTable().update();
			setPartName(fCurrentFile.getName());			
		}
	}
	
	public void checkVisible() {
		boolean visible = getSite().getPage().isPartVisible(this);		
		if (!visible) {
			clearView();
			return;
		}
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(part instanceof DipTableEditor)) {
			clearView();
		}
	}
	
	private void clearView() {
		setPartName(VIEW_NAME);
		fCurrentFile = null;
		clearComposite();
	}

	public IEditorPart currentEditor() {
		return fCurrentEditor;
	}
	
}
