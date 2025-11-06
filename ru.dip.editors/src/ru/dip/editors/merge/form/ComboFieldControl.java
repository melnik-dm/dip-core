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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.Item;
import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;

public class ComboFieldControl  extends FieldControl  {

	// model		
	private ComboField fComboField;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private boolean fShowHint = false; // флаг - установлена ли в данный момент подсказка
	private boolean fReadonly = false;
	// control
	private Composite fParentComposite;
	private Composite fMainComposite;
	private Combo fCombo;
	private Font fFont;
	

	public ComboFieldControl(Composite parent, ComboField field, boolean readOnly) {
		fParentComposite = parent;
		fMainComposite = new Composite(fParentComposite, SWT.NONE);
		fMainComposite.setLayout(new GridLayout());
		
		fComboField = field;
		fReadonly = readOnly;
		fBackgroundColor = fComboField.getBackgroundColor();
		fForegroundColor = fComboField.getForegraundColor();
		fFont = getFont();
		createContent();
	}
	
	@Override
	void updateStatus() {
		fMainComposite.setBackground(getDiffColor());
	}
	
	//==============================
	// content
	
	private void createContent(){
		createLabel();
		createCombo();
	}
	
	private void createLabel(){
		Label label = new Label(fMainComposite, SWT.NONE);
		label.setForeground(ColorProvider.SELECT);
		String  title = fComboField.getTitle();
		label.setText(title);	
		setLabelToolTip(label);
	}
	
	private void setLabelToolTip(Label label){
		String hint = fComboField.getHint();
		if (hint != null){
			label.setToolTipText(hint);
		}
	}
	
	private void createCombo(){
		int style = SWT.NONE;
		if (fReadonly) {
			style |= SWT.READ_ONLY;
		}		
		fCombo = new Combo(fMainComposite, style);
		if (!fReadonly) {
			fCombo.setItems(fComboField.getItemValues());
		} else {
			fCombo.setLayoutData(GridDataFactory.widthGridData(150));
		}
			
		enableContentProposal(fCombo);
		addAutoCompleteFeature(fCombo);		
		setColors();
		addFocusListener();
		fCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index= fCombo.getSelectionIndex();
				if (index >= 0){
					fShowHint = false;
				}
				updateColors();
				//updateItems();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});		
		setChangedListener();		
	}
	
	//=========================
	// autocomplete
	
