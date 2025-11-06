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
package ru.dip.editors.formeditor.fieldpage;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextViewer;
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
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.TextField;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.editors.editview.EditViewPart;
import ru.dip.editors.formeditor.FormsEditorActionBarContributor;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.md.MDEditor;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.MdAutoCorrector;
import ru.dip.editors.md.MarkdownDocument.MdDocumentListener;
import ru.dip.editors.md.actions.MdActionUpdater;
import ru.dip.editors.md.field.ContextMenuManager;
import ru.dip.editors.md.field.MdField;
import ru.dip.ui.table.editor.DipTableEditor;

public class TextFieldControl extends FieldControl implements MdDocumentListener, IDocumentListener {

	private static final String HINT = "hint"; //$NON-NLS-1$
	
	// model
	private TextField fTextField;
	private Color fForeground;
	private boolean fShowHint = false;
	private FormsEditor fEditor;
	private int fOldCaretPosition = -1;
	private int fOldLength = 0;	
	
	// context
	private IContextActivation fContextActivation;	
	private final FocusListener fFocusListener = new FocusListener() {

	    @Override
	    public void focusLost(FocusEvent e) {
	        deactivateEditorContext();
	    }

	    @Override
	    public void focusGained(FocusEvent arg0) {
	        activateEditorContext();
	    }
	 };

	// control
	private ScrolledComposite fScrollParentComposite;
	private Group fGroup;
	private MdField fMdField;
	private Font fFont;
	
	private MdActionUpdater fMdActionUpdater;

	
	public Integer getSchemaHeight() {
		return fTextField.getHeight();
	}
			
	public TextFieldControl(FormsEditor editor, Composite parent, TextField field) {
		fEditor = editor;
		fTextField = field;
		fGroup =  new Group(parent, SWT.NONE);
		fFont = getFont();
		setScolledComposite();
		createContent();
		setGroupToolTip();
	}
	
	private void setScolledComposite(){
    	Composite comp = fGroup.getParent().getParent().getParent().getParent();    	    	
    	if (comp instanceof ScrolledComposite){
    		fScrollParentComposite = (ScrolledComposite) comp;
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
		addHintFocusListener();
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
		
		gd.horizontalSpan = FieldsPage.COLUMNS;
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
		int height = styledText().getLineHeight();
		int lines = styledText().getLineCount();
		return lines * height + 20;
	}
		
	private void createText(){
		IDipUnit dipUnit = fEditor != null ? (IDipUnit) fEditor.getDipElement() : null;		
		fMdField = MdField.createWithScroll(dipUnit);
		fMdField.createField(fGroup);	
		fMdField.mdDocument().addListener(this);
		fMdField.document().addDocumentListener(this);
		createActionUpdater();
		styledText().setFont(fFont);
		addMouseWheelListener();
		setContentListener();
		setKeyListener();
		setChangedListener();
		setCaretListener();
		createDND();
	}
		
	private void addMouseWheelListener(){
		styledText().addMouseWheelListener(new MouseWheelListener() {
		    @Override
		    public void mouseScrolled(MouseEvent e) {
		    			    	
            	ScrollBar bar = styledText().getVerticalBar();        	
            	if (fScrollParentComposite != null){
    		        if (!styledText().isFocusControl() ) {   		        	
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
	}
	
	private void createDND() {		
		DragSource source = new DragSource(styledText(), DND.DROP_COPY | DND.DROP_MOVE);
		source.setTransfer(TextTransfer.getInstance());
		source.addDragListener(new DragSourceAdapter() {
			
			Point selection;
			int startSize = 0;
			
			@Override
			public void dragStart(DragSourceEvent e) {
				selection = styledText().getSelection();		
				e.doit = selection.x != selection.y;
				startSize =  styledText().getText().length();
				
			}
			@Override
			public void dragSetData(DragSourceEvent e) {
				e.data = styledText().getText(selection.x, selection.y-1);
			}
			@Override
			public void dragFinished(DragSourceEvent e) {
				if (e.detail == DND.DROP_MOVE) {
					int newSize = styledText().getText().length();
					if (newSize > startSize && styledText().getCaretOffset() < selection.x) {						
						styledText().replaceTextRange(selection.y, selection.y - selection.x, ""); //$NON-NLS-1$
					} else {
						styledText().replaceTextRange(selection.x, selection.y - selection.x, ""); //$NON-NLS-1$
					}								
				}
				selection = null;
			}
		});

		DropTarget target = new DropTarget(styledText(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
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
				styledText().getForeground();
				if (Boolean.TRUE.equals(styledText().getData(HINT))) {
					styledText().setText((String)e.data);
					updateTagValue();
					fShowHint = false;
					styledText().setData(HINT, false);
				} else {
					styledText().insert((String)e.data);
					updateTagValue();
					fShowHint = false;
					styledText().setData(HINT, false);
				}
			}
		});		
	}
	
	private void addHintFocusListener() {
		styledText().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				// setValue();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				setColors();
				if (fShowHint){
					 fShowHint = false;
					 styledText().setText(""); //$NON-NLS-1$
				}				
			}
		});
	}
	
	private void setColors() {
		Color color = fTextField.getBackgroundColor();
		styledText().setBackground(color);
		fForeground = fTextField.getForegraundColor();
		styledText().setForeground(fForeground);
	}
	
	private void setContentListener(){
		styledText().addFocusListener(fFocusListener);
	}
	
	private void setKeyListener(){
		sourceViewer().getTextWidget().addKeyListener(new KeyAdapter() {
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
	
	private void activateEditorContext() {
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService != null)
			fContextActivation = contextService.activateContext(MDEditor.MD_CONTEXT);
	}

	private void deactivateEditorContext() {
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService != null && fContextActivation != null)
			contextService.deactivateContext(fContextActivation);
	}
	
	private void setChangedListener(){
		styledText().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				// обновляет значение если текст изменился более чем на один символ (вставка, удаление и т.п.)
				if (!fShowHint){
					if (Math.abs(styledText().getText().length() - fOldLength) > 1) {
						updateTagValue();
					} 					
					fOldLength = styledText().getText().length();										
				}
			}
		});
	}
	
