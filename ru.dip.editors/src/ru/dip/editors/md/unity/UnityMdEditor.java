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
package ru.dip.editors.md.unity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.md.UnityMdInput;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.editors.md.comment.ICommentManager;
import ru.dip.editors.md.comment.ICommentManagerHolder;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.editors.md.unity.action.RedoAction;
import ru.dip.editors.md.unity.action.UndoAction;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.preferences.MdPreferences.MdPreferenciesListener;

public class UnityMdEditor extends EditorPart implements ICommentManagerHolder, MdPreferenciesListener {

	private IContainer fFolder;
	private IDipParent fDipParent;
	private List<IDipUnit> fUnits;
	private boolean fShowComment = false;

	private ScrolledComposite fScroll;
	private Composite fMainComposite;
	
	private UndoAction fUndoAction = new UndoAction();
	private RedoAction fRedoAction = new RedoAction();
	
	private List<MdTextField> fMdTextFields = new ArrayList<MdTextField>();

	public UnityMdEditor() {
		MdPreferences.instance().addListener(this);
	}
	
	@Override
	public String getPartName() {
		if (fFolder != null) {
			return fFolder.getName() + " <Markdown>";
		}
		return super.getPartName();
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof UnityMdInput) {
			UnityMdInput unityMdInput = (UnityMdInput) input;
			fFolder = unityMdInput.getContainer();
			IDipElement element = DipUtilities.findElement(fFolder);
			if (element instanceof IDipParent) {
				fDipParent = (IDipParent) element;
				fUnits = fDipParent.getDipDocChildrenList().stream()
					.filter(IDipUnit.class::isInstance)
					.map(IDipUnit.class::cast)
					.filter(un -> un.getUnitPresentation().getUnitType().isMarkdown())
					.collect(Collectors.toList());
			}
		}
	}
	
	//==================================
	// control
	
	@Override
	public void createPartControl(Composite parent) {
		fScroll = new ScrolledComposite(parent, SWT.V_SCROLL);		
		fMainComposite =  CompositeBuilder.instance(fScroll).full().notIndetns().build();
		try {
			for (IDipUnit unit: fUnits) {
				String content = FileUtilities.readFile(unit.resource());		
				MdTextField mdField = new MdTextField(unit, this, fMainComposite, content);
				fMdTextFields.add(mdField);
				addModifyListener(mdField);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fScroll.setExpandHorizontal(true);
		fScroll.setExpandVertical(true);
		fScroll.setContent(fMainComposite);
		fScroll.setMinSize( fScroll.computeSize( SWT.DEFAULT, SWT.DEFAULT ));	

	}
	
	private void addModifyListener(MdTextField mdField) {
		mdField.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {					
				if (!mdField.isTextFit()) {
					int oldWidth = fMainComposite.getSize().x;							
					fMainComposite.layout();
					Point p = fMainComposite.computeSize(oldWidth, SWT.DEFAULT);
					fScroll.setMinSize(oldWidth, p.y);
				}				
				firePropertyChange(PROP_DIRTY);
			}

		});
	}
	
	@Override
	public void mardownPreferencesChanged() {
		PartitionStyles.updateTokens();
		for (MdTextField field: fMdTextFields) {
			field.updateViewerConfiguration();
		}
	}
	
	//==========================
	// focus
	
	@Override
	public void setFocus() {
		if (!fMdTextFields.isEmpty()) {
			fMdTextFields.get(0).setFocus();
		}
	}
	
	public void changeFocus(MdTextField field) {
		fMdTextFields.stream()
		.filter(f -> f != field)
		.forEach(MdTextField::desellect);
	}
	
	
	public MdTextField getActiveField() {
		for (MdTextField mdField: fMdTextFields) {
			if (mdField.isFocus()) {
				return mdField;
			}
		}
		return null;
	}
	
	//==========================
	// scroll

	
	public Point getVisibleArea() {
		 int height = fScroll.getClientArea().height;
		 int origin = fScroll.getOrigin().y;
		 return new Point(origin, height + origin);
	}
	
	public void scrollUp() {
		Point origin = fScroll.getOrigin();
		origin.y -= 20;
		if (origin.y < 0) {
			origin.y = 0;
		}
		fScroll.setOrigin(origin);
	}
	
	public void scrollDown() {
		Point origin = fScroll.getOrigin();
		origin.y += 20;
		fScroll.setOrigin(origin);
	}
	
	//===========================
	// comments
	
	@Override
	public ICommentManager getCommentManager() {
		MdTextField activeField = getActiveField();
		if (activeField != null) {
			return activeField.getCommentManager();
		}
		
		return null;
	}

	public boolean isShowComment() {
		return fShowComment;
	}

	public void showComment() {
		fShowComment = !fShowComment;
		fMdTextFields.stream()
			.map(MdTextField::getCommentManager)
			.forEach(UnityCommentManager::showComment);
	}
	
	//===========================
	// actions
	
	public void up(MdTextField field) {
		int index = fMdTextFields.indexOf(field);
		if (index > 0) {
			fMdTextFields.get(index - 1).setEndCaret();
		}
		
	}
	
	public void down(MdTextField field) {
		int index = fMdTextFields.indexOf(field);
		if (index < fMdTextFields.size() - 1) {
			fMdTextFields.get(index + 1).setStartCaret();
		}
		
	}

	//===========================
	// save
	
	@Override
	protected void firePropertyChange(int propertyId) {
		super.firePropertyChange(propertyId);
	}
	
	@Override
	public boolean isDirty() {
		return fMdTextFields.stream().anyMatch(MdTextField::isDirty);
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		fMdTextFields.forEach(MdTextField::save);
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	//=============================
	// getters

	public UndoAction getUndoAction() {
		return fUndoAction;
	}
	
	public RedoAction getRedoAction() {
		return fRedoAction;
	}
	
	public DipProject getDipProject() {
		return fDipParent.dipProject();
	}

}
