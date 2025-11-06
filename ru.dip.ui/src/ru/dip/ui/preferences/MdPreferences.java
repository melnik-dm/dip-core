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
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import ru.dip.ui.ReqUIPlugin;

public class MdPreferences {
	
	public static class PartitionPreferences {
		
		private static final TextAttribute DEFAULT_TEXT_ATTRIBUTE = new TextAttribute(null);
		
		private boolean fEnable;
		private Color fColor;
		private boolean fBold;
		private boolean fItalic;
		
		private String fTitle;
		private boolean fDefalultEnable = true;
		private RGB fDefaultRGB;
		private boolean fDefaultBold = false;
		private boolean fDefaultItalic = false;
		
		private final String PREF_ENABLE;
		private final String PREF_RGB1;
		private final String PREF_RGB2;
		private final String PREF_RGB3;
		private final String PREF_BOLD;
		private final String PREF_ITALIC;
		
		public PartitionPreferences(String title, RGB defaultRGB, boolean defaultBold, boolean defaultItalic) {
			this(title, defaultRGB);
			fDefaultBold = defaultBold;
			fDefaultItalic = defaultItalic;
		}
		
		public PartitionPreferences(boolean defaultEnable, String title, RGB defaultRGB) {
			this(title, defaultRGB);
			fDefalultEnable = defaultEnable;
		}
		
		public PartitionPreferences(String title, RGB defaultRGB) {
			fTitle = title;
			fDefaultRGB = defaultRGB;
			PREF_ENABLE = "_ENABLE_" + fTitle;
			PREF_RGB1 = "_RGB1_" + fTitle;
			PREF_RGB2 = "_RGB2_" + fTitle;
			PREF_RGB3 = "_RGB3_" + fTitle;	
			PREF_BOLD = "_BOLD_" + fTitle;
			PREF_ITALIC = "_ITALIC_" + fTitle;
		}
				
		public void setDefaultPrefereneces(IPreferenceStore store) {
			store.setDefault(PREF_ENABLE, fDefalultEnable);
			store.setDefault(PREF_RGB1, fDefaultRGB.red);
			store.setDefault(PREF_RGB2, fDefaultRGB.green);
			store.setDefault(PREF_RGB3, fDefaultRGB.blue);
			store.setDefault(PREF_BOLD, fDefaultBold);			
			store.setDefault(PREF_ITALIC, fDefaultItalic);
		}
		
		public void setPreferences(IPreferenceStore store, Display display) {
			fEnable = store.getBoolean(PREF_ENABLE);
			RGB codeRgb = new RGB(store.getInt(PREF_RGB1), store.getInt(PREF_RGB2), store.getInt(PREF_RGB3));
			fColor = new Color(display, codeRgb);
			fBold = store.getBoolean(PREF_BOLD);
			fItalic = store.getBoolean(PREF_ITALIC);
		}
		
		public void dispose() {
			if (fColor != null && !fColor.isDisposed()) {
				fColor.dispose();
			}
		}
		
		public void update(boolean enable, RGB color, boolean bold, boolean italic) {
			updateEnable(enable);
			updateColor(color);
			updateBold(bold);
			updateItalic(italic);
		}

		public void updateEnable(boolean newValue) {
			if (fEnable != newValue) {
				fEnable = newValue;
				ReqUIPlugin.getDefault().getPreferenceStore().setValue(PREF_ENABLE, fEnable);
			}
		}
		
		public void updateColor(RGB newRgb){
			if (!fColor.getRGB().equals(newRgb)){
				fColor = new Color(Display.getDefault(), newRgb);
				IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
				store.setValue(PREF_RGB1, newRgb.red);
				store.setValue(PREF_RGB2, newRgb.green);
				store.setValue(PREF_RGB3, newRgb.blue);
			}
		}
		
		public void updateBold(boolean newValue){
			if (fBold != newValue){
				fBold = newValue;
				ReqUIPlugin.getDefault().getPreferenceStore().setValue(PREF_BOLD, fBold);
			}
		}
		
		public void updateItalic(boolean newValue){
			if (fItalic != newValue){
				fItalic = newValue;
				ReqUIPlugin.getDefault().getPreferenceStore().setValue(PREF_ITALIC, fItalic);
			}
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(fTitle);
			builder.append("-");
			builder.append(fEnable);
			builder.append("-");
			builder.append(fColor);
			builder.append("-");
			builder.append(fBold);
			builder.append("-");
			builder.append(fItalic);
			return builder.toString();
		}
				
