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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.ReqUIPlugin;

public class ReqEditorSettings {
	
	//======================
	// singleton
	
	private static ReqEditorSettings instance;
	
	private ReqEditorSettings(){
		setPreferences();
	}
	
	public static ReqEditorSettings getInstance(){
		if (instance == null){
			instance = new ReqEditorSettings();
		}
		return instance;
	}
	
	//=====================
	// setings
	
	public static final String ATTRIBUTE_RGB = "_attribute_rgb";
	public static final String DEFAULT_ATTRIBUTE_RGB = "127,0,127";
	
	public static final String CDATA_RGB = "_cdata_rgb";
	public static final String DEFAULT_CDATA_RGB = "128,128,128";

	public static final String COMMENT_RGB = "_comment_rgb";
	public static final String DEFAULT_COMMENT_RGB = "63,95,191";
	
	public static final String TAG_RGB = "_comment_tag_rgb";
	public static final String DEFAULT_TAG_RGB = "98,168,166";
	
	public static final String DECLARATION_RGB = "_declaration_rgb";
	public static final String DEFAULT_DECLARATION_RGB = "128,128,128";

	public static final String DEFAULT_RGB = "_default_rgb";
	public static final String DEFAULT_DEFAULT_RGB = "0,0,0";

	public static final String PROCESSOR_INSTRUCTION_RGB = "_processor_instrucion_rgb";
	public static final String DEFAULT_PROCESSOR_INSTRUCTION_RGB = "128,128,128";
	
	public static final String STRING_RGB = "_STRING_rgb";
	public static final String DEFAULT_STRING_RGB = "42,0,255";
	
	private Color fAttributeColor;
	private Color fCdataColor;
	private Color fCommentColor;
	private Color fTagColor;
	private Color fDeclarationColor;
	private Color fDefaultColor;
	private Color fProcessorInstructionColor;
	private Color fStringColor;
	
	//===========================
	// default values
	
	public static void setDefaultValues(IPreferenceStore store){
		store.setDefault(ATTRIBUTE_RGB, DEFAULT_ATTRIBUTE_RGB);
		store.setDefault(CDATA_RGB, DEFAULT_CDATA_RGB);
		store.setDefault(COMMENT_RGB, DEFAULT_COMMENT_RGB);
		store.setDefault(TAG_RGB, DEFAULT_TAG_RGB);
		
		store.setDefault(DECLARATION_RGB, DEFAULT_DECLARATION_RGB);
		store.setDefault(DEFAULT_RGB, DEFAULT_DEFAULT_RGB);
		store.setDefault(PROCESSOR_INSTRUCTION_RGB, DEFAULT_PROCESSOR_INSTRUCTION_RGB);
		store.setDefault(STRING_RGB, DEFAULT_STRING_RGB);
	}
	
	//===========================
	// start preferences
	
	public void setPreferences(){
		IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
		fAttributeColor = ColorProvider.getRGBColor(store.getString(ATTRIBUTE_RGB));
		fCdataColor = ColorProvider.getRGBColor(store.getString(CDATA_RGB));
		fCommentColor = ColorProvider.getRGBColor(store.getString(COMMENT_RGB));
		fTagColor = ColorProvider.getRGBColor(store.getString(TAG_RGB));
		fDeclarationColor = ColorProvider.getRGBColor(store.getString(DECLARATION_RGB));
		fDefaultColor = ColorProvider.getRGBColor(store.getString(DEFAULT_RGB));
		fProcessorInstructionColor = ColorProvider.getRGBColor(store.getString(PROCESSOR_INSTRUCTION_RGB));
		fStringColor = ColorProvider.getRGBColor(store.getString(STRING_RGB));	
	}
	
	//===========================
	// update preferences
	
	public void updateAttributeColor(RGB rgb){
		if (!fAttributeColor.getRGB().equals(rgb)){
			fAttributeColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(ATTRIBUTE_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}
	
	public void updateCdataColor(RGB rgb){
		if (!fCdataColor.getRGB().equals(rgb)){
			fCdataColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(CDATA_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}
	
	public void updateCommentColor(RGB rgb){
		if (!fCommentColor.getRGB().equals(rgb)){
			fCommentColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(COMMENT_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}
	
	public void updateTagColor(RGB rgb){
		if (!fTagColor.getRGB().equals(rgb)){
			fTagColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(TAG_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}
	
	public void updateDeclarationColor(RGB rgb){
		if (!fDeclarationColor.getRGB().equals(rgb)){
			fDeclarationColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(DECLARATION_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}

	public void updateDefaultColor(RGB rgb){
		if (!fDefaultColor.getRGB().equals(rgb)){
			fDefaultColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(DEFAULT_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}	
	
	public void updateProcessorInstructionColor(RGB rgb){
		if (!fProcessorInstructionColor.getRGB().equals(rgb)){
			fProcessorInstructionColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(PROCESSOR_INSTRUCTION_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}
	
	public void updateStringColor(RGB rgb){
		if (!fStringColor.getRGB().equals(rgb)){
			fStringColor = new Color(Display.getDefault(), rgb);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(STRING_RGB, ColorProvider.RGBtoString(rgb));
		}		
	}	
	
	//==========================
	// getters
	
	public static Color getAttributeColor(){
		return getInstance().fAttributeColor;
	}
	
	public static Color getCdataColor(){
		return getInstance().fCdataColor;
	}
	
	public static Color getCommentColor(){
		return getInstance().fCommentColor;
	}
	
	public static Color getTagColor(){
		return getInstance().fTagColor;
	}
	
	public static Color getDeclarationColor(){
		return getInstance().fDeclarationColor;
	}
	
	public static Color getDefaultColor(){
		return getInstance().fDefaultColor;
	}
	
	public static Color getProcessorInstructionColor(){
		return getInstance().fProcessorInstructionColor;
	}
	
	public static Color getStringColor(){
		return getInstance().fStringColor;
	}
	
}
