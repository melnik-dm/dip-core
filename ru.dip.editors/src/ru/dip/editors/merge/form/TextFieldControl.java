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
package ru.dip.editors.merge.form;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.TextField;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.editview.EditViewPart;
import ru.dip.editors.md.DocumentPartitioner;
import ru.dip.editors.md.MDViewerConfiguration;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.MarkdownDocument.MdDocumentListener;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.editors.md.model.MdDocumentModel;
import ru.dip.editors.md.partitions.MdPartitionScanner;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.action.hyperlink.HyperlinkDetector;
import ru.dip.ui.glossary.GlossaryHover;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.table.editor.DipTableEditor;

public class TextFieldControl extends FieldControl implements MdDocumentListener, IDocumentListener {

	private static final String HINT = "hint"; //$NON-NLS-1$
	
	// model
	private Group fGroup;
	private TextField fTextField;
	private Document fDocument;
	private Color fForegraund;
	private int fOldCaretPosition = -1;
	private int fOldLength = 0;	
	private MarkdownDocument fMdDocument;	
	private MdDocumentModel fMdModel;
	private boolean fReadonly = false;
	// control
	private ScrolledComposite fScrollParentComposite;
	private Composite fMainComposite;
	private StyledText fStyledText;
	private SourceViewer fSourceViewer;
	private Font fFont;
	// for context menu

	

	public Integer getSchemaHeight() {
		return fTextField.getHeight();
	}
			
	public TextFieldControl(Composite parent, TextField field, boolean readOnly) {
		fTextField = field;
		fGroup =  new Group(parent, SWT.NONE);
		fFont = getFont();
		fReadonly = readOnly;
		setScolledComposite();
		createContent();
		setGroupToolTip();
	}
	
	private void setScolledComposite(){
    	Composite comp = fGroup.getParent().getParent().getParent().getParent();    	    	
    	if (comp instanceof ScrolledComposite){
    		fScrollParentComposite = (ScrolledComposite) comp;
    		fScrollParentComposite.setBackground(ColorProvider.GREEN);
    	}
	}
	
	private void setGroupToolTip(){
		String hint = fTextField.getHint();
		if (hint != null){
			fGroup.setToolTipText(hint);
		}
	}
	
	//========================
	// content
	
	private void createContent(){
		init();
		createText();
		setColors();
	}
	 
	private void init(){
		fGroup.setText(fTextField.getTitle());		
		fGroup.setForeground(ColorProvider.SELECT);
		fGroup.setLayout(new GridLayout());		
	
		GridData gd;
		if (fTextField.getWidth() != null){
			gd = new GridData(GridData.FILL_VERTICAL);
			gd.minimumWidth = fTextField.getWidth();
			gd.widthHint = fTextField.getWidth();
		} else {	
			gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 0;
			gd.heightHint = 0;
		}
		
		gd.horizontalSpan = 2;
		if (fTextField.getHeight() != null){
			gd.minimumHeight = fTextField.getHeight();
		} else {
			gd.minimumHeight = 160;
		}
		fGroup.setLayoutData(gd);
	}
	
	public void setDataHeight(int height){
		GridData gd = (GridData) fGroup.getLayoutData();
		gd.minimumHeight = height;
		gd.heightHint = height;
	}
	
	public int getContentHeight() {
		int height = fStyledText.getLineHeight();
		int lines = fStyledText.getLineCount();
		return lines * height + 20;
	}
		
	private void createText(){
		int style =  SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER;
		if (fReadonly) {
			style = style | SWT.READ_ONLY;
		}
		
		fMainComposite = new Composite(fGroup, SWT.NONE);
		fMainComposite.setLayout(new GridLayout());
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fSourceViewer = new SourceViewer(fMainComposite, null, null, true, style);
		setDocument(fSourceViewer);

		fSourceViewer.configure(new MDViewerConfiguration(fDocument));
		fStyledText = createStylledText(fSourceViewer);
		setSourceViewerDecorationSupport(fSourceViewer);
		setPartitioner(fSourceViewer);
			
		fMdModel = new MdDocumentModel(fDocument, null);
		fMdModel.createModel();		
		fMdDocument = new MarkdownDocument(fStyledText, fDocument, fMdModel);
		fMdDocument.addListener(this);

		setHyperlinkDetector(fSourceViewer);
		fSourceViewer.getTextWidget().setIndent(0);
		setUndoRedoManager();
		setKeyListener();
		setChangedListener();
		setCaretListener();
		createDND();
	}
		
