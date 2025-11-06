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
package ru.dip.editors.md.comment;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.editors.Messages;
import ru.dip.ui.utilities.dialog.EditTextDialog;
import ru.dip.ui.utilities.image.ImageProvider;

public class AddCommentAction extends Action {
	
	public static final String ID = "ru.dip.editors.add.text.comment"; //$NON-NLS-1$
	
	private static final String DIALOG_NAME = Messages.AddCommentAction_DialogName;
	private static final String COMMENT_LABEL = Messages.AddCommentAction_CommentLabel;
	private static final String START_VALUE = ""; //$NON-NLS-1$
	
		
	private static AddCommentAction instance = new AddCommentAction();
	
	public static AddCommentAction instance() {
		return instance;
	}

	private AddCommentAction() {
		setText(Messages.AddCommentAction_ActionTitle);
		setId(ID);
		setImageDescriptor(ImageProvider.ADD_COMMENT_DESC);
	}
	
	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService()
				.getSelection();
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			IEditorPart editorPart = WorkbenchUtitlities.getActiveEditor();
			if (editorPart instanceof ICommentManagerHolder) {
				ICommentManagerHolder editor = (ICommentManagerHolder) editorPart;
				EditTextDialog dialog = new EditTextDialog(window.getShell(), 
						DIALOG_NAME, COMMENT_LABEL, START_VALUE);
				if (dialog.open() == Window.OK) {
					editor.getCommentManager().addComment(textSelection.getOffset(), textSelection.getLength(), dialog.getResult());					
				}
			}
		}
	}
	
	//======================================
	// selection
		
	public void updateEmptySelection() {			
		setEnabled(false);
	}
	
	public void updateFullSelection() {
		setEnabled(true);
	}

}
