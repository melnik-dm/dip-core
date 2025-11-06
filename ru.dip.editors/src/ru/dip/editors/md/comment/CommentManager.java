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

import static ru.dip.core.utilities.md.MdUtilities.COMMENT_ARGUMENT;
import static ru.dip.core.utilities.md.MdUtilities.COMMENT_END_TAG;
import static ru.dip.core.utilities.md.MdUtilities.COMMENT_START_TAG;
import static ru.dip.core.utilities.md.MdUtilities.COMMENT_TAG_NAME;
import static ru.dip.core.utilities.md.MdUtilities.comment_tag_pattern;
import static ru.dip.core.utilities.md.MdUtilities.end_comment_tag_pattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipComment;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.Messages;
import ru.dip.editors.md.MDEditor;
import ru.dip.ui.utilities.dialog.EditTextDialog;

public class CommentManager implements ICommentManager {
	
	private static final Font fCommentFont = FontManager.getMonoFont(9);
	private static final FontDimension fCommentFontDimentsion = FontDimension.createFontSize(fCommentFont);	
	private static final int fCommentPanelWidth = 300;
	
	private final MDEditor fEditor;
	private boolean fShowComment = true;
	// controls
	private Composite fCommentComposite;
	
	// комментарии (все)
	private TreeSet<CommentAnnotation> fCommentsInModel = new TreeSet<CommentAnnotation>();	
	// комментарии отображаемые на панели (которые поместились
	private List<CommentAnnotation> fVisibleComments = new ArrayList<CommentAnnotation>();
	private CommentAnnotation fSelelctAnnotation;
		
	// for update
	private CommentPositionUpdater fCommentPositionUpdater;
	private Map<CommentAnnotation, String> fAnnotationTexts = new HashMap<>();
	private Set<Long> autoformatModificationStamps = new HashSet<>();
	
	public CommentManager(MDEditor editor) {
		fEditor = editor;
		fCommentPositionUpdater = new CommentPositionUpdater();
	}
		
	//=================================
	// Decoration SourceViewer
	
	public void addCommentDecorationInSourceViewer() {
		// обновление commentComposite
		addBidiListener();
		addMouseWheelListener();
		// отрисовка textWidget
		addPaintListener();
	}
	
	private void addBidiListener() {
		text().addBidiSegmentListener(new BidiSegmentListener() {
			@Override
			public void lineGetSegments(BidiSegmentEvent event) {
				if (isShowComment()) {
					fCommentComposite.redraw();
				}
			}
		});
	}
	
