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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.editors.md.MDEditor;
import ru.dip.editors.md.comment.CommentManager;
import ru.dip.editors.md.comment.ICommentManager;

public class UnityCommentManager extends CommentManager implements ICommentManager {
	
	private final UnityMdEditor fUnityEditor;
	private final MdTextField fEditor;
		
	public UnityCommentManager(UnityMdEditor unityEditor, MdTextField editor) {
		super(null);
		fUnityEditor = unityEditor;
		fEditor = editor;
	}
	
	@Override
	public void showComment() {
		if (!isShowComment()) {
			hideCommentComposite();	
		} else {
			showCommentComposite();
		}		
	}

	//===============================
	// getters
	
	@Override
	protected StyledText text() {
		return fEditor.styledText();
	}
	
	@Override
	protected TextViewer viewer() {
		return fEditor.getMDViewer();
	}
	
	@Override
	public boolean isShowComment() {
		return fUnityEditor.isShowComment();
	}
	
	@Override
	protected Composite mainComposite() {
		return fEditor.getMainComposite();
	}
	
	@Override
	protected void setDirty() {
		fEditor.incrementCurrentTimeStamp();
		fEditor.firePropertyChange(MDEditor.PROP_DIRTY);
	}
	
	@Override
	protected IDipUnit getUnit() {
		return fEditor.getUnit();
	}
	
	@Override
	protected IDocument document() {
		return fEditor.document();
	}
		
}
