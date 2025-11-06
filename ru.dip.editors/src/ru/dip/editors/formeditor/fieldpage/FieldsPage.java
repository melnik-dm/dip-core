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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.Tag;
import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.formeditor.FormsEditor;
import ru.dip.editors.formeditor.model.FormModel;
import ru.dip.editors.md.actions.BoldAction;
import ru.dip.editors.md.actions.CodeAction;
import ru.dip.editors.md.actions.CommentAction;
import ru.dip.editors.md.actions.ItalicAction;
import ru.dip.editors.md.actions.LinkAction;
import ru.dip.editors.md.actions.MarkerListAction;
import ru.dip.editors.md.actions.NumberListAction;
import ru.dip.editors.md.actions.ParagraphAction;
import ru.dip.ui.glossary.GlossaryHover;

public class FieldsPage extends FormPage {

	public static final int COLUMNS = 2;
	
	// controls
	private Composite fMainComposite;
	private Composite fBody;
	private ScrolledForm fForm;
	private Composite fParent;
	private List<FieldControl> fFieldControls = new ArrayList<>();
	private Label fFileLabel;

	// model
	private FormModel fFormModel;
	private FormsEditor fEditor;	
	private boolean fDirty = false;
	
	public FieldsPage(FormsEditor editor, String title) {
		super(editor, "fieds", title);
		fEditor = editor;
		fFormModel = editor.getFormModel();
	}
	
	//=====================
	// create content
	
	@Override
	public void createPartControl(Composite parent) {
		fParent = parent;
		super.createPartControl(parent);
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		fForm = managedForm.getForm();
		fBody = managedForm.getForm().getBody();		
		fBody.setLayout(new GridLayout());
		createMainComposite(fBody);
		createFileLabelComposite();
		createEntries();	
		createTextListeners();
		updateValues();
	}
	
	private void createMainComposite(Composite parent){
		fMainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(COLUMNS ,false);
		layout.verticalSpacing = 10;
		fMainComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fMainComposite.setLayoutData(gd);
		addMainMouseListener();		
	}
	
	private void addMainMouseListener(){
		fMainComposite.setBackground(ColorProvider.WHITE);
		fMainComposite.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				setFocus();
			}
			