	private StyledText createStylledText(SourceViewer viewer){
		StyledText styledText = viewer.getTextWidget();
		GridData gd = new GridData(GridData.FILL_BOTH);
		styledText.setLayoutData(gd);		
		styledText.setFont(fFont);
		styledText.setAlwaysShowScrollBars(false);
		styledText.addMouseWheelListener(new MouseWheelListener() {
		    @Override
		    public void mouseScrolled(MouseEvent e) {
		    			    	
            	ScrollBar bar = styledText.getVerticalBar();        	
            	if (fScrollParentComposite != null){
    		        if (!styledText.isFocusControl() ) {   		        	
    		        	bar.setEnabled(false);
    		            if (e.count == 3) {
    		            	scrollParentUp();
   		            	} else if (e.count == -3) {
    		            	scrollParentDown();
    		            }
    		        } else {
    		        	bar.setEnabled(true);
    		        	int now = bar.getSelection();
    		        	int max = bar.getMaximum();
    		        	int thumb = bar.getThumb();
    		            if (e.count == 3 && now == 0) {
    		            	scrollParentUp();
    		            } else if (e.count == -3 && now > (max - thumb - 1)) {
    		            	scrollParentDown();
    		            }	
    		        }           		
            	}
		    }
		    
		     void scrollParentUp(){
		    	 fScrollParentComposite.setOrigin(fScrollParentComposite.getOrigin().x, fScrollParentComposite.getOrigin().y - 30);
		     }		     
		     void scrollParentDown(){
	            fScrollParentComposite.setOrigin(fScrollParentComposite.getOrigin().x, fScrollParentComposite.getOrigin().y + 30);
		     }
		});	
		return styledText;
	}
	
	private void setDocument(SourceViewer viewer){
		fDocument = new Document();
		fDocument.addDocumentListener(this);
		viewer.setDocument(fDocument, new AnnotationModel());
	}
	
	private void setSourceViewerDecorationSupport(SourceViewer viewer){
		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		final SourceViewerDecorationSupport support = new SourceViewerDecorationSupport(viewer, null, annotationAccess, EditorsUI.getSharedTextColors());
		for (Iterator<?> e = new MarkerAnnotationPreferences().getAnnotationPreferences().iterator(); e.hasNext();) {
			AnnotationPreference preference = (AnnotationPreference) e.next();		
			support.setAnnotationPreference(preference);
		}
		support.install(EditorsUI.getPreferenceStore());
		viewer.showAnnotations(true);
	}
	
	private void setPartitioner(SourceViewer viewer){
		IDocumentPartitioner partitioner = new DocumentPartitioner(new MdPartitionScanner(),
				new String[] { PartitionStyles.COMMENT, 
						PartitionStyles.EMPTY_LINE, 
						PartitionStyles.PARAGRAPH, 
						PartitionStyles.NUMBER_LIST_ITEM,
						PartitionStyles.GRAPHIC_LIST_ITEM,
						PartitionStyles.CODE });
		
		partitioner.connect(fDocument);
		fDocument.setDocumentPartitioner(partitioner);
	}
	
	private void setHyperlinkDetector(SourceViewer viewer) {
		IHyperlinkDetector detector = new HyperlinkDetector();		
		viewer.setHyperlinkDetectors(new IHyperlinkDetector[]{detector},  SWT.MOD1);
	}
	
	private void setUndoRedoManager() {
		TextViewerUndoManager undoMgr = new TextViewerUndoManager(15);	
		fSourceViewer.setUndoManager(undoMgr);
		fSourceViewer.getUndoManager().connect(fSourceViewer);
	}
	