	public static void addAutoCompleteFeature(Combo combo) {
		combo.addKeyListener(new KeyListener() {
			
			String oldValue = "";
			int pos;
			
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				Combo cmb = (Combo)keyEvent.getSource();
				setClosestMatch(cmb);
			}
			
			@Override
			public void keyPressed(KeyEvent keyEvent) {
				String text = combo.getText();
				if (isNormalText(text)) {
					oldValue = combo.getText();
					pos = combo.getCaretPosition();
				} 
				if (keyEvent.keyCode == SWT.BS) {
					Combo cmb = ((Combo) keyEvent.getSource());
					Point pt = cmb.getSelection();
					cmb.setSelection(new Point(Math.max(0, pt.x -1), pt.y));
				}
			}
			
			private void setClosestMatch(Combo combo) {
				String str = combo.getText();
				if (!isNormalText(str)) {
					combo.setText(oldValue);;
					combo.setSelection(new Point(pos, pos));
				}
			}
			
			private boolean isNormalText(String str) {
				String[] cItems = combo.getItems();
				int index = -1;
				for (int i = 0; i < cItems.length; i++) {
					if (cItems[i].toLowerCase().startsWith(str.toLowerCase())) {
						index = i;
						break;
					}
				}
				return index != -1;
			}
		});
	}
	
	public static void enableContentProposal(Combo combo) {
		SimpleContentProposalProvider proposalProvider = null;
		ContentProposalAdapter proposalAdapter = null;
		proposalProvider = new SimpleContentProposalProvider(combo.getItems());
		proposalAdapter = new ContentProposalAdapter(combo, new ComboContentAdapter(), proposalProvider,
				getActivationKeystroke(), getAutoactivationChars());
		proposalProvider.setFiltering(true);
		proposalAdapter.setPropagateKeys(true);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}
	
	static KeyStroke getActivationKeystroke() {
		KeyStroke instance = KeyStroke.getInstance(SWT.CTRL, (int)' ');
		return instance;
	}
	
	private static final String LCL = "abcdefghijklmnopqrstuvwxyz" +
	"абвгдуёжзийклмнопрстуфхцчшщъыьэюя";
	private static final String UCL = LCL.toUpperCase();
	private static final String NUMS = "0123456789";
	
	static char[] getAutoactivationChars() {
		String delete = new String(new char[] {8});
		String allChars = LCL + UCL + NUMS + delete;
		return allChars.toCharArray();
	}
	
	//=========================
	// items
	
	public void updateItems() {
		ComboField field = (ComboField) getField();
		String currentValue = fCombo.getText();
		String[] itemValues = field.getComboItemValues(currentValue);	
		fCombo.setItems(itemValues);
		fCombo.setText(currentValue);
		updateColors();
	}
	
	//==================================================

	public void addFocusListener(){
		fCombo.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				 Point cursorLocation = Display.getCurrent().getCursorLocation();
				 Point relativeCursorLocation = fCombo.getParent().toControl(cursorLocation);
				 boolean contains = fCombo.getBounds().contains(relativeCursorLocation);
				 if (contains){
					 setDefaultColors();
				 } else {
					 setValue();
					 updateColors();
				 }
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				 setDefaultColors();
				 if (fShowHint){
					 fShowHint = false;
					 fCombo.setText("");
				 }
			}
		});
	}
	
	@Override
	public void addFocusListener(FocusListener focusListener) {
		fCombo.addFocusListener(focusListener);
	}
	
	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		WSelectionListener listener = e -> modifyListener.modifyText(null);
		fCombo.addSelectionListener(listener);
	}
	
	private void setColors(){
		fCombo.setForeground(fForegroundColor);
		fCombo.setBackground(fBackgroundColor);
	}
	
	private void setChangedListener(){
		fCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!fShowHint){
					updateTagValue();
				}
			}
		});
	}

	private void updateColors(){
		String value = fComboField.getValue();
		if (value != null && !value.isEmpty()){
			Item item = fComboField.getItem(value);
			if (item != null){
				setItemColor(item);
			} else {
				setColors();
			}
		} else {
			String hint  = fComboField.getHint();
			if (hint != null && !hint.isEmpty()){
				fCombo.setForeground(ColorProvider.GRAY);
				fCombo.setBackground(null);
			}
		}
	}
	
	private void setItemColor(Item item){
		Color backColor = item.getBackgroundColor();
		Color foreColor = item.getForegroundColor();
		if (backColor != null){
			fCombo.setBackground(backColor);
		} else if (fBackgroundColor != null) {
			fCombo.setBackground(fBackgroundColor);
		} else {
			fCombo.setBackground(null);
		}
		if (foreColor != null){
			fCombo.setForeground(foreColor);
		} else if (fForegroundColor != null){
			fCombo.setForeground(fForegroundColor);						 
		} else {
			fCombo.setForeground(null);
		}	
	}
	
	private void setDefaultColors(){
		fCombo.setBackground(null);
		fCombo.setForeground(null);
	}

	//===============================
	// get & set value 
	
	public boolean updateTagValue(){
		if (fShowHint) {
			return false;
		}
		String newValue = fCombo.getText();
		if (!newValue.equals(fComboField.getValue())){
			fComboField.setValue(newValue);
			return true;
		}
		return false;
	}
	
	public void setValue(){
		String value = fComboField.getValue();
		if (value == null || value.isEmpty()){
			String hint = fComboField.getHint();
			if (hint != null && !hint.isEmpty()){
				fShowHint = true;
				fCombo.setText(hint);
				fCombo.setForeground(ColorProvider.GRAY);
			} else {
				value = "";
				fShowHint = false;
				if (fReadonly) {
					fCombo.setItems(value);
				}
				
				fCombo.setText(value);
			}			
		} else {
			fShowHint = false;
			if (fReadonly) {
				fCombo.setItems(value);
			}
			fCombo.setText(value);
		}
		updateColors();		
	}
	
	public void dispose(){
		fFont.dispose();
	}

	@Override
	public Field getField() {
		return fComboField;
	}

	//========================
	// selection
	
	@Override
	public void selectText(String text) {
		String value = fCombo.getText();
		if (value.contains(text)){
			fCombo.setSelection(new Point(0, value.length()));			
		}
	}
	
	@Override
	public void setNullSelection(){
		if (fCombo != null){
			fCombo.setSelection(new Point(0,0));
		}
	}
	
	@Override
	public boolean isFocus() {
		return fCombo.isFocusControl();
	}

}
