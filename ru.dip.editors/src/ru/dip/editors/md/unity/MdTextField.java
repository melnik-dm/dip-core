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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.editors.md.IMdEditor;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.MdAutoCorrector;
import ru.dip.editors.md.MarkdownDocument.MdDocumentListener;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.MdActionUpdater;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.editors.md.actions.TextTransferExecutor;
import ru.dip.editors.md.comment.ICommentManagerHolder;
import ru.dip.editors.md.field.ContextMenuManager;
import ru.dip.editors.md.field.MdField;
import ru.dip.ui.preferences.MdPreferences;

public class MdTextField implements IDocumentListener, MdDocumentListener, ICommentManagerHolder, IMdEditor {

	private final IDipUnit fDipUnit;
	private final IFile fFile;
	private final UnityMdEditor fEditor;

	private Composite fMainComposite;
	private Composite fEditorComposite;
	private MdField fMdField;
	
	private UnityCommentManager fCommentManager;
	private TextTransferExecutor fTextTransferExecutor;
	private MdActionUpdater fMdActionUpdater;
	private MdAutoCorrector fAutoCorrector = new MdAutoCorrector() {

		@Override
		protected void replace(int offset, int length, String text) {
			getShell().getDisplay().asyncExec(() -> styledText().replaceTextRange(offset, length, text));
		}
	};
	
	// for save-dirty (счетчики состояний)
	private long fLastTimeStamp = 1;
	private long fCurrentLastTimeStamp = 1;


	public MdTextField(IDipUnit unit, UnityMdEditor editor, Composite composite, String content) {
		fDipUnit = unit;
		fFile = unit.resource();
		fEditor = editor;
		fCommentManager = new UnityCommentManager(fEditor, this);
		fCommentManager.setCommentModel(fDipUnit);
		createContent(composite, content);
		createDocumentListeners();
		createTextListeners();
		fTextTransferExecutor = new TextTransferExecutor(this);
	}

	private void createContent(Composite parent, String content) {
		fMainComposite = CompositeBuilder.instance(parent).columns(2, false).horizontal().notIndetns().build();
		fEditorComposite = CompositeBuilder.instance(fMainComposite).full().notIndetns().build();
		if (fEditor.isShowComment()) {
			fCommentManager.createCommentComposite(fMainComposite);
		}
		fMdField = new UnityMdField(this, fDipUnit, content);
		fMdField.createField(fEditorComposite);
		fMdActionUpdater = new MdActionUpdater(fMdField);
		addContextMenuManager();	
	}
	
	//====================================
	// MdDocumentListener, IDocumentListener 
	
	private void createDocumentListeners() {
		fMdField.mdDocument().addListener(this);
		fMdField.document().addDocumentListener(this);
	}
	
	@Override
	public void mdDocumentUpdated() {
		fMdActionUpdater.updateActionStatus();
	}
	
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {}

	@Override
	public void documentChanged(DocumentEvent event) {
		// замена кавычек, длинное тире
		fAutoCorrector.autoCorrect(event);
		// автоперенос текста
		if (MdPreferences.autoTextTransfer()) {
			if (fTextTransferExecutor.transfer(event)) {
				return;
			}
		}
		fEditor.getUndoAction().setEnabled(canUndo());
		fEditor.getRedoAction().setEnabled(canRedo());
		fCurrentLastTimeStamp = event.getModificationStamp();
		fMdField.createMdModel();
	}
	
	//=================================
	// Text Listeners
	
	private void createTextListeners() {
		addFocusListener();
		addSelectionListener();
		addCaretListener();
		addKeyListener();
	}
	
	private void addFocusListener() {
		styledText().addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				updateFocusLost();
				fEditor.getUndoAction().setMdField(null);
				fEditor.getRedoAction().setMdField(null);
			}
			
			private void updateFocusLost() {
				BoldAction.instance().setEnabled(false);
				ItalicAction.instance().setEnabled(false);
				CommentAction.instance().setEnabled(false);
				CodeAction.instance().setEnabled(false);
				NumberListAction.instance().setEnabled(false);
				MarkerListAction.instance().setEnabled(false);
				LinkAction.instance().setEnabled(false);
				ParagraphAction.instance().setEnabled(false);
			}

