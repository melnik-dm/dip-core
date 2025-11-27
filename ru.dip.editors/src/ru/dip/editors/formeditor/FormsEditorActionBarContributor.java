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
package ru.dip.editors.formeditor;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

import ru.dip.core.form.model.Field;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.fieldpage.FieldControl;
import ru.dip.editors.formeditor.fieldpage.TextFieldControl;
import ru.dip.editors.md.MDEditorContributor.OpenViewAction;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class FormsEditorActionBarContributor extends MultiPageEditorActionBarContributor {

	private FormsEditor fReqEditor;
	
	public FormsEditorActionBarContributor() {	
	}
	
	private RedoAction fRedoAction;
	private UndoAction fUndoAction;
	
	public IAction getRedoAction() {
		return fRedoAction;
	}
	
	public IAction getUndoAction() {
		return fUndoAction;
	}
	
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(BoldAction.instance());	
		toolBarManager.add(ItalicAction.instance());
		toolBarManager.add(CommentAction.instance());
		toolBarManager.add(NumberListAction.instance());
		toolBarManager.add(MarkerListAction.instance());
		toolBarManager.add(LinkAction.instance());
		toolBarManager.add(CodeAction.instance());
		toolBarManager.add(ParagraphAction.instance());

		toolBarManager.add(new AutoSizeAction());
		toolBarManager.add(new UpAction());
		toolBarManager.add(new DownAction());	
	}
	
	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
	}
	
	@Override
	public void setActiveEditor(IEditorPart activeEditor) {
		IActionBars actionBars = getActionBars();
		IWorkbenchPart page = activeEditor;
		if (actionBars != null && page instanceof FormsEditor) {
			fReqEditor = (FormsEditor) page;
			fRedoAction = new RedoAction(fReqEditor);
			fUndoAction = new UndoAction(fReqEditor);
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
								
			BoldAction.instance().setEnabled(false);
			ItalicAction.instance().setEnabled(false);
			CommentAction.instance().setEnabled(false);
			CodeAction.instance().setEnabled(false);	
			NumberListAction.instance().setEnabled(false);
			MarkerListAction.instance().setEnabled(false);
			LinkAction.instance().setEnabled(false);
			ParagraphAction.instance().setEnabled(false);		
		}	
	}
	
	@Override
	public void setActivePage(IEditorPart activeEditor) {
		
	}
		
	public void setEditViewActivePage(IActionBars actionBars, FormsEditor activeEditor) {		
		if (actionBars != null && activeEditor instanceof FormsEditor) {			
			fReqEditor = (FormsEditor) activeEditor;
			fRedoAction = new RedoAction(fReqEditor);
			fUndoAction = new UndoAction(fReqEditor);
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), fUndoAction);
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), fRedoAction);
		}	
	}
	
	public void contributeEditView(IActionBars bars, IEditorPart editor) {
		if (bars == null) {
			return;
		}
		init(bars);			
		setActiveEditor(editor);	
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.remove(OpenViewAction.ID);
		toolbar.update(true);
	}
	
	
	class UndoAction extends Action {
		
		private IAction undoAction;
		
		public UndoAction(FormsEditor editor) {
			undoAction = editor.getTextEditor().getAction(ActionFactory.UNDO.getId());
			undoAction.addPropertyChangeListener(new IPropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (Messages.ReqEditorActionBarContributor_UndoEnabledProperty.equals(event.getProperty())) {
						setEnabled(undoAction.isEnabled());
					}
					firePropertyChange(event);
				}
			});
		}
				
		@Override
		public void run() {
			if (!TableSettings.isUndoRedoMode()) {
				FieldControl control = fReqEditor.getFieldsPage().getFocusField();
				if (control instanceof TextFieldControl) {
					TextFieldControl text = (TextFieldControl) control;
					text.doUndo();
				}
			} else {
				runAction(undoAction);
			}			
		}
		
		@Override
		public boolean isEnabled() {
			if (!TableSettings.isUndoRedoMode()) {		
				return isUndoFieldEnabled();
			} else {
				return isUndoPageEnabled();
			}
		}
		
		private boolean isUndoFieldEnabled() {
			if (!undoAction.isEnabled()) {
				return false;
			}			
			FieldControl control = fReqEditor.getFieldsPage().getFocusField();
			if (control instanceof TextFieldControl) {
				TextFieldControl text = (TextFieldControl) control;
				return text.canUndo();
			}
			return false;
		}
		
		private boolean isUndoPageEnabled() {
			if (fReqEditor.getFieldsPage().isActive()) {
				boolean hasChanged = fReqEditor.getFieldsPage().updateTagValues();
				if (hasChanged) {
					undoAction.setEnabled(true);
				} 
			}
			return undoAction.isEnabled();
		}
			
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
		}
		
		@Override
		public String getText() {
			return undoAction.getText();
		}
		
		@Override
		public String getDescription() {
			return undoAction.getDescription();
		}
		
		@Override
		public ImageDescriptor getImageDescriptor() {
			return undoAction.getImageDescriptor();
		}
		
	}
	
	class RedoAction extends Action {
		
		private IAction redoAction;
		
		public RedoAction(FormsEditor editor) {
			redoAction = editor.getTextEditor().getAction(ActionFactory.REDO.getId());
			redoAction.addPropertyChangeListener(new IPropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if (Messages.ReqEditorActionBarContributor_RedoEnabledProperty.equals(event.getProperty())) {
						setEnabled(redoAction.isEnabled());
					}
					firePropertyChange(event);
				}
			});
		}
		
		@Override
		public void run() {
			if (!TableSettings.isUndoRedoMode()) {
				FieldControl control = fReqEditor.getFieldsPage().getFocusField();
				if (control instanceof TextFieldControl) {
					TextFieldControl text = (TextFieldControl) control;
					text.doRedo();
				}
			} else {
				runAction(redoAction);
			}
		}		
		
		@Override
		public boolean isEnabled() {
			if (!TableSettings.isUndoRedoMode()) {
				FieldControl control = fReqEditor.getFieldsPage().getFocusField();
				if (control instanceof TextFieldControl) {
					TextFieldControl text = (TextFieldControl) control;
					return text.canRedo();
				}
				return false;
			} else {			
				return redoAction.isEnabled();
			}
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
		}
		
		@Override
		public String getText() {
			return redoAction.getText();
		}
		
		@Override
		public String getDescription() {
			return redoAction.getDescription();
		}
		
		@Override
		public ImageDescriptor getImageDescriptor() {
			return redoAction.getImageDescriptor();
		}	
	}

	private void runAction(IAction action) {
		// get old values	
		List<FieldControl> controls = fReqEditor.getFieldsPage().fieldControls();
		List<Field> fields = controls.stream().map(FieldControl::getField).
				collect(Collectors.toList());
		List<String> oldvalues = fields.stream().map(Field::getValue)
				.collect(Collectors.toList());
		// run und
		fReqEditor.setUndoRedoProcessing(true);
		action.run();
		// update control
		List<String> newvalues = fields.stream().map(Field::getValue)
				.collect(Collectors.toList());			
		boolean update = false;			
		for (int i = 0; i < oldvalues.size(); i++) {
			String oldValue = oldvalues.get(i);
			String newValue = newvalues.get(i);
			if (!oldValue.equals(newValue)) {
				updateControl(controls.get(i));
				update = true;
				break;
			}
		}
		
		if (!update) {
			updateFieldPage();
		}
		fReqEditor.getFieldsPage().checkHint();
		fReqEditor.setUndoRedoProcessing(false);		
		fReqEditor.checkDirty();
	}
	
	private void updateFieldPage() {
		FieldControl control = fReqEditor.getFieldsPage().getFocusField();
		if (control instanceof TextFieldControl) {
			TextFieldControl textFieldControl = (TextFieldControl) control;
			String oldContent = textFieldControl.getControlValue();
			int offset = textFieldControl.getOffset();
			fReqEditor.getFieldsPage().updateValues();
			String newContent = textFieldControl.getControlValue();
			int newCursorPosition = evaluateCursorPosition(oldContent, newContent, offset);
			textFieldControl.setCursor(newCursorPosition);
		} else {
			fReqEditor.getFieldsPage().updateValues();
		}
	}
	
	private void updateControl(FieldControl control) {
		if (control instanceof TextFieldControl) {
			TextFieldControl textFieldControl = (TextFieldControl) control;
			setFocusToField(textFieldControl);
			fReqEditor.getFieldsPage().scrolToField(textFieldControl);
		} else {
			fReqEditor.getFieldsPage().updateValues();
		}
	}
	
	private void setFocusToField(TextFieldControl textFieldControl) {
		String oldContent = textFieldControl.getControlValue();
		int offset = textFieldControl.getOffset();
		fReqEditor.getFieldsPage().updateValues();
		String newContent = textFieldControl.getControlValue();
		int newCursorPosition = evaluateCursorPosition(oldContent, newContent, offset);
		textFieldControl.setFocus();
		textFieldControl.setCursor(newCursorPosition);
	}
	
	private int evaluateCursorPosition(String oldContent, String newContent, int oldPosition) {
		char[] oldChars = oldContent.toCharArray();
		char[] newChars = newContent.toCharArray();	
		int newPos = 0;
		if (oldContent.length() > newContent.length()) {			
			for (int i = 0; i < newContent.length(); i++) {
				if (oldChars[i] != newChars[i]) {
					break;
				} else {
					newPos++;
				}
			}
			return newPos;
		} else {
			for (int i = 0; i < oldContent.length(); i++) {
				if (oldChars[i] != newChars[i]) {
					break;
				} else {
					newPos++;
				}
			}
			return newPos + (newContent.length() - oldContent.length());
		}
	}
	
	public void disposeEditView(IActionBars bars) {
		if (bars != null) {		
			IToolBarManager toolbar = bars.getToolBarManager();
			toolbar.remove(BoldAction.ID);
			toolbar.remove(ItalicAction.ID);
			toolbar.remove(CommentAction.ID);
			toolbar.remove(NumberListAction.ID);
			toolbar.remove(MarkerListAction.ID);
			toolbar.remove(LinkAction.ID);
			toolbar.remove(CodeAction.ID);
			toolbar.remove(ParagraphAction.ID);
			
			toolbar.remove(AutoSizeAction.ID);
			toolbar.remove(UpAction.ID);
			toolbar.remove(DownAction.ID);
			toolbar.update(true);
		}	
	}
	
	class AutoSizeAction extends Action {
		
		private static final String ID = "ru.dip.editors.form.action.autosize"; //$NON-NLS-1$

		public AutoSizeAction() {
			setToolTipText(Messages.FormsEditorActionBarContributor_UpdateTextboxActionName);
			setImageDescriptor(ImageProvider.MATCH_HEIGHT);
			setId(ID);
		}
		
		@Override
		public void run() {			
			//setToolTipText("Update Textbox Height");
			fReqEditor.getFieldsPage().autoHeightTextField();
		}
		
	}

	
	class UpAction extends Action {

		private static final String ID = "ru.dip.editors.form.action.upaction"; //$NON-NLS-1$
		
		public UpAction() {		
			setToolTipText(Messages.FormsEditorActionBarContributor_PreviousTextFieldActionName);
			setImageDescriptor(ImageProvider.UP_DESCRIPTOR);
			setId(ID);
		}
		
		@Override
		public void run() {
			fReqEditor.getFieldsPage().goToPreviousTextField();
		}
		
	}
	
	class DownAction extends Action {
		
		private static final String ID = "ru.dip.editors.form.action.downaction"; //$NON-NLS-1$
		
		public DownAction() {
			setToolTipText(Messages.FormsEditorActionBarContributor_NextTextFieldActionName);
			setImageDescriptor(ImageProvider.DOWN_DESCRIPTOR);
			setId(ID);
		}
		
		@Override
		public void run() {
			fReqEditor.getFieldsPage().goToNextTextField();
		}
		
	}

	
}
