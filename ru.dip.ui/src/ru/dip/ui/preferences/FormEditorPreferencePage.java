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

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;

public class FormEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private ColorSelector fAttributeColorSelector;
	private ColorSelector fCdataColorSelector;
	private ColorSelector fCommentColorSelector;
	private ColorSelector fTagColorSelector;
	private ColorSelector fDeclarationColorSelector;
	private ColorSelector fDefaultColorSelector;
	private ColorSelector fProcessorInstructionColor;
	private ColorSelector fStringColorSelector;
	
	public FormEditorPreferencePage() {
		
	}

	public FormEditorPreferencePage(String title) {
		super(title);
	}

	public FormEditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//createGuiPageComposite(composite);
		Label xmlLabel = new Label(composite, SWT.NONE);
		xmlLabel.setText(Messages.FormEditorPreferencePage_XmlPreferencesLabel);
		xmlLabel.setFont(FontManager.boldFont);
		createTextPageComposite(composite);
		setValues();
		return composite;
	}
	
	@Override
	protected void performApply() {
		super.performApply();
	}
	
	private void createTextPageComposite(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fAttributeColorSelector = new ColorSelector(composite);
		Label attributeLabel = new Label(composite, SWT.NONE);
		attributeLabel.setText(Messages.FormEditorPreferencePage_AttributeLabel);
		
		fCdataColorSelector = new ColorSelector(composite);
		Label cdataLabel = new Label(composite, SWT.NONE);
		cdataLabel.setText(Messages.FormEditorPreferencePage_CDATA);		
		
		fCommentColorSelector = new ColorSelector(composite);
		Label comment = new Label(composite, SWT.NONE);
		comment.setText(Messages.FormEditorPreferencePage_Comment);
		
		fTagColorSelector = new ColorSelector(composite);
		Label commentTag = new Label(composite, SWT.NONE);
		commentTag.setText(Messages.FormEditorPreferencePage_Tag);
		
		fDeclarationColorSelector = new ColorSelector(composite);
		Label declarationLabel = new Label(composite, SWT.NONE);
		declarationLabel.setText(Messages.FormEditorPreferencePage_Declaration);
		
		fDefaultColorSelector = new ColorSelector(composite);
		Label defaultLabel = new Label(composite, SWT.NONE);
		defaultLabel.setText(Messages.FormEditorPreferencePage_ByDefault);
		
		fProcessorInstructionColor = new ColorSelector(composite);
		Label processotInstructionLabel = new Label(composite, SWT.NONE);
		processotInstructionLabel.setText(Messages.FormEditorPreferencePage_ProcessorInstruction);		
		
		fStringColorSelector = new ColorSelector(composite);
		Label stringLabel = new Label(composite, SWT.NONE);
		stringLabel.setText(Messages.FormEditorPreferencePage_TextLine);
	}
	
	//==========================
	// init values
	
	private void setValues(){		
		fAttributeColorSelector.setColorValue(ReqEditorSettings.getAttributeColor().getRGB());
		fCdataColorSelector.setColorValue(ReqEditorSettings.getCdataColor().getRGB());
		fCommentColorSelector.setColorValue(ReqEditorSettings.getCommentColor().getRGB());
		fTagColorSelector.setColorValue(ReqEditorSettings.getTagColor().getRGB());
		fDeclarationColorSelector.setColorValue(ReqEditorSettings.getDeclarationColor().getRGB());
		fDefaultColorSelector.setColorValue(ReqEditorSettings.getDefaultColor().getRGB());
		fProcessorInstructionColor.setColorValue(ReqEditorSettings.getProcessorInstructionColor().getRGB());
		fStringColorSelector.setColorValue(ReqEditorSettings.getStringColor().getRGB());
	}
	
	@Override
	protected void performDefaults() {
		fAttributeColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_ATTRIBUTE_RGB));
		fCdataColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_CDATA_RGB));;
		fCommentColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_COMMENT_RGB));;
		fTagColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_TAG_RGB));;
		fDeclarationColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_DECLARATION_RGB));;
		fDefaultColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_DEFAULT_RGB));;
		fProcessorInstructionColor.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_PROCESSOR_INSTRUCTION_RGB));;
		fStringColorSelector.setColorValue(ColorProvider.getRGB(ReqEditorSettings.DEFAULT_STRING_RGB));;
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		ReqEditorSettings settings = ReqEditorSettings.getInstance();
		settings.updateAttributeColor(fAttributeColorSelector.getColorValue());
		settings.updateCdataColor(fCdataColorSelector.getColorValue());
		settings.updateCommentColor(fCommentColorSelector.getColorValue());
		settings.updateTagColor(fTagColorSelector.getColorValue());
		settings.updateDeclarationColor(fDeclarationColorSelector.getColorValue());
		settings.updateDefaultColor(fDefaultColorSelector.getColorValue());
		settings.updateProcessorInstructionColor(fProcessorInstructionColor.getColorValue());
		settings.updateStringColor(fStringColorSelector.getColorValue());	
		return super.performOk();
	}

}