		//========================
		// default getters
		
		public boolean defaultEnable() {
			return fDefalultEnable;
		}
		
		public RGB defaultColorRGB() {
			return fDefaultRGB;
		}
		
		public boolean defaultBold() {
			return fDefaultBold;
		}
		
		public boolean defaultItalic() {
			return fDefaultItalic;
		}
		
		//=========================
		// getters
		
		public boolean enable() {
			return fEnable;
		}
		
		public Color color() {
			return fColor;
		}
		
		public boolean bold() {
			return fBold;
		}
		
		public boolean italic() {
			return fItalic;
		}
		
		public TextAttribute textAttribute() {
			if (!fEnable || !MdPreferences.highlghtEnable()) {
				return DEFAULT_TEXT_ATTRIBUTE;
			}			
			int style = SWT.NONE;
			if (fBold) {
				style = style | SWT.BOLD;
			}
			if (fItalic) {
				style = style | SWT.ITALIC;
			}
			return new TextAttribute(fColor, null, style);
		}			
	}
	
	//======================
	// singleton
	
	private static MdPreferences instance;
	
	private MdPreferences(){
		createPartitionPreferences();
	}
	
	public static MdPreferences instance(){
		if (instance == null){
			instance = new MdPreferences();
			instance.setPreferences();
		}
		return instance;
	}
	
	// listener interface
	public static interface MdPreferenciesListener {
		
		public void mardownPreferencesChanged();
	}
	
	// показывать форматирование
	public static final String HIGHLIGHT_ENABLE = "_highlihgt_enable";
	public static final boolean DEFAULT_HIGHLIGHT_ENABLE = true;
	
	public static final String AUTO_TEXT_TRANSFER = "_auto_text_transfer";
	public static final String AUTO_CORRECT = "_auto_correct";
	public static final boolean DEFAULT_AUTO_TEXT_TRANSFER = false;
	public static final boolean DEFAULT_AUTO_CORRECT = false;
	public static final boolean DEFAULT_FORMAT_BLOCK_CODE= false;  // автоформат для блоков кода
	public static final String FORMAT_BLOCK_CODE = "_format_block_code";
	
		
	private boolean fHighlightEnable;
	private boolean fAutoTextTransferEnable;
	private boolean fAutoCorrect;
	private boolean fFormatBlockCode;
	private HashMap<String, PartitionPreferences> fPartitionPreferences = new HashMap<>();
	
	public final static String CODE = "code";
	public final static String COMMENT = "comment";
	public final static String LINK = "link";
	public final static String LIST = "list";
	public final static String GLOSS_WORD = "glos_word";
	public final static String VARIABLE_WORD = "variable_word";
	
	private final static RGB DEFAULT_COMMENT_RGB = new RGB(0xC0,0xC0,0xC0);
	private final static RGB DEFAULT_CODE_RGB = new RGB(0x34, 0x65, 0xA4);
	private final static RGB DEFAULT_LINK_RGB = new RGB(0x34, 0x65, 0xA4);
	private final static RGB DEFAULT_LIST_RGB = new RGB(0x34, 0x65, 0xA4);
	private final static RGB DEFAULT_GLOSS_WORD_RGB = new RGB(0x34, 0x65, 0xA4);
	private final static RGB DEFAULT_VARIABLE_WORD_RGB = new RGB(0x34, 0x65, 0xA4);

	private void createPartitionPreferences() {
		fPartitionPreferences.put(COMMENT, new PartitionPreferences(COMMENT, DEFAULT_COMMENT_RGB, false, true));
		fPartitionPreferences.put(CODE, new PartitionPreferences(CODE, DEFAULT_CODE_RGB));
		fPartitionPreferences.put(LINK, new PartitionPreferences(LINK, DEFAULT_LINK_RGB));
		fPartitionPreferences.put(LIST, new PartitionPreferences(LIST, DEFAULT_LIST_RGB, true, false));
		fPartitionPreferences.put(GLOSS_WORD, new PartitionPreferences(false, GLOSS_WORD, DEFAULT_GLOSS_WORD_RGB));
		fPartitionPreferences.put(VARIABLE_WORD, new PartitionPreferences(false, VARIABLE_WORD, DEFAULT_VARIABLE_WORD_RGB));		

	}
	
