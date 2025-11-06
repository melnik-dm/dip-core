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
package ru.dip.editors.md.unity.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.editors.md.unity.MdTextField;
import ru.dip.editors.md.unity.UnityMdEditor;
import ru.dip.ui.utilities.dialog.EditTextDialog;
import ru.dip.ui.utilities.image.ImageProvider;

public class AddUnityCommentAction extends Action {
	
	public static final String ID = "ru.dip.editors.add.text.comment2";
	
	private static final String DIALOG_NAME = "Add Comment";
	private static final String COMMENT_LABEL = "Comment";
	private static final String START_VALUE = "";
	
		
	private static AddUnityCommentAction instance = new AddUnityCommentAction();
	
	public static AddUnityCommentAction instance() {
		return instance;
	}

	private AddUnityCommentAction() {
		setText("Add Text Comment");
		setId(ID);
		setImageDescriptor(ImageProvider.ADD_COMMENT_DESC);
	}
	
	@Override
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart editorPart = WorkbenchUtitlities.getActiveEditor();
		if (editorPart instanceof UnityMdEditor) {
			UnityMdEditor editor = (UnityMdEditor) editorPart;
			MdTextField field = editor.getActiveField();
			if (field != null) {
				Point p = field.getSelection();
				if (p.x != p.y) {
					EditTextDialog dialog = new EditTextDialog(window.getShell(), 
							DIALOG_NAME, COMMENT_LABEL, START_VALUE);
					if (dialog.open() == Window.OK) {
						field.getCommentManager().addComment(p.x, p.y - p.x,dialog.getResult());					
					}								
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
