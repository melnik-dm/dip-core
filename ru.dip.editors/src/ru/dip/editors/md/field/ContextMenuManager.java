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
package ru.dip.editors.md.field;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.editors.Messages;
import ru.dip.ui.glossary.EditGlossFieldDialog;
import ru.dip.ui.glossary.NewGlossFieldDialog;
import ru.dip.ui.utilities.image.ImageProvider;

public class ContextMenuManager {

	// search menu
	private CommandContributionItem fSearchInWorkspaceItem;
	private CommandContributionItem fSearchInProjectItem;
	private CommandContributionItem fSearchInFileItem;
	private CommandContributionItem fSearchInWorkingSetItem;

	private final StyledText fStyledText;
	private final IServiceLocator fServiceLocator;
	private final IAction fUndoAction;
	private final IAction fRedoAction;

	public ContextMenuManager(StyledText styledText, IServiceLocator serviceLocator, IAction undo, IAction redo) {
		fStyledText = styledText;
		fServiceLocator = serviceLocator;
		fUndoAction = undo;
		fRedoAction = redo;
	}

	public void addContextMenu(IFile file) {
		GlossaryFolder glossFolder = GlossaryFolder.getFor(file);
		if (glossFolder == null) {
			return;
		}
		createSearchTextItems();
		MenuManager popupMenuManager = new MenuManager();
		AddGlossEntryAction addGlossAction = new AddGlossEntryAction(glossFolder);
		popupMenuManager.setRemoveAllWhenShown(true);
		IMenuListener listener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mng) {
				createMenu(mng);
			}

			private IMenuManager createMenu(IMenuManager mng) {
				// undo
				if (fUndoAction != null) {
					fUndoAction.setText(Messages.TextFieldControl_UndoActionName);
					mng.add(fUndoAction);
				}
				// redo
				if (fRedoAction != null) {

					fRedoAction.setText(Messages.TextFieldControl_RedoActionName);
					mng.add(fRedoAction);
				}

				SaveAction saveAction = new SaveAction();
				mng.add(saveAction);

				mng.add(new Separator());

				CommandContributionItemParameter cutParameter = new CommandContributionItemParameter(fServiceLocator,
						Messages.TextFieldControl_CutActionName, Messages.TextFieldControl_CutActionID, 0);
				mng.add(new CommandContributionItem(cutParameter));

				CommandContributionItemParameter copyParameter = new CommandContributionItemParameter(fServiceLocator,
						Messages.TextFieldControl_CopyActionName, Messages.TextFieldControl_CopyActionID, 0);
				mng.add(new CommandContributionItem(copyParameter));

				CommandContributionItemParameter pasteParameter = new CommandContributionItemParameter(fServiceLocator,
						Messages.TextFieldControl_PasteActionName, Messages.TextFieldControl_PasteActionID, 0);
				mng.add(new CommandContributionItem(pasteParameter));

				DeleteAction deleteAction = new DeleteAction();
				mng.add(deleteAction);
				mng.add(new Separator());

				// add glossary action
				String text = fStyledText.getSelectionText();
				if (canAddToGloss(text)) {
					GlossaryField field = glossFolder.getChild(text);
					if (field == null) {
						mng.add(addGlossAction);
					} else {
						mng.add(new EditGlossEntryAction(field));
					}
				}
				// serach text menu
				MenuManager searchSubMenu = new MenuManager(Messages.TextFieldControl_SearchTextActionName);
				mng.add(searchSubMenu);
				searchSubMenu.add(fSearchInWorkspaceItem);
				searchSubMenu.add(fSearchInProjectItem);
				searchSubMenu.add(fSearchInFileItem);
				searchSubMenu.add(fSearchInWorkingSetItem);

				return mng;
			}
		};

		popupMenuManager.addMenuListener(listener);
		Menu menu = popupMenuManager.createContextMenu(fStyledText);
		fStyledText.setMenu(menu);
	}

	private void createSearchTextItems() {
		CommandContributionItemParameter parameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
				Messages.TextFieldControl_TextSearchWorspaceActionName,
				Messages.TextFieldControl_TextSearchWorkspaceActionID, SWT.NONE);
		parameter.label = Messages.TextFieldControl_WorkspaceLabel;
		fSearchInWorkspaceItem = new CommandContributionItem(parameter);

		parameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
				Messages.TextFieldControl_TextSearchProjectActionName,
				Messages.TextFieldControl_TextSearchProjectActionID, SWT.NONE);
		parameter.label = Messages.TextFieldControl_ProjectLabel;
		fSearchInProjectItem = new CommandContributionItem(parameter);

		parameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
				Messages.TextFieldControl_TextSearchFileActionName, Messages.TextFieldControl_TextSearchFileActionID,
				SWT.NONE);
		parameter.label = Messages.TextFieldControl_FileLabel;
		fSearchInFileItem = new CommandContributionItem(parameter);

		parameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(),
				Messages.TextFieldControl_TextSearxhWorkingSetActionName,
				Messages.TextFieldControl_TextSearchWorkingSetActionID, SWT.NONE);
		parameter.label = Messages.TextFieldControl_WorkingSetLabel;
		fSearchInWorkingSetItem = new CommandContributionItem(parameter);
	}

	private boolean canAddToGloss(String text) {
		if (text == null || text.isEmpty() || text.length() > GlossaryField.NAME_MAX_LENGTH) {
			return false;
		}
		return true;
	}

	private class AddGlossEntryAction extends Action {

		private GlossaryFolder fGlossFolder;

		public AddGlossEntryAction(GlossaryFolder glossaryFolder) {
			setText(Messages.TextFieldControl_AddToGlossaryActionName);
			fGlossFolder = glossaryFolder;
		}

		@Override
		public void run() {
			String text = fStyledText.getSelectionText();
			NewGlossFieldDialog dialog = new NewGlossFieldDialog(getShell(), fGlossFolder, text);
			dialog.open();
		}

	}

	private class EditGlossEntryAction extends Action {

		private GlossaryField fField;

		public EditGlossEntryAction(GlossaryField field) {
			setText(Messages.TextFieldControl_EditToGlossaryActionName);
			fField = field;
		}

		@Override
		public void run() {
			EditGlossFieldDialog dialog = new EditGlossFieldDialog(getShell(), fField);
			dialog.open();
		}
	}

	private class SaveAction extends Action {

		public SaveAction() {
			setText(Messages.TextFieldControl_SaveActionTitle);
			setImageDescriptor(ImageProvider.SAVE);
		}

		@Override
		public void run() {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().doSave(null);
		}

		@Override
		public boolean isEnabled() {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().isDirty();
		}

	}

	private class DeleteAction extends Action {

		public DeleteAction() {
			setText(Messages.TextFieldControl_DeleteActionTitle);
			setImageDescriptor(ImageProvider.DELETE_DESCRIPTOR);
		}

		@Override
		public void run() {
			fStyledText.invokeAction(ST.DELETE_NEXT);
			super.run();
		}

	}

	private Shell getShell() {
		return fStyledText.getShell();
	}

}