	private void addMouseWheelListener() {
		text().addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseScrolled(MouseEvent e) {
				if (isShowComment()) {
					fCommentComposite.redraw();
				}
			}
		});
	}
	
	private void addPaintListener() {
		text().addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				int width = text().getClientArea().width;
				int lineHeight = text().getLineHeight();
									
				for (CommentAnnotation annotation: fCommentsInModel) {			
					decorateBackground(annotation);		
					if (isShowComment()) {
						paintArrow(annotation, e.gc, width, lineHeight);
					}
				}						
			}
		});	
	}
	
	private void decorateBackground(CommentAnnotation annotation) {
		StyleRange[] ranges = text().getStyleRanges(annotation.getOffset(), annotation.getLength());

		boolean needUpate = false;
		for (StyleRange range: ranges) {
			Color newColor = (fSelelctAnnotation == annotation) ? ColorProvider.SELECT : ColorProvider.LIGHT_YELLOW;
			if (!Objects.equals(newColor, range.background)) {
				range.background = newColor;
				needUpate = true;
			}
		}
		
		if (needUpate) {

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					StyledText text = text();
					if (text != null) {
						text.replaceStyleRanges(annotation.getOffset(), annotation.getLength(), ranges);
					}
				}
			});
		}
	}
	
	private void paintArrow(CommentAnnotation annotation, GC gc , int width, int lineHeigh) {
		gc.setForeground(ColorProvider.RED);
		Point location = text().getLocationAtOffset(annotation.getEndOffset());
		int y = location.y + lineHeigh;
		for (int x = location.x; x < width - 30; x += 20) {
			gc.drawLine(x, y, x + 10, y);
		}

		int y2 = y;
		if (fVisibleComments.contains(annotation)) {
			y2 = annotation.getViewerY() + 4;
		}

		gc.drawLine(width - 30, y, width - 10, y2);
		gc.drawLine(width - 10, y2, width, y2);

		gc.setBackground(ColorProvider.RED);
		gc.fillPolygon(new int[] { location.x - 5, y, location.x, y - 5, location.x + 5, y });
		gc.fillPolygon(new int[] { width - 5, y2 - 5, width, y2, width - 5, y2 + 5 });
	}
	
	//=================================
	// Comment composite
	
	public void createCommentComposite(Composite parent) {
		fCommentComposite = new Composite(parent, SWT.BORDER);
		fCommentComposite.setLayout(new GridLayout());
		GridData rightGd = new GridData(GridData.FILL_VERTICAL);
		rightGd.widthHint = fCommentPanelWidth;
		fCommentComposite.setLayoutData(rightGd);
		addPaintCommentsListener();
		addMouseListener();
		createContextMenu();
	}
	
	private void addPaintCommentsListener() {
		fCommentComposite.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {												
				computeVisibleComments();				
				computeLocations(fVisibleComments, text().getClientArea().height);
				paintComments(e.gc);
			}
			
			private void computeVisibleComments() {
				int startLine = viewer().getTopIndex();
				int endLine = viewer().getBottomIndex();
				fVisibleComments = new ArrayList<CommentAnnotation>();
				for (CommentAnnotation annotation: fCommentsInModel) {
					int end = annotation.getEndOffset();
					int annotationLine = text().getLineAtOffset(end);
					if (annotationLine >= startLine && annotationLine <= endLine) {
						Point location = text().getLocationAtOffset(end);
						annotation.setViewerY(location.y);
						annotation.computeHeight(fCommentFontDimentsion, fCommentPanelWidth - 10);
						fVisibleComments.add(annotation);
					}
				}
			}
			
			private void computeLocations(List<CommentAnnotation> annotations, int height) {		
				for (int i = 0; i < annotations.size() - 1; i++) {
					CommentAnnotation current = annotations.get(i);						
					CommentAnnotation next = annotations.get(i+1);
					int factNextStart = current.getViewerY() + current.getHeight();
					if (factNextStart > next.getViewerY()) {
						next.setViewerY(factNextStart + 2);
					}									
				}
			}
		
			private void paintComments(GC gc) {
				for (CommentAnnotation annotation: fVisibleComments) {
					int y = annotation.getViewerY();		
					
					if (fSelelctAnnotation == annotation) {
						gc.setBackground(ColorProvider.SELECT);
					} else {
						gc.setBackground(ColorProvider.LIGHT_YELLOW);
					}
					
					gc.fillRectangle(0, y, fCommentPanelWidth, annotation.getHeight());
					gc.drawRectangle(0, y, fCommentPanelWidth, annotation.getHeight());
														
				    final TextLayout layout = new TextLayout(gc.getDevice());
				    layout.setText(annotation.getContent());
				    layout.setFont(fCommentFont);
				    layout.setIndent(0);
				    layout.setWidth(fCommentPanelWidth - 10);
				    layout.draw(gc, 5, y);
				    layout.dispose();
				}
			}
		});
	}
	
	private void addMouseListener() {
		fCommentComposite.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {}
			
			@Override
			public void mouseDown(MouseEvent e) {
				int y = e.y;
				for (CommentAnnotation annotation: fVisibleComments) {
					if (annotation.getViewerY() < y && y < annotation.getHeight() + annotation.getViewerY()) {
						CommentAnnotation old = annotation;
						if (fSelelctAnnotation != annotation) {
							fSelelctAnnotation = annotation;
							fCommentComposite.redraw();
							
							if (old != null) {
								decorateBackground(old);
							}
							if (fSelelctAnnotation != null) {
								decorateBackground(fSelelctAnnotation);
							}
						}
						return;
					}										
				}
				if (fSelelctAnnotation != null) {
					CommentAnnotation old = fSelelctAnnotation;
					fSelelctAnnotation = null;
					fCommentComposite.redraw();
					decorateBackground(old);
				}			
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				doEdit();
			}
		});
	}


	private void createContextMenu() {
		MenuManager popupMenuManager = new MenuManager();
		popupMenuManager.setRemoveAllWhenShown(true);
		popupMenuManager.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new EditCommentAction());
				manager.add(new DeleteCommentAction());
			}
		});

		Menu menu = popupMenuManager.createContextMenu(fCommentComposite);
		fCommentComposite.setMenu(menu);
	}

	/**
	 * Устанавливает комментарии из файла .r (для совместимости со старой версии)
	 */
	public void setCommentModel(IDipUnit unit) {
		if (unit != null && unit.comment() != null) {
			fCommentsInModel = new TreeSet<CommentAnnotation>(
					unit.comment().getTextComments().stream()
					.map(CommentAnnotation::new)
					.collect(Collectors.toSet()));
		}
	}
		
	/**
	 * Добавляет комментарии из текста (теги)
	 * И устанавливает в Document текст без тегов
	 */
	public void setCommentModel(IDocument document) {
		String contentWithoutComments = findCommentsInDocument(document);		
		if (document.getLength() > contentWithoutComments.length()) {
			document.set(contentWithoutComments);
		}
	}
	
	/**
	 * Находит комментарии в документу, возвращает содержимое документа с вырезанными комментариями 
	 */
	public String findCommentsInDocument(IDocument document) {
		String content = document.get();
		CommentAnnotation annotation = findComment(content);
		while (annotation != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(content, 0, annotation.getOffset());			
			int startMainText = annotation.getOffset() +  annotation.getContent().length() + 6 + COMMENT_TAG_NAME.length() + COMMENT_ARGUMENT.length();			
			builder.append(content,startMainText, startMainText + annotation.getLength());
			builder.append(content, startMainText + annotation.getLength() + COMMENT_END_TAG.length(), content.length());			
			content = builder.toString();			
			fCommentsInModel.add(annotation);
			annotation = findComment(content);
		}
		return content;
	}
	
	private CommentAnnotation findComment(String content) {
		Matcher matcher = comment_tag_pattern.matcher(content);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			
			Matcher endMatcher = end_comment_tag_pattern.matcher(content);
			if (endMatcher.find(end)) {
				int endStart = endMatcher.start();
				
				String description = content.substring(start + 4 + COMMENT_TAG_NAME.length() + COMMENT_ARGUMENT.length() , end - 2);
				int offset = start;
				int length = endStart - end;
							
				return new CommentAnnotation(offset, length, description);		
			}						
		} 
		return null;		
	}
	
	public void addComment(int offset, int length, String text) {
		fCommentsInModel.add(new CommentAnnotation(offset, length, text));
		if (isShowComment()) {
			fCommentComposite.redraw();
		}
		setDirty();
	}
	
	private class EditCommentAction extends Action {
		
		public EditCommentAction() {
			setText(Messages.CommentManager_EditActionName);			
		}
		
		@Override
		public void run() {
			doEdit();	
		}
	}
	
	private void doEdit() {
		if (fSelelctAnnotation != null) {
			EditTextDialog dialog = new EditTextDialog(getShell(), Messages.CommentManager_EditCommentDialogName, Messages.CommentManager_CommentLabel,
					fSelelctAnnotation.getContent());
			if (dialog.open() == Window.OK) {
				String result = dialog.getResult();
				if (result.isEmpty()) {
					deleteSelectAnnotation();
				} else {
					fSelelctAnnotation.setComment(result);
				}
				fCommentComposite.update();
				setDirty();
			}
		}
	}
	
	private class DeleteCommentAction extends Action {
		
		public DeleteCommentAction() {
			setText(Messages.CommentManager_DeleteActionName);			
		}
		
		@Override
		public void run() {
			if (fSelelctAnnotation != null) {
				boolean confirm = MessageDialog.openConfirm(getShell(), Messages.CommentManager_DeleteCommentDialogName, Messages.CommentManager_DeleteCommentConfirm);
				if (!confirm) {
					return;
				}
				deleteSelectAnnotation();
			}
		}
	}
	
	private void deleteSelectAnnotation() {
		fCommentsInModel.remove(fSelelctAnnotation);
		int offset = fSelelctAnnotation.getOffset();
		int length = fSelelctAnnotation.getLength();
		StyleRange range = new StyleRange();
		range.start = offset;
		range.length = length;
		text().setStyleRange(range);	
		fSelelctAnnotation = null;	
		fCommentComposite.redraw();
		setDirty();
	}
	
	public void showComment() {
		if (isShowComment()) {
			hideCommentComposite();
		} else {
			showCommentComposite();
		}		
	}
	
	protected void hideCommentComposite() {
		fCommentComposite.dispose();
		fShowComment = false;
		mainComposite().layout();
	}
	
	protected void showCommentComposite() {
		createCommentComposite(mainComposite());
		fShowComment = true;
		mainComposite().layout();
	}
	
	/**
	 * Для совместимости, стирает все комменты из файла .r
	 * @throws IOException 
	 */
	public void deleteTextCommentFromMainFile() throws IOException {
		IDipComment comment = getUnit().comment();
		if (comment instanceof DipComment) {
			DipComment dipComment = (DipComment) comment;;
			dipComment.saveOnlyMainReview();
		}		
	}
	
	public String saveCommentsContent() {					
		List<CommentAnnotation> comments = fCommentsInModel.stream().collect(Collectors.toList());		
		String content = fEditor.document().get();
		StringBuilder builder = new StringBuilder();
		int index = 0;
				
		for (CommentAnnotation comment: comments) {
			builder.append(content, index, comment.getOffset());
			index = comment.getOffset();
			builder.append(COMMENT_START_TAG);
			builder.append("\""); //$NON-NLS-1$
			builder.append(comment.getContent());
			builder.append("\">"); //$NON-NLS-1$
			builder.append(content, comment.getOffset(), comment.getEndOffset());
			builder.append(COMMENT_END_TAG);
			index = comment.getEndOffset();							
		}
		builder.append(content, index, content.length());
		return builder.toString();
	}
		
	//=====================================
	// update posotopms
	
	/**
	 * Сохраняет текст аннотаций из документа (к чему сделан комментарий)
	 * Перед изменением документа (documentAboutToBeChanged)
	 */
	public void saveAnnotationSelectedTexts() {
		fAnnotationTexts.clear();
		fCommentsInModel.forEach(this::putToAnnotationTexts);
	}
	
	public void savedCoveredAnnotations(DocumentEvent event) {
		final int eventStart = event.getOffset();
		final int eventEnd = event.getOffset() + event.getLength();
		
		fAnnotationTexts.clear();
		fCommentsInModel.stream()
			.filter(ann -> EventRelaitionPosition.isCover(ann, eventStart, eventEnd))
			.forEach(this::putToAnnotationTexts);
	}
	
	/**
	 * Положить текст (для которого добавлен комментарий) в мапу
	 */
	private void putToAnnotationTexts(CommentAnnotation annotation) {
		String text = text().getText(annotation.getOffset(), annotation.getEndOffset() - 1);
		try {
			text = document().get(annotation.getOffset(), annotation.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
		fAnnotationTexts.put(annotation, text);	
	}
	

	public void updateCommentPositions(DocumentEvent event, boolean autoFormat) {
		if (autoFormat) {
			updateCommentPositionAfterFormat(event);
			autoformatModificationStamps.add(event.getModificationStamp());
			autoformatModificationStamps.add(event.getModificationStamp() - 1);
		} else {			
			if (autoformatModificationStamps.contains(event.getModificationStamp())){
				updateCommentPositionAfterFormat(event);
			} else {
				updateCommentPositionAfterDocumentChanged(event);
			}
		}
	}
	
	private void updateCommentPositionAfterDocumentChanged(DocumentEvent event) {
		final List<CommentAnnotation> forDelete = new ArrayList<>();		
		final int eventStart = event.getOffset();
		final int eventEnd = event.getOffset() + event.getLength();
		final int eventLength = event.getLength();
		final int eventTextLengt = event.getText().length();

		for (CommentAnnotation annotation : fCommentsInModel) {
			EventRelaitionPosition relationPosition = EventRelaitionPosition.getDocumentEventPosition(annotation, eventStart, eventEnd);
			switch (relationPosition) {
			case BEFORE: { // до аннотации
				int delta = eventTextLengt - eventLength;
				if (delta != 0) {
					annotation.setOffsetDelta(delta);
				}
				continue;
			}
			case AFTER: { // после аннотации
				continue;
			}
			case PART_START: {
				// было удалено до аннотации
				int beforeDelete = annotation.getOffset() - eventStart;
				// смсестить оффест
				int offsetDelta = eventTextLengt - beforeDelete;
				// удалено внутри аннотации
				int inDelete = eventEnd - annotation.getOffset();
				annotation.setLengthDelta(-inDelete);
				annotation.setOffsetDelta(offsetDelta);
				continue;
			}
			case COVER: {
				String text = fAnnotationTexts.get(annotation);
				if (text != null && event.getText().contains(text)) {
					int index = event.getText().indexOf(text);
					annotation.setOffset(eventStart + index);
				} else {
					forDelete.add(annotation);
				}
				continue;
			}
			case INLINE: {
				int lengthDelta = eventTextLengt - eventLength;
				annotation.setLengthDelta(lengthDelta);
				continue;
			}
			case PART_END: {
				int lengthDelta = -(annotation.getEndOffset() - eventStart);
				annotation.setLengthDelta(lengthDelta);
				continue;
			}
			}
		}
		fCommentsInModel.removeAll(forDelete);
	}
	
	/**
	 * Обновление аннотации во время команды формат
	 */
	private void updateCommentPositionAfterFormat(DocumentEvent event) {		
		final int eventStart = event.getOffset();
		final int eventEnd = event.getOffset() + event.getLength();
		final int eventLength = event.getLength();
		final int eventTextLengt = event.getText().length();
				
		for (CommentAnnotation annotation: fCommentsInModel) {			
			EventRelaitionPosition relationPosition = EventRelaitionPosition.getDocumentEventPosition(annotation, eventStart, eventEnd);
			switch (relationPosition) {
			case BEFORE: {
				int delta = eventTextLengt - eventLength;				
				if (delta != 0) {
					annotation.setOffsetDelta(delta);
				}
				continue;				
			}
			case AFTER:{
				continue;				
			}
			case PART_START:{
				try {
					fCommentPositionUpdater.formatPartStart(annotation, fAnnotationTexts.get(annotation), event);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				continue;
			}
			case COVER:{
				try {
					fCommentPositionUpdater.formatCoverAnnotation(annotation, fAnnotationTexts.get(annotation), event);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				continue;
			}
			case INLINE:{
				int lengthDelta = eventTextLengt - eventLength;
				annotation.setLengthDelta(lengthDelta);
				continue;
			}
			case PART_END:{
				try {
					fCommentPositionUpdater.formatPartEnd(annotation, fAnnotationTexts.get(annotation), event);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				continue;
			}
			}
		}
	}
	
	
	//===============================
	// getters
	
	protected StyledText text() {
		return viewer().getTextWidget();
	}
	
	protected TextViewer viewer() {
		return fEditor.getMDViewer();
	}

	public boolean isShowComment() {
		return fShowComment;
	}
	
	protected Composite mainComposite() {
		return fEditor.getMainComposite();
	}
	
	protected void setDirty() {
		fEditor.incrementCurrentTimeStamp();
		fEditor.firePropertyChange(MDEditor.PROP_DIRTY);
	}
	
	protected Shell getShell() {
		return fCommentComposite.getShell();
	}
	
	protected IDipUnit getUnit() {
		return fEditor.getUnit();
	}
	
	protected IDocument document() {
		return fEditor.mdDocument().document();
	}

	public boolean hasComments() {
		return !fCommentsInModel.isEmpty();
	}

}
