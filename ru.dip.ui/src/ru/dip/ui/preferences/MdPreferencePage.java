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
package ru.dip.ui.preferences;

import java.util.ArrayList;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.ReqUIPlugin;
import ru.dip.ui.preferences.MdPreferences.PartitionPreferences;

public class MdPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private ArrayList<HighlightPrefComposite> fHighlihgtComposites = new ArrayList<>();
	// controls
	private Text fMaxTextLength;
	
	public MdPreferencePage() {
	}

	public MdPreferencePage(String title) {
		super(title);
	}

	public MdPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());		
		maxLengthComposite(composite);		
		colorsComposite(composite);		
		//setValues();
		return composite;
	}
	
	private void maxLengthComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.MdPreferencePage_MaxLineLength);
		fMaxTextLength = new Text(composite, SWT.BORDER);
		fMaxTextLength.setText(String.valueOf(ReqUIPlugin.getMarkdownMaxLine()));
		fMaxTextLength.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String newValue = fMaxTextLength.getText();
				try {
					Integer.parseInt(newValue);
					setValid(true);
				} catch (NumberFormatException exc) {
					setValid(false);
				}
			}
		});
		GridData gd = new GridData();
		gd.minimumWidth = 70;
		gd.widthHint = 70;
		fMaxTextLength.setLayoutData(gd);
	}
	
	private void colorsComposite(Composite parent) {
		Composite colorComposite  = new Composite(parent, SWT.NONE);
		colorComposite.setLayout(new GridLayout(6, false));
		Label colorsLabel = new Label(colorComposite, SWT.NONE);
		colorsLabel.setText(Messages.MdPreferencePage_SyntaxColor);
		colorsLabel.setLayoutData(GridDataFactory.spanGridData(6));
		colorsLabel.setFont(FontManager.boldFont);
		
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite,
				MdPreferences.commentPreferences(), Messages.MdPreferencePage_Comments));
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite,
				MdPreferences.codePreferences(), Messages.MdPreferencePage_CodeBlocks));
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite,
				MdPreferences.linkPreferences(), Messages.MdPreferencePage_Links));
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite, 
				MdPreferences.listPreferences(), Messages.MdPreferencePage_Lists));
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite, 
				MdPreferences.glossPreferences(), Messages.MdPreferencePage_Terms));
		fHighlihgtComposites.add(new HighlightPrefComposite(colorComposite, 
				MdPreferences.varPreferences(), Messages.MdPreferencePage_Variables));
	}
	
	private class HighlightPrefComposite {
		
		// model
		private PartitionPreferences fPreferences;		
		// controls
		private Button fEnable;
		private ColorSelector fColor;
		private StyledText fText;
		private Button fBold;
		private Button fItalic;
		
		public HighlightPrefComposite(Composite parent, PartitionPreferences preferences, String label) {
			fPreferences = preferences;
			createContent(parent, label);
			setValues();
			addListeners();
		}	
		
		private void createContent(Composite parent, String label) {
			Label comment = new Label(parent, SWT.NONE);
			comment.setText(label);
			fEnable = new Button(parent, SWT.CHECK);			
			fColor = new ColorSelector(parent);		
			fText = new StyledText(parent, SWT.BORDER);
			GridData gd = new GridData();
			gd.widthHint = 80;
			fText.setLayoutData(gd);
			fText.setEditable(false);
			fText.setAlignment(SWT.CENTER);		
			fBold = new Button(parent, SWT.CHECK);
			fBold.setText(Messages.MdPreferencePage_Bold);
			fItalic  = new Button(parent,  SWT.CHECK);
			fItalic.setText(Messages.MdPreferencePage_Italian);	
		}
		
		private void addListeners() {
			fEnable.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					checkEnable();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			
			fColor.addListener(new IPropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					fText.setText(rgbToString(fColor.getColorValue()));
				}
			});
		}
				
		private void setValues() {
			fEnable.setSelection(fPreferences.enable());		
			fColor.setColorValue(fPreferences.color().getRGB());			
			fText.setText(rgbToString(fPreferences.color().getRGB()));
			fBold.setSelection(fPreferences.bold());
			fItalic.setSelection(fPreferences.italic());
			checkEnable();
		}
		
		private void checkEnable() {
			boolean enable = fEnable.getSelection();
			fColor.setEnabled(enable);
			fText.setEnabled(enable);
			fBold.setEnabled(enable);
			fItalic.setEnabled(enable);
		}
		
		private void performDefaults(){
			fEnable.setSelection(fPreferences.defaultEnable());
			fColor.setColorValue(fPreferences.defaultColorRGB());
			fText.setText(rgbToString(fPreferences.defaultColorRGB()));
			fBold.setSelection(fPreferences.defaultBold());
			fItalic.setSelection(fPreferences.defaultItalic());
		}
		
		private void update() {
			fPreferences.update(
				fEnable.getSelection(), 
				fColor.getColorValue(), 
				fBold.getSelection(),
				fItalic.getSelection());
		}
	}

	private String rgbToString(RGB rgb) {
		StringBuilder builder = new StringBuilder();
		builder.append(" #"); //$NON-NLS-1$
		String red = Integer.toHexString(rgb.red);
		if (red.length() == 1) {
			builder.append(0);
		}
		builder.append(red);
		String green = Integer.toHexString(rgb.green);
		if (green.length() == 1) {
			builder.append(0);
		}
		builder.append(green);
		String blue = Integer.toHexString(rgb.blue);
		if (blue.length() == 1) {
			builder.append(0);
		}		
		builder.append(blue);		
		return builder.toString().toUpperCase();
	}
	
	@Override
	protected void performDefaults() {
		fMaxTextLength.setText(String.valueOf(ReqUIPlugin.DEFAULT_MD_MAX_LINE_WIDTH));	
		fHighlihgtComposites.forEach(HighlightPrefComposite::performDefaults);
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		ReqUIPlugin.setMarkdownMaxLine(Integer.parseInt(fMaxTextLength.getText()));
		fHighlihgtComposites.forEach(HighlightPrefComposite::update);
		MdPreferences.instance().fireListeners();
		return super.performOk();
	}

}