	// listeners	
	private ArrayList<MdPreferenciesListener> fListeners = new ArrayList<>();
	
	public void fireListeners() {
		for (MdPreferenciesListener listener: fListeners) {
			listener.mardownPreferencesChanged();
		}
	}
	
	public void addListener(MdPreferenciesListener listener) {
		fListeners.add(listener);
	}
	
	public  void removeListener(MdPreferenciesListener listener) {
		fListeners.remove(listener);
	}
	
	//===========================
	// default values
			
	public static void setDefaultValues(IPreferenceStore store){
		store.setDefault(HIGHLIGHT_ENABLE, DEFAULT_HIGHLIGHT_ENABLE);		
		store.setDefault(AUTO_TEXT_TRANSFER, DEFAULT_AUTO_TEXT_TRANSFER);
		store.setDefault(AUTO_CORRECT, DEFAULT_AUTO_CORRECT);
		store.setDefault(FORMAT_BLOCK_CODE, DEFAULT_FORMAT_BLOCK_CODE);
		instance().fPartitionPreferences.values()
			.forEach((part) -> part.setDefaultPrefereneces(store));
	}
	
	// ===========================
	// start preferences

	public void setPreferences() {
		IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
		Display display = Display.getDefault();		
		fHighlightEnable = store.getBoolean(HIGHLIGHT_ENABLE);
		fAutoTextTransferEnable = store.getBoolean(AUTO_TEXT_TRANSFER);
		fAutoCorrect = store.getBoolean(AUTO_CORRECT);
		fFormatBlockCode = store.getBoolean(FORMAT_BLOCK_CODE);
		fPartitionPreferences.values().forEach((part) -> part.setPreferences(store, display));
	}

	//===========================
	// dispose preferences
	
	public void disposeResources() {
		instance.fPartitionPreferences.values()
			.forEach(PartitionPreferences::dispose);
	}
	
	//===========================
	// update preferences
	
	public void updateHighlightEnable(boolean newValue){
		if (fHighlightEnable != newValue){
			fHighlightEnable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(HIGHLIGHT_ENABLE, fHighlightEnable);
		}
	}
	
	public void udpateAutoTextTransfer(boolean newValue) {
		if (fAutoTextTransferEnable != newValue) {
			fAutoTextTransferEnable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(AUTO_TEXT_TRANSFER, fAutoTextTransferEnable);
		}
	}
	
	public void updateAutoCorrect(boolean newValue) {
		if (fAutoCorrect != newValue) {
			fAutoCorrect = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(AUTO_CORRECT, fAutoCorrect);
		}
	}
	
	public void updateFormatBlockCode(boolean newValue) {
		if (fFormatBlockCode != newValue) {
			fFormatBlockCode = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(FORMAT_BLOCK_CODE, fFormatBlockCode);
		}
	}
	
	//============================
	// getters
	
	public static boolean highlghtEnable() {
		return instance().fHighlightEnable;
	}
	
	public static boolean autoTextTransfer() {
		return instance().fAutoTextTransferEnable;
	}
	
	public static boolean autoCorrect() {
		return instance().fAutoCorrect;
	}
	
	public static boolean formatBlockCode() {
		return instance().fFormatBlockCode;
	}
	
	private PartitionPreferences mdPartitionPreferences(String key) {
		return fPartitionPreferences.get(key);
	}
	
	public static PartitionPreferences codePreferences() {
		return instance().mdPartitionPreferences(CODE);
	}
	
	public static PartitionPreferences commentPreferences() {
		return instance().mdPartitionPreferences(COMMENT);
	}
	
	public static PartitionPreferences listPreferences() {
		return instance().mdPartitionPreferences(LIST);
	}
	
	public static PartitionPreferences linkPreferences() {
		return instance().mdPartitionPreferences(LINK);
	}
	
	public static PartitionPreferences glossPreferences() {
		return instance().mdPartitionPreferences(GLOSS_WORD);
	}
	
	public static PartitionPreferences varPreferences() {
		return instance().mdPartitionPreferences(VARIABLE_WORD);
	}

}