	private void createDND() {		
		DragSource source = new DragSource(fStyledText, DND.DROP_COPY | DND.DROP_MOVE);
		source.setTransfer(TextTransfer.getInstance());
		source.addDragListener(new DragSourceAdapter() {
			
			Point selection;
			int startSize = 0;
			
			@Override
			public void dragStart(DragSourceEvent e) {
				selection = fStyledText.getSelection();		
				e.doit = selection.x != selection.y;
				startSize =  fStyledText.getText().length();
				
			}
			@Override
			public void dragSetData(DragSourceEvent e) {
				e.data = fStyledText.getText(selection.x, selection.y-1);
			}
			@Override
			public void dragFinished(DragSourceEvent e) {
				if (e.detail == DND.DROP_MOVE) {
					int newSize = fStyledText.getText().length();
					if (newSize > startSize && fStyledText.getCaretOffset() < selection.x) {						
						fStyledText.replaceTextRange(selection.y, selection.y - selection.x, ""); //$NON-NLS-1$
					} else {
						fStyledText.replaceTextRange(selection.x, selection.y - selection.x, ""); //$NON-NLS-1$
					}								
				}
				selection = null;
			}
		});

		DropTarget target = new DropTarget(fStyledText, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(TextTransfer.getInstance());
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent e) {
				if (e.detail == DND.DROP_DEFAULT)
					e.detail = DND.DROP_MOVE;
			}
			@Override
			public void dragOperationChanged(DropTargetEvent e) {
				if (e.detail == DND.DROP_DEFAULT)
					e.detail = DND.DROP_COPY;
			}
			@Override
			public void drop(DropTargetEvent e) {
				fStyledText.getForeground();
				if (Boolean.TRUE.equals(fStyledText.getData(HINT))) {
					fStyledText.setText((String)e.data);
					updateTagValue();
				} else {
					fStyledText.insert((String)e.data);
					updateTagValue();
				}
			}
		});		
	}
	
	public void addSelectionListener(IFile file){
		fStyledText.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = fStyledText.getSelectionText();
				String hoverText = GlossaryHover.getInstance().getText(file, text);
				GlossaryHover.getInstance().mouseHover(hoverText, fStyledText);
				fStyledText.setFocus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});	
	}
	
	private void setColors() {
		Color color = fTextField.getBackgroundColor();
		fStyledText.setBackground(color);
		fForegraund = fTextField.getForegraundColor();
		fStyledText.setForeground(fForegraund);
	}
	
	private void setKeyListener(){
		fSourceViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (Character.isWhitespace(e.character)) {
					updateTagValue();
				}
				
				super.keyReleased(e);
			}
		});
	}
	
	private void setChangedListener(){
		fStyledText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// обновляет значение если текст изменился более чем на один символ (вставка, удаление и т.п.)
				if (Math.abs(fStyledText.getText().length() - fOldLength) > 1) {
					updateTagValue();
				} 					
				fOldLength = fStyledText.getText().length();										
			}
		});
	}
	
	private void setCaretListener() {
		fStyledText.addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				int currentPosition = event.caretOffset;
				if (fOldCaretPosition >= 0 && Math.abs(currentPosition - fOldCaretPosition) > 1) {
					
					/*if (!fEditor.isUndoRedoProcessing()) {
						updateTagValue();
					}*/			
				}
				fOldCaretPosition = currentPosition;				
			}		
		});
	}
	
	@Override
	void updateStatus() {
		fMainComposite.setBackground(getDiffColor());
	}
	
	//========================
	// document listener
	
	@Override
	public void documentChanged(DocumentEvent event) {
		// замена кавычек, длинное тире
		if (MdPreferences.autoCorrect()) {
			if (">".equals(event.fText) && event.fOffset > 0) { //$NON-NLS-1$
				try {
					String text = fDocument.get(event.fOffset - 1, 2);
					if (">>".equals(text)) { //$NON-NLS-1$
						replaceInDocument(event.fOffset - 1, 2, "»"); //$NON-NLS-1$
						return;
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			} else if ("<".equals(event.fText) && event.fOffset > 0) { //$NON-NLS-1$
				try {
					String text = fDocument.get(event.fOffset - 1, 2);
					if ("<<".equals(text)) { //$NON-NLS-1$
						replaceInDocument(event.fOffset - 1, 2, "«"); //$NON-NLS-1$
						return;
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			} else if ("-".equals(event.fText)) { //$NON-NLS-1$
				if (event.fOffset > 1) {
					try {
						String text = fDocument.get(event.fOffset - 2, 3);
						if ("---".equals(text)) { //$NON-NLS-1$
							replaceInDocument(event.fOffset - 2, 3, "—"); //$NON-NLS-1$
							return;
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			} else if (".".equals(event.fText)) { //$NON-NLS-1$
				if (event.fOffset > 1) {
					try {
						String text = fDocument.get(event.fOffset - 2, 3);
						if ("--.".equals(text)) { //$NON-NLS-1$
							replaceInDocument(event.fOffset - 2, 3, "–"); //$NON-NLS-1$
							return;
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		}

		fMdModel.createModel();
	}
	
	public void replaceInDocument(int offset, int length, String text) {
		fStyledText.getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {

				try {
					fDocument.replace(offset, length, text);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

			}
		});
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		
	}
	
	//==========================
	// update text actions
	
	@Override
	public void mdDocumentUpdated() {
		updateTextActions();
	}

	private void updateTextActions() {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof DipTableEditor) {
			IWorkbenchPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (viewPart instanceof EditViewPart) {
				EditViewPart editViewPart = (EditViewPart) viewPart;
				part = editViewPart.currentEditor();
			}
		}
		
		/*if (!part.equals(fEditor)) {
			return;
		}*/
		Point p = fStyledText.getSelection();
		if (p.x == p.y) {
			int caret = fStyledText.getCaretOffset();
			updateEmptySelection(caret);
		} else {
			updateSelection(p);
		}	
	}
	
	private void updateEmptySelection(int caret) {	
		if (fDocument.getLength() < caret) {
			return;
		}
		BoldAction.instance().updateEmptySelection(fMdDocument, caret);
		ItalicAction.instance().updateEmptySelection(fMdDocument, caret);
		CommentAction.instance().updateEmptySelection(fMdDocument, caret);
		CodeAction.instance().updateEmptySelection(fMdDocument, caret);		
		NumberListAction.instance().updateEmptySelection(fMdDocument, caret);
		MarkerListAction.instance().updateEmptySelection(fMdDocument, caret);
		LinkAction.instance().updateEmptySelection(fMdDocument, caret);
		ParagraphAction.instance().updateEmptySelection(fMdDocument, caret);
	}
	
	private void updateSelection(Point selection) {	
		CommentAction.instance().updateFullSelection(fMdDocument, selection);
		BoldAction.instance().updateFullSelection(fMdDocument, selection);
		ItalicAction.instance().updateFullSelection(fMdDocument, selection);
		CodeAction.instance().updateFullSelection(fMdDocument, selection);
		NumberListAction.instance().updateFullSelection(fMdDocument, selection);
		MarkerListAction.instance().updateFullSelection(fMdDocument, selection);		
		LinkAction.instance().updateFullSelection(fMdDocument, selection);
		ParagraphAction.instance().updateFullSelection(fMdDocument, selection);		
	}
	
	//===========================
	// get & set value
	
	@Override
	public boolean updateTagValue(){		
		String newValue = fStyledText.getText();
		newValue =  newValue.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		newValue =  newValue.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		newValue =  newValue.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!newValue.equals(fTextField.getValue())){
			fTextField.setValue(newValue);
			return true;
		}
		return false;
	}
		
	@Override
	public void setValue(){
		String value = fTextField.getValue();
		String currentValue = getControlValue();
		if (currentValue != null && currentValue.equals(value)) {
			return;
		}
		if (value == null || value.isEmpty()){
			value = ""; //$NON-NLS-1$
			fStyledText.setText(value);
			fStyledText.setData(HINT, false);
			setColors();						
		} else {		
			fStyledText.setText(value);	
			fStyledText.setData(HINT, false);
			setColors();
		}
	}
	
	@Override
	public Field getField() {
		return fTextField;
	}
	
	public boolean isEqualsField() {
		String textValue = fTextField.getValue();
		if (textValue == null) {			
			return true;
		}
		return fStyledText.getText().equals(fTextField.getValue());
	}
	
	public String getControlValue() {
		return fStyledText.getText();
	}
	
	public void setControlValue(String value){
		fStyledText.setText(value);
	}
	
	//====================
	// pack
	
	public void pack(){
		fGroup.pack();
	}
	
	//=======================
	// undo-redo
	
	public void doUndo() {
		fSourceViewer.getUndoManager().undo();
	}
	
	public boolean canUndo() {
		return fSourceViewer.getUndoManager().undoable();
	}
	
	public void doRedo() {
		fSourceViewer.getUndoManager().redo();
	}
	
	public boolean canRedo() {
		return fSourceViewer.getUndoManager().redoable();
	}
	
	//=====================
	// selection
	
	@Override
	public void selectText(String text) {
		String value = fStyledText.getText();
		if (value.contains(text)){
			int start = value.indexOf(text);			
			int end = start + text.length();
			fStyledText.setSelection(start, end);
		}
	}
	
	@Override
	public void addFocusListener() {
		fStyledText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {				
				updateTagValue();	// обновляет тег, + сделать нужно update - после ввода пробельного символа
				if (fStyledText.getText().isEmpty()) {
					setValue();
				}
				fGroup.redraw();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				fGroup.redraw();
			}
		});
		
		// отрисовка границы когда текст в фокусе
		fGroup.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				if (fStyledText.isFocusControl()) {
					Color fg = e.gc.getForeground();
					GC gc = e.gc;
					gc.setForeground(ColorProvider.SELECT);			
					Rectangle rectangle = fStyledText.getBounds();
					rectangle.x = 2;
					rectangle.y = 1;
					rectangle.width = rectangle.width + 5;
					rectangle.height = rectangle.height + 5;
					gc.drawRectangle(rectangle);
					gc.setForeground(fg);
				}
			}
		});
		
	}
	
	@Override
	public void addFocusListener(FocusListener focusListener) {
		fStyledText.addFocusListener(focusListener);
	}

	public void addSelectionListener(SelectionListener listener) {
		fStyledText.addSelectionListener(listener);
	}
	
	public void addCaretListener(CaretListener listener) {
		fStyledText.addCaretListener(listener);
	}
	
	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		fStyledText.addModifyListener(modifyListener);
	}
	
	public boolean isEndCursor(){
		 int offset = getOffset();
		 int length = getLength();
		 return offset == length;
	}
	
	public void setEndCursor(){
		fStyledText.setCaretOffset(fStyledText.getText().length());
	}
	
	public int getOffset(){
		return fStyledText.getCaretOffset();
	}
	
	public int getLength(){
		return fStyledText.getText().length();
	}
	
	public void setCursor(int offset){
		if (offset > getLength()){
			setEndCursor();
		} else {
			fStyledText.setCaretOffset(offset);
		}
		
		int y = fStyledText.getCaret().getLocation().y;
		int index = fStyledText.getLineIndex(y);	
		fStyledText.setTopIndex(index);	
	}
	
	@Override
	public boolean isFocus() {
		return fStyledText.isFocusControl();
	}
	
	@Override
	public void setNullSelection(){
		Point selection = fStyledText.getSelection();
		if (selection.x == selection.y) {
			return;
		}
		fStyledText.setSelection(new Point(0,0));		
	}
	
	public void checkHint() {
		if (fTextField.getValue().isEmpty() && !fStyledText.getText().isEmpty()){
			fStyledText.setText("");	 //$NON-NLS-1$
			setColors();
		}	
	}
	
	//==============================
	
	@Override
	public void dispose(){
		fFont.dispose();
	}
	
	//==============================
	// getters & setters

	public StyledText styledText() {
		return fStyledText;
	}

	public IDocument document() {
		return fDocument;
	}
	
	public SourceViewer sourceViewer() {
		return fSourceViewer;
	}
	
	public MarkdownDocument mdDocument() {
		return fMdDocument;
	}

	public void setFocus() {
		fStyledText.setFocus();
	}
	
	public Point getLocation() {
		return fGroup.getLocation();
	}
	
	public Point size() {
		return fGroup.getSize();
	}
	
	public Control control() {
		return fGroup;
	}
	
}