	private void setCaretListener() {
		styledText().addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				int currentPosition = event.caretOffset;
				if (fOldCaretPosition >= 0 && Math.abs(currentPosition - fOldCaretPosition) > 1) {
					
					if (!fEditor.isUndoRedoProcessing()) {
						updateTagValue();
					}			
				}
				fOldCaretPosition = currentPosition;				
			}		
		});
	}
	
	//========================
	// document listener
	
	
	private MdAutoCorrector fAutoCorrector = new MdAutoCorrector() {
		
		@Override
		protected void replace(int offset, int length, String text) {
			TextFieldControl.this.replaceInDocument(offset, length, text);
		}
	};
	
	@Override
	public void documentChanged(DocumentEvent event) {
		// замена кавычек, длинное тире
		fAutoCorrector.autoCorrect(event);
		fMdField.createMdModel();
	}
	
	public void replaceInDocument(int offset, int length, String text) {
		styledText().getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					document().replace(offset, length, text);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {}
	
	//==========================
	// update text actions
	
	
	private void createActionUpdater() {
		 fMdActionUpdater = new MdActionUpdater(fMdField) {
				
				public void updateActionStatus() {
					if (isEditorPageActive()) {
						super.updateActionStatus();
					}			
				}
		};
	}
	
	@Override
	public void mdDocumentUpdated() {
		fMdActionUpdater.updateActionStatus();
	}
	
	public void updateTextActions() {
		fMdActionUpdater.updateActionStatus();
	}
	
	private boolean isEditorPageActive() {		
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof DipTableEditor) {
			IWorkbenchPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (viewPart instanceof EditViewPart) {
				EditViewPart editViewPart = (EditViewPart) viewPart;
				part = editViewPart.currentEditor();
			}
		}
		return part.equals(fEditor);
	}
	
	//===========================
	// get & set value
	
	@Override
	public boolean updateTagValue(){
		if (fShowHint) {
			return false;
		}
		
		String newValue = styledText().getText();
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
			String hint = fTextField.getHint();
			if (hint != null && !hint.isEmpty()){
				fShowHint = true;
				styledText().setText(hint);
				styledText().setForeground(ColorProvider.GRAY);
				styledText().setData(HINT, true);
			} else {
				value = ""; //$NON-NLS-1$
				fShowHint = false;
				styledText().setText(value);
				styledText().setData(HINT, false);
				setColors();
			}			
		} else {		
			styledText().setText(value);	
			styledText().setData(HINT, false);
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
		if (fShowHint && (textValue == null || textValue.isEmpty())) {
			return true;
		}
		return styledText().getText().equals(fTextField.getValue());
	}
	
	public String getControlValue() {
		return styledText().getText();
	}
	
	public void setControlValue(String value){
		styledText().setText(value);
	}
	
	//====================
	// pack
	
	public void pack(){
		fGroup.pack();
	}
	
	//====================
	// context menu
	
	public void addContextMenu(IFile file) {
		IAction undo = null;
		IAction redo = null;
		IEditorActionBarContributor contributor = fEditor.getActionBarContributor();
		if (contributor instanceof FormsEditorActionBarContributor) {
			FormsEditorActionBarContributor formContributor = (FormsEditorActionBarContributor) contributor;
			undo = formContributor.getUndoAction();
			redo = formContributor.getRedoAction();
		}
		ContextMenuManager menuManager = new ContextMenuManager(styledText(), fEditor.getEditorSite(), undo, redo);
		menuManager.addContextMenu(file);
	}
	
	//=======================
	// undo-redo
	
	public void doUndo() {
		sourceViewer().getUndoManager().undo();
	}
	
	public boolean canUndo() {
		return sourceViewer().getUndoManager().undoable();
	}
	
	public void doRedo() {
		sourceViewer().getUndoManager().redo();
	}
	
	public boolean canRedo() {
		return sourceViewer().getUndoManager().redoable();
	}
	
	//=====================
	// selection
	
	@Override
	public void selectText(String text) {
		String value = styledText().getText();
		if (value.contains(text)){
			int start = value.indexOf(text);			
			int end = start + text.length();
			styledText().setSelection(start, end);
		}
	}
	
	@Override
	public void addFocusListener() {
		styledText().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {				
				updateTagValue();	// обновляет тег, + сделать нужно update - после ввода пробельного символа
				if (styledText().getText().isEmpty()) {
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
				if (styledText().isFocusControl()) {
					Color fg = e.gc.getForeground();
					GC gc = e.gc;
					gc.setForeground(ColorProvider.SELECT);			
					Rectangle rectangle = styledText().getBounds();
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
		styledText().addFocusListener(focusListener);
	}

	public void addSelectionListener(SelectionListener listener) {
		styledText().addSelectionListener(listener);
	}
	
	public void addCaretListener(CaretListener listener) {
		styledText().addCaretListener(listener);
	}
	
	public void addModifyListener(ModifyListener modifyListener) {
		styledText().addModifyListener(modifyListener);
	}
	
	public boolean isEndCursor(){
		 int offset = getOffset();
		 int length = getLength();
		 return offset == length;
	}
	
	public void setEndCursor(){
		styledText().setCaretOffset(styledText().getText().length());
	}
	
	public int getOffset(){
		return styledText().getCaretOffset();
	}
	
	public int getLength(){
		return styledText().getText().length();
	}
	
	public void setCursor(int offset){
		if (offset > getLength()){
			setEndCursor();
		} else {
			styledText().setCaretOffset(offset);
		}
		
		int y = styledText().getCaret().getLocation().y;
		int index = styledText().getLineIndex(y);	
		styledText().setTopIndex(index);	
	}
	
	@Override
	public boolean isFocus() {
		return styledText().isFocusControl();
	}
	
	@Override
	public void setNullSelection(){
		Point selection = styledText().getSelection();
		if (selection.x == selection.y) {
			return;
		}
		styledText().setSelection(new Point(0,0));		
	}
	
	public void checkHint() {
		if (fTextField.getValue().isEmpty() && !styledText().getText().isEmpty()){
			styledText().setText("");	 //$NON-NLS-1$
			fShowHint = false;
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

	public void setReqEditor(FormsEditor editor) {
		fEditor = editor;
	}

	public StyledText styledText() {
		return fMdField.styledText();
	}

	public IDocument document() {
		return fMdField.document();
	}
	
	public TextViewer sourceViewer() {
		return fMdField.getMDViewer();
	}
	
	public MarkdownDocument mdDocument() {
		return fMdField.mdDocument();
	}

	public void setFocus() {
		styledText().setFocus();
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