			@Override
			public void focusGained(FocusEvent e) {
				fMdActionUpdater.updateActionStatus();
				fEditor.getUndoAction().setMdField(MdTextField.this);
				fEditor.getRedoAction().setMdField(MdTextField.this);
				fEditor.changeFocus(MdTextField.this);
			}
			
		});

	}
	
	private void addSelectionListener() {
		styledText().addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fMdActionUpdater.updateActionStatus();

			}
		});

	}
	
	private void addCaretListener() {
		styledText().addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				fMdActionUpdater.updateActionStatus();
				checkScroll(event.caretOffset);
			}
			
			private void checkScroll(int caretOffset) {
				int line = styledText().getLineAtOffset(caretOffset);
				int linePixel = styledText().getLinePixel(line);
				Point textLocation = fMainComposite.getLocation();
				int lineHeight = styledText().getLineHeight(caretOffset);
				int cursorLocation = textLocation.y + linePixel + lineHeight;
				Point p = fEditor.getVisibleArea();
				if (cursorLocation < p.x) {
					fEditor.scrollUp();
				} else if (cursorLocation > p.y) {
					fEditor.scrollDown();
				}
			}

		});
	}
	
	private void addKeyListener() {
		styledText().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP) {
					int caretOffset = styledText().getCaretOffset();
					if (caretOffset == 0) {
						fEditor.up(MdTextField.this);
					}

				} else if (e.keyCode == SWT.ARROW_DOWN) {
					int caretOffset = styledText().getCaretOffset();
					if (caretOffset == styledText().getCharCount()) {
						fEditor.down(MdTextField.this);
					}
				}
			}
		});
	}
	
	//=============================
	
	private void addContextMenuManager() {
		ContextMenuManager menuManager = new ContextMenuManager(styledText(), fEditor.getEditorSite(), fEditor.getUndoAction(), fEditor.getRedoAction());
		menuManager.addContextMenu(fFile);
	}

	public void setEndCaret() {
		styledText().setFocus();
		styledText().setCaretOffset(styledText().getCharCount());
	}

	public void setStartCaret() {
		styledText().setFocus();
		styledText().setCaretOffset(0);
	}

	public void updateViewerConfiguration() {
		fMdField.getViewerConfiguration();
		fMdField.getViewerConfiguration().getReconciler(fMdField.getMDViewer()).install(fMdField.getMDViewer());
		fMdField.getViewerConfiguration().getPresentationReconciler(fMdField.getMDViewer())
				.install(fMdField.getMDViewer());
	}

	public void addModifyListener(ModifyListener modifyListener) {
		styledText().addModifyListener(modifyListener);
	}

	/**
	 * Все ли строки умещаются
	 */
	public boolean isTextFit() {
		int lineCount = styledText().getLineCount();
		int height = styledText().getBounds().height;
		int lineHeight = styledText().getLineHeight();
		int visibleLine = height / lineHeight;
		int lineIndex = styledText().getLineIndex(height - 4);
		if (lineIndex + 1 != lineCount) {
			return false;
		}
		return visibleLine == lineCount;
	}
	
	// =======================
	// undo-redo

	public void doUndo() {
		getMDViewer().getUndoManager().undo();
		fEditor.getRedoAction().setEnabled(canRedo());
	}

	public boolean canUndo() {
		return getMDViewer().getUndoManager().undoable();
	}

	public void doRedo() {
		getMDViewer().getUndoManager().redo();
	}

	public boolean canRedo() {
		return getMDViewer().getUndoManager().redoable();
	}

	// ==========================
	// focus

	public boolean isFocus() {
		return styledText().isFocusControl();
	}

	public void setFocus() {
		styledText().setFocus();
	}

	public void desellect() {
		styledText().setSelection(new Point(0, 0));
	}

	// ===========================
	// save

	public void incrementCurrentTimeStamp() {
		fCurrentLastTimeStamp++;
	}

	public void firePropertyChange(int propDirty) {
		fEditor.firePropertyChange(propDirty);
	}

	public boolean isDirty() {
		return fLastTimeStamp != fCurrentLastTimeStamp;
	}

	public void save() {
		try {
			String content = null;
			if (fCommentManager.hasComments()) {
				content = fCommentManager.saveCommentsContent();
			} else {
				content = styledText().getText();
			}						
			FileUtilities.writeFile(fFile, content);
			fCommentManager.deleteTextCommentFromMainFile();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Ошибка записи файла " + fFile);
		}
		fLastTimeStamp = fCurrentLastTimeStamp;
	}

	// ====================================
	// getters

	public Shell getShell() {
		return fMainComposite.getShell();
	}

	@Override
	public IDocument document() {
		return fMdField.document();
	}

	@Override
	public SourceViewer getMDViewer() {
		return fMdField.getMDViewer();
	}

	public Composite getMainComposite() {
		return fMainComposite;
	}

	public UnityCommentManager getCommentManager() {
		return fCommentManager;
	}

	public Point getSelection() {
		return styledText().getSelection();
	}

	@Override
	public StyledText styledText() {
		return fMdField.styledText();
	}

	public IDipUnit getUnit() {
		return fDipUnit;
	}

	@Override
	public MarkdownDocument mdDocument() {
		return fMdField.mdDocument();
	}

}