			@Override
			public void mouseDown(MouseEvent e) {}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
	}
	
	private void createFileLabelComposite(){
		Composite labelComposite = new Composite(fMainComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 0;
		labelComposite.setLayout(layout);		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = COLUMNS;
		labelComposite.setLayoutData(gd);
		labelComposite.setBackground(ColorProvider.WHITE);

		IFile file = fEditor.getFile();
		if (file == null){
			return;
		}
		
		Label projectLabel = new Label(labelComposite, SWT.NONE);
		IProject project = file.getProject();
		String projectName = null;
		DipProject dipProject = DipUtilities.findDipProject(file);
		if (dipProject != null) {
			projectName = dipProject.decorateName();
		} else {
			projectName = project.getName();
		}
		projectLabel.setText(projectName);		
		projectLabel.setFont(FontManager.boldFont);
		projectLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		fFileLabel = new Label(labelComposite, SWT.NONE);
		fFileLabel.setText(getIDLabelText(file));
		fFileLabel.setFont(FontManager.boldFont);
		fFileLabel.setForeground(ColorProvider.BLACK);
		fFileLabel.setText(getIDLabelText(file));			
		
		Hyperlink link = new Hyperlink(labelComposite, SWT.NONE);
		link.setBackground(ColorProvider.WHITE);
		link.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		link.setFont(FontManager.boldFont);
		link.setText(" [?]");
		link.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				HelpFormDialog dialog = new HelpFormDialog(fFormModel, labelComposite.getShell());
				dialog.open();
			}
		});

	}

	private String getIDLabelText(IFile file){
		String projectRelativePath = file.getProjectRelativePath().toOSString();	
		return File.separator + projectRelativePath;
	}
		
	private void createEntries(){
		if (fFormModel.getFields() == null) {
			return;
		}
		for (Field field: fFormModel.getFields()){
			FieldControl control = FieldControl.createFiledControl(fEditor, fMainComposite, field);
			fFieldControls.add(control);
			if (control instanceof TextFieldControl){
				configureTextFieldControl((TextFieldControl) control);			
			}			
			control.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {}
				
				@Override
				public void focusGained(FocusEvent e) {
					clearSelection(control);
				}
			});
		}
	}
	
	private void configureTextFieldControl(TextFieldControl textFieldControl) {
		textFieldControl.setReqEditor(fEditor);
		textFieldControl.addContextMenu(fEditor.getFile());
		textFieldControl.addFocusListener();
		textFieldControl.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!textFieldControl.isEqualsField()) {
					fDirty = true;
					fEditor.fireDirty();
				}
			}
		});
	}
	
	private void createTextListeners() {
		for (FieldControl control: fFieldControls) {
			if (control instanceof TextFieldControl) {
				TextFieldControl text = (TextFieldControl) control;
				text.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						updateFocusLost(text);
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						updateTextActions(text);
					}
				});
				
				text.addSelectionListener(new WSelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateTextActions(text);
					}
				});
				
				text.addCaretListener(new CaretListener() {
					
					@Override
					public void caretMoved(CaretEvent event) {
						updateTextActions(text);
					}
				});			
			}			
		}		
	}
	
	private void updateFocusLost(TextFieldControl control) {		
		BoldAction.instance().setEnabled(false);
		ItalicAction.instance().setEnabled(false);
		CommentAction.instance().setEnabled(false);
		CodeAction.instance().setEnabled(false);	
		NumberListAction.instance().setEnabled(false);
		MarkerListAction.instance().setEnabled(false);
		LinkAction.instance().setEnabled(false);
		ParagraphAction.instance().setEnabled(false);
	}
	
	private void updateTextActions(TextFieldControl control) {		
		if (!control.isFocus()) {
			return;
		}	
		control.updateTextActions();
	}
	
	private void clearSelection(FieldControl focusControl) {
		for (FieldControl control: fFieldControls) {
			if (control.equals(focusControl)) {
				continue;
			}
			control.setNullSelection();	
		}
		// закрыть ReductionHover при фокусе на Combo или Check
		if (!(focusControl instanceof TextFieldControl)) {
			GlossaryHover.getInstance().closeHover();
		}	
	}
	
	public void setDirty(boolean dirty) {
		fDirty = dirty;
	}
	
	@Override
	public boolean isDirty() {
		return fDirty;
	}

	//=============================
	// actions
	
	public void autoHeightTextField() {			
		for (FieldControl control: fFieldControls) {
			if (control instanceof TextFieldControl) {
				TextFieldControl textControl = (TextFieldControl) control;
				int contentHeight = textControl.getContentHeight();
				if (contentHeight < 60) {
					textControl.setDataHeight(60);
				} else if (contentHeight > fMainComposite.getSize().y) {
					textControl.setDataHeight(fMainComposite.getSize().y);
				} else {
					textControl.setDataHeight(contentHeight);
				}
			}
		}
		
		// redraw control
		fMainComposite.layout();
		Rectangle rect = fMainComposite.getClientArea();
		fForm.setSize(rect.width, 13000);
		fParent.layout();
	
		// set focus / scroll
		FieldControl textFieldControl = getFocusField();
		if (textFieldControl instanceof TextFieldControl) {
			scrolToField((TextFieldControl) textFieldControl);	
		}
	}
	
	public void goToPreviousTextField() {
		FieldControl focusControl = getFocusField();
		if (focusControl == null) {
			return;
		}
		int index = fFieldControls.indexOf(focusControl);
		for (int i = index -1; i>= 0; i--) {
			FieldControl control = fFieldControls.get(i);
			if (control instanceof TextFieldControl) {
				TextFieldControl textControl = (TextFieldControl) control;
				textControl.setFocus();
				scrolToField(textControl);
				return;
			}						
		}

	}
	
	public void goToNextTextField() {
		FieldControl focusControl = getFocusField();
		if (focusControl == null) {
			return;
		}
		int index = fFieldControls.indexOf(focusControl);
		for (int i = index +1; i < fFieldControls.size(); i++) {
			FieldControl control = fFieldControls.get(i);
			if (control instanceof TextFieldControl) {
				TextFieldControl textControl = (TextFieldControl) control;
				textControl.setFocus();
				scrolToField(textControl);
				return;
			}
		}
	}
	
	public void scrolToField(TextFieldControl textFieldControl) {
		ScrolledForm form = getManagedForm()
				.getForm();
		int textY = textFieldControl.getLocation().y;
		int textHeight = textFieldControl.size().y;
		int formHeight = form.getSize().y;
				
		if (textHeight  > formHeight) {
			// текстовое поле больше по размеру, нужно выставлять по курсору			
		} else {
			int y = textY - ((formHeight - textHeight)/2);
			if (y < 0) {
				y = 0;
			}
			form.setOrigin(form.getOrigin().x, y);
		}
	}
	
	//=============================
	// update
	
	public void update(){
		updateValues();
		clearSelection();
	}
	
	public void updateValues(){
		fFieldControls.forEach(FieldControl::setValue);
	}
	
	public boolean updateTagValues() {
		boolean result = false;
		for (FieldControl control: fFieldControls) {
			boolean change = control.updateTagValue();
			if (change) {
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * Проверяет, чтобы не отображались хинты при установленом фокусе
	 */
	public void checkHint() {
		FieldControl fieldControl = getFocusField();
		if (fieldControl != null && fieldControl instanceof TextFieldControl) {
			TextFieldControl textFieldControl = (TextFieldControl) fieldControl;
			textFieldControl.checkHint();
		}
	}

	public void updateComboItems(){
		fFieldControls
		.stream()
		.filter(ComboFieldControl.class::isInstance)
		.forEach(combo -> ((ComboFieldControl) combo).updateItems());
	}
	
	//=============================
	// select
	
	public void select(Tag tag, String text){
		FieldControl fieldControl = findFieldControl(tag);
		if (fieldControl != null){
			fieldControl.selectText(text);
		}
	}
	
	public FieldControl findFieldControl(Tag tag){
		for (FieldControl fieldControl: fFieldControls){
			if (tag.equals(fieldControl.getField().getTag())){
				return fieldControl;
			}
		}
		return null;
	}
	
	public void selectText(String text) {
		for (FieldControl control: fFieldControls) {
			if (control instanceof TextFieldControl) {
				TextFieldControl textControl = (TextFieldControl) control;
				String controlContent = textControl.getControlValue();
				controlContent.indexOf(text);
				textControl.selectText(text);
				return;				
			}
		}				
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		((FormsEditor)getEditor()).setFocus();
		clearSelection();
	}
	
	private void clearSelection(){
		if (fFieldControls.size() > 0 && fFieldControls.get(0) instanceof ComboFieldControl){
			ComboFieldControl comboFieldControl = (ComboFieldControl) fFieldControls.get(0);
			comboFieldControl.setNullSelection();			
		} 
		GlossaryHover.getInstance().closeHover();
	}

	@Override
	public void dispose() {
		super.dispose();
		fFieldControls.forEach(FieldControl::dispose);
	}
	
	@Override
	public FormsEditor getEditor() {
		return (FormsEditor) super.getEditor();
	}
	
	public FieldControl getFocusField(){
		for (FieldControl control: fFieldControls){
			if (control.isFocus()){
				return control;
			}
		}
		return null;
	}
	
	public List<FieldControl> fieldControls() {
		return fFieldControls;
	}

}
