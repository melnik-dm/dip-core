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
package ru.dip.ui.table.table;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import ru.dip.core.unit.md.MarkdownSettings;
import ru.dip.core.utilities.ui.swt.FontDimension;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.ReqUIPlugin;

public final class TableSettings {
	
	//======================
	// singleton
	
	private static TableSettings instance;
	
	private TableSettings(){
		setPreferences();
	}
	
	public static TableSettings instance(){
		if (instance == null){
			instance = new TableSettings();
		}
		return instance;
	}
	
	//=====================
	// setings
	
	// отступы
	public static final String MARGIN_TOP = "_table_margin_top";
	public static final String MARGIN_BOTTOM = "_table_margin_bottom";
	public static final String MARGIN_LEFT = "_table_left_margin";
	public static final String MARGIN_RIGHT = "_table_right_margin";
	
	public static final int DEFAULT_MARGIN_TOP = 5; 
	public static final int DEFAULT_MARGIN_BOTTOM = 5;
	public static final int DEFAULT_MARGIN_LEFT = 5;
	public static final int DEFAULT_MARGIN_RIGHT = 3;
	
	public static int VERSION_TOP_INDENT = 0;
	public static int VERSION_BOTTOM_INDENT = 0;
	public static int VERSION_LEFT_INDENT = 0;
	public static int VERSION_RIGHT_INDENT = 0;
	
	static {
		// на старых версиях эклипс - не нужно
		VERSION_TOP_INDENT += 1;
		VERSION_BOTTOM_INDENT += 1;
		VERSION_LEFT_INDENT += 1;
		VERSION_RIGHT_INDENT += 1;
	}
	
	private int fMarginTop;
	private int fMarginBottom;
	private int fMarginLeft;
	private int fMarginRight;
	
	// режимы заливки
	public static final int FILES_BACKGROUND_MODE = 0; 		// чередование строк
	public static final int FOLDERS_BACKGROUND_MODE = 1;	// чередование директорий
	public static final int FOLDER_ITEMS_MODE = 2;			// чередование строк начиная с директории
	public static final int FOLDERS_FILES_MODE = 3;			// директории/строки
	public static final int NOT_BACKGROUND_MODE = 4;		// нет заливки
	// цвета	
	public static final String LINE_RGB_1 = "_line_rgb_1";
	public static final String LINE_RGB_2 = "_line_rgb_2";
	public static final String LINE_RGB_3 = "_line_rgb_3";
	public static final int DEFAULT_LINE_RGB_1 = 192;
	public static final int DEFAULT_LINE_RGB_2 = 192;
	public static final int DEFAULT_LINE_RGB_3 = 192;
	public static final RGB DEFAULT_LINE_RGB = new RGB(DEFAULT_LINE_RGB_1, DEFAULT_LINE_RGB_2, DEFAULT_LINE_RGB_3);
	
	public static final String TABLE_1_RRB_1 = "_table1_rgb_1";
	public static final String TABLE_1_RRB_2 = "_table1_rgb_2";
	public static final String TABLE_1_RRB_3 = "_table1_rgb_3";
	public static final int DEFAULT_TABLE_1_RGB_1 = 255;
	public static final int DEFAULT_TABLE_1_RGB_2 = 255;
	public static final int DEFAULT_TABLE_1_RGB_3 = 255;
	public static final RGB DEFAULT_TABLE_1_RGB = new RGB(DEFAULT_TABLE_1_RGB_1, DEFAULT_TABLE_1_RGB_2, DEFAULT_TABLE_1_RGB_3);
	
	public static final String TABLE_2_RRB_1 = "_table2_rgb_1";
	public static final String TABLE_2_RRB_2 = "_table2_rgb_2";
	public static final String TABLE_2_RRB_3 = "_table2_rgb_3";
	public static final int DEFAULT_TABLE_2_RGB_1 = 238;
	public static final int DEFAULT_TABLE_2_RGB_2 = 238;
	public static final int DEFAULT_TABLE_2_RGB_3 = 236;
	public static final RGB DEFAULT_TABLE_2_RGB = new RGB(DEFAULT_TABLE_2_RGB_1, DEFAULT_TABLE_2_RGB_2, DEFAULT_TABLE_2_RGB_3);

	public static final String SELECT_RRB_1 = "_select_rgb_1";
	public static final String SELECT_RRB_2 = "_select_rgb_2";
	public static final String SELECT_RRB_3 = "_select_rgb_3";
	public static final int DEFAULT_SELECT_RGB_1 = 255;
	public static final int DEFAULT_SELECT_RGB_2 = 238;
	public static final int DEFAULT_SELECT_RGB_3 = 166;
	public static final RGB DEFAULT_SELECT_RGB = new RGB(DEFAULT_SELECT_RGB_1, DEFAULT_SELECT_RGB_2, DEFAULT_SELECT_RGB_3);
		
	public static final String DISABLE_RRB_1 = "_disable_rgb_1";
	public static final String DISABLE_RRB_2 = "_disable_rgb_2";
	public static final String DISABLE_RRB_3 = "_disable_rgb_3";
	public static final int DEFAULT_DISABLE_RGB_1 = 0xFF;
	public static final int DEFAULT_DISABLE_RGB_2 = 0xD9;
	public static final int DEFAULT_DISABLE_RGB_3 = 0xD9;
	public static final RGB DEFAULT_DISABLE_RGB = new RGB(DEFAULT_DISABLE_RGB_1, DEFAULT_DISABLE_RGB_2, DEFAULT_DISABLE_RGB_3);
				
	private Color fLineColor;
	private Color fTableColor1;
	private Color fTableColor2;
	private Color fSelectColor; 
	private Color fDisableColor;
	
	// размер шрифта
	public static final String ID_FONT_SIZE = "_id_font_size";
	public static final String PRESENTATION_FONT_SIZE = "_presentation_font_size";
	public static final String COMMENT_FONT_SIZE = "_comment_font_size";
	public static final int DEFAULT_ID_FONT_SIZE = 8;
	public static final int DEFAULT_PRESENTATION_FONT_SIZE = 10;
	public static final int DEFAULT_COMMENT_FONT_SIZE = 8;
	
	private int fIdFontSize;
	private int fPresentationFontSize;
	private int fCommentFontSize;
	private Font fIdFont;
	private Font fBoldIdFont;
	private FontDimension fIdFontDimension;
	private FontDimension fIdBoldFontDimension;
	
	private Font fPresentationFont;
	private Font fBoldPresentationFont;
	private Font fBoldMDPresentationFont;
	private Font fItalicPresentationFont;
	private Font fBoldItalicPresentationFont;
	
	private Font fCommentFont;
	private FontDimension fPresentationFontDimension;
	private FontDimension fItalicPresentationFontDimension;
	private FontDimension fBoldPresentationFontDimension;
	private FontDimension fCommentFontDimension;
	
	private TextStyle fBoldMdPresentationStyle;
	
	
	// Markdown
	public static final String MD_FONT_STYLES = "_md_apply_font_styles";
	public static final boolean DEFAULT_MD_FONT_STYLES = true;
	
	public static final String MD_PARAGRAPH_EMPTY_LINE = "_md_parahraph_empty_line";
	public static final boolean DEFAULT_MD_PARAGRAPH_EMPTY_LINE = false;
	
	public static final String MD_LIST_EMPTY_LINE = "_md_list_empty_lien";
	public static final boolean DEFAULT_MD_LIST_EMPTY_LINE = true;
	
	public static final String MD_PARAGRAPH_INDENT_ENABLE = "md_paragraph_indent_enable";
	public static final boolean DEFAULT_MD_PARAGRAPH_INDENT_ENABLE = true;
	
	public static final String MD_PARAGRAPH_INDENT = "_md_paragraph_intent";
	public static final int DEFAULT_MD_PARAGRAPH_INDENT = 4;
	
	private boolean fMDFontStyles;
	private boolean fMDParagraphEmptyLine;
	private boolean fMDListEmptyLine;
	private boolean fMDParagraphIndentEnable;
	private int fMDParagraphIndent;
	private Color fMDCodeFG;
	private Color fMDCommentFG;
		
	// формы	
	public static final String NEW_STR_FOR_TEXT_BOX = "_new_string_for_text_box";
	public static final boolean DEFAULT_NEW_STR_FOR_TEXT_BOX = true;

	public static final String LINE_BETWEEN_ENTRIES = "_line_between_entries";
	public static final boolean DEFAULT_LINE_BETWEEN_ENTRIES = false;	
	
	public static final String WRAP_FIELDS = "_wrap_fields";
	public static final boolean DEFAULT_WRAP_FIELDS = false;	
	
	public static final String NOT_SHOW_EMPTY_FIELDS = "_not_show_empty_fields";
	public static final boolean DEFAULT_NOT_SHOW_EMPTY_FIELDS = false;
	
	public static final String UNDO_REDO_MODE = "_undo_redo_mode";
	public static final boolean DEFAULT_UNDO_REDO_MODE = true;
			
	// другие настройки
	public static final String SHOW_LINE = "_show_line";
	public static final boolean DEFAULT_SHOW_LINE = true;

	public static final String HEADERS_MONO_FONT = "_title_mono_font";
	public static final boolean DEFAULT_HEADERS_MONO_FONT = true;

	public static final String BACKGROUND_MODE = "_background_mode";
	public static final int DEFAULT_BACKGRAUND_MODE = FOLDERS_FILES_MODE;

	public static final String DND_ENABLE = "_dnd_enable";
	public static final boolean DEFAULT_DND_ENABLE = false;

	public static final String WRAP_ID_ENABLE = "_wrap_id_enable";
	public static final boolean DEFAULT_WRAP_ID_ENABLE = true;
	
	public static final String OPEN_LINK_FOLDER_IN_SECTION = "_open_link_folder_section";
	public static final boolean DEFAULT_OPEN_LINK_FOLDER_IN_SECTION = true;
	
	public static final String CSV_TABLE_WIDTH_BY_CONTENT = "_csv_table_by_content";
	public static final boolean DEFAULT_CSV_COLUMN_WIDTH_BY_CONTENT = true;
	
	public static final String EDIT_DESC_IN_TABLE = "_edit_desc_in_table";
	public static final boolean DEFAULT_EDIT_DESC_IN_TABLE = true;
	
	public static final String EDIT_FILE_IN_TABLE = "_edit_file_in_table";
	public static final boolean DEFAULT_EDIT_FILE_IN_TABLE = false;
	
	public static final String FIXED_MODE_FOR_DISABLE_OBJS = "_fixed_mode_for_disable_objs";
	public static final boolean DEFAULT_FIXED_MODE_FOR_DISABLE_OBJS = true;
	
	public static final String RENAME_DISABLE_FILE = "_rename_disable_file";
	public static final boolean DEFAULT_RENAME_DISABLE_FILE = true;
	
	public static final String RENAME_DISABLE_FOLDER = "_rename_disable_folder";
	public static final boolean DEFAULT_RENAME_DISABLE_FOLDER = true;
		
	private boolean fShowLines;
	// 0 - черездование строк, 1 -  чередование директорий, 2 - чередование, начиная с директории, 3 - директории-строки ,4 - отключено
	private int fBackgroundMode;	
	private boolean fHeadersMonoFont;
	private boolean fDndEnable;
	private boolean fWrapIDEnable;
	private boolean fNewStrForTextbox;
	private boolean fLineBetweenEntries;
	private boolean fWrapField;
	private boolean fNotShowEmptyFields;
	private boolean fUndoRedoMode;
	private boolean fOpenLinkFolderSection;
	private boolean fCsvColumnWidthByContent;
	private boolean fEditDescInTable;
	private boolean fEditFileInTable;
	private boolean fFixedModeForDisableObjs;
	private boolean fRenameDisableFile;
	private boolean fRenameDisableFolder;
	
	//===========================
	// default values
	
	public static void setDefaultValues(){
		IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(MARGIN_TOP, DEFAULT_MARGIN_TOP);
		store.setDefault(MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM);
		store.setDefault(MARGIN_LEFT, DEFAULT_MARGIN_LEFT);
		store.setDefault(MARGIN_RIGHT, DEFAULT_MARGIN_RIGHT);
		
		store.setDefault(LINE_RGB_1, DEFAULT_LINE_RGB_1);
		store.setDefault(LINE_RGB_2, DEFAULT_LINE_RGB_2);
		store.setDefault(LINE_RGB_3, DEFAULT_LINE_RGB_3);

		store.setDefault(TABLE_1_RRB_1, DEFAULT_TABLE_1_RGB_1);
		store.setDefault(TABLE_1_RRB_2, DEFAULT_TABLE_1_RGB_2);
		store.setDefault(TABLE_1_RRB_3, DEFAULT_TABLE_1_RGB_3);
		
		store.setDefault(TABLE_2_RRB_1, DEFAULT_TABLE_2_RGB_1);
		store.setDefault(TABLE_2_RRB_2, DEFAULT_TABLE_2_RGB_2);
		store.setDefault(TABLE_2_RRB_3, DEFAULT_TABLE_2_RGB_3);

		store.setDefault(SELECT_RRB_1, DEFAULT_SELECT_RGB_1);
		store.setDefault(SELECT_RRB_2, DEFAULT_SELECT_RGB_2);
		store.setDefault(SELECT_RRB_3, DEFAULT_SELECT_RGB_3);
		
		store.setDefault(DISABLE_RRB_1, DEFAULT_DISABLE_RGB_1);
		store.setDefault(DISABLE_RRB_2, DEFAULT_DISABLE_RGB_2);
		store.setDefault(DISABLE_RRB_3, DEFAULT_DISABLE_RGB_3);
		
		store.setDefault(ID_FONT_SIZE, DEFAULT_ID_FONT_SIZE);
		store.setDefault(PRESENTATION_FONT_SIZE, DEFAULT_PRESENTATION_FONT_SIZE);
		store.setDefault(COMMENT_FONT_SIZE, DEFAULT_COMMENT_FONT_SIZE);
		
		store.setDefault(MD_FONT_STYLES, DEFAULT_MD_FONT_STYLES);
		store.setDefault(MD_PARAGRAPH_EMPTY_LINE, DEFAULT_MD_PARAGRAPH_EMPTY_LINE);
		store.setDefault(MD_PARAGRAPH_INDENT, DEFAULT_MD_PARAGRAPH_INDENT);
		store.setDefault(MD_LIST_EMPTY_LINE, DEFAULT_MD_LIST_EMPTY_LINE);

		store.setDefault(SHOW_LINE, DEFAULT_SHOW_LINE);
		store.setDefault(HEADERS_MONO_FONT, DEFAULT_HEADERS_MONO_FONT);
		store.setDefault(BACKGROUND_MODE, DEFAULT_BACKGRAUND_MODE);
		store.setDefault(DND_ENABLE, DEFAULT_DND_ENABLE);
		store.setDefault(WRAP_ID_ENABLE, DEFAULT_WRAP_ID_ENABLE);
		store.setDefault(NEW_STR_FOR_TEXT_BOX, DEFAULT_NEW_STR_FOR_TEXT_BOX);
		store.setDefault(LINE_BETWEEN_ENTRIES, DEFAULT_LINE_BETWEEN_ENTRIES);
		store.setDefault(WRAP_FIELDS, DEFAULT_WRAP_FIELDS);
		store.setDefault(NOT_SHOW_EMPTY_FIELDS, DEFAULT_NOT_SHOW_EMPTY_FIELDS);
		store.setDefault(UNDO_REDO_MODE, DEFAULT_UNDO_REDO_MODE);
		store.setDefault(OPEN_LINK_FOLDER_IN_SECTION, DEFAULT_OPEN_LINK_FOLDER_IN_SECTION);
		store.setDefault(CSV_TABLE_WIDTH_BY_CONTENT, DEFAULT_CSV_COLUMN_WIDTH_BY_CONTENT);
		store.setDefault(EDIT_DESC_IN_TABLE, DEFAULT_EDIT_DESC_IN_TABLE);
		store.setDefault(EDIT_FILE_IN_TABLE, DEFAULT_EDIT_FILE_IN_TABLE);
		store.setDefault(FIXED_MODE_FOR_DISABLE_OBJS, DEFAULT_FIXED_MODE_FOR_DISABLE_OBJS);
		store.setDefault(RENAME_DISABLE_FILE, DEFAULT_RENAME_DISABLE_FILE);
		store.setDefault(RENAME_DISABLE_FOLDER, DEFAULT_RENAME_DISABLE_FOLDER);
	}
	
	//===========================
	// start preferences
	
	public void setPreferences(){
		IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
		Display display = Display.getDefault();
		fMarginTop = store.getInt(MARGIN_TOP);
		fMarginBottom = store.getInt(MARGIN_BOTTOM);
		fMarginLeft = store.getInt(MARGIN_LEFT);
		fMarginRight = store.getInt(MARGIN_RIGHT);
		
		RGB lineRgb = new RGB(store.getInt(LINE_RGB_1), store.getInt(LINE_RGB_2), store.getInt(LINE_RGB_3));
		fLineColor = new Color(display, lineRgb);
		RGB table1Rgb = new RGB(store.getInt(TABLE_1_RRB_1), store.getInt(TABLE_1_RRB_2), store.getInt(TABLE_1_RRB_3));
		fTableColor1 = new Color(display, table1Rgb);
		RGB table2Rgb = new RGB(store.getInt(TABLE_2_RRB_1), store.getInt(TABLE_2_RRB_2), store.getInt(TABLE_2_RRB_3));
		fTableColor2 = new Color(display, table2Rgb);
		RGB selectRgb = new RGB(store.getInt(SELECT_RRB_1), store.getInt(SELECT_RRB_2), store.getInt(SELECT_RRB_3));
		fSelectColor = new Color(display, selectRgb);	
		RGB disableRgb = new RGB(store.getInt(DISABLE_RRB_1), store.getInt(DISABLE_RRB_2), store.getInt(DISABLE_RRB_3));
		fDisableColor = new Color(display, disableRgb);
			
		fShowLines = store.getBoolean(SHOW_LINE);
		fBackgroundMode = store.getInt(BACKGROUND_MODE);
		fHeadersMonoFont = store.getBoolean(HEADERS_MONO_FONT);
		fDndEnable = store.getBoolean(DND_ENABLE);
		fWrapIDEnable = store.getBoolean(WRAP_ID_ENABLE);
		fNewStrForTextbox = store.getBoolean(NEW_STR_FOR_TEXT_BOX);
		fLineBetweenEntries = store.getBoolean(LINE_BETWEEN_ENTRIES);
		fWrapField = store.getBoolean(WRAP_FIELDS);
		fNotShowEmptyFields = store.getBoolean(NOT_SHOW_EMPTY_FIELDS);
		fUndoRedoMode = store.getBoolean(UNDO_REDO_MODE);
		fOpenLinkFolderSection = store.getBoolean(OPEN_LINK_FOLDER_IN_SECTION);
		fCsvColumnWidthByContent = store.getBoolean(CSV_TABLE_WIDTH_BY_CONTENT);		
		fEditDescInTable = store.getBoolean(EDIT_DESC_IN_TABLE);
		fEditFileInTable = store.getBoolean(EDIT_FILE_IN_TABLE);	
		fFixedModeForDisableObjs = store.getBoolean(FIXED_MODE_FOR_DISABLE_OBJS);
		fRenameDisableFile = store.getBoolean(RENAME_DISABLE_FILE);
		fRenameDisableFolder = store.getBoolean(RENAME_DISABLE_FOLDER);

		fMDFontStyles = store.getBoolean(MD_FONT_STYLES);
		fMDParagraphEmptyLine = store.getBoolean(MD_PARAGRAPH_EMPTY_LINE);
		fMDListEmptyLine = store.getBoolean(MD_LIST_EMPTY_LINE);
		fMDParagraphIndentEnable = store.getBoolean(MD_PARAGRAPH_INDENT_ENABLE);
		fMDParagraphIndent = store.getInt(MD_PARAGRAPH_INDENT);		
			
		updateIdFontSize(store.getInt(ID_FONT_SIZE));
		updatePresentationFontSize(store.getInt(PRESENTATION_FONT_SIZE));
		updateCommentFontSize(store.getInt(COMMENT_FONT_SIZE));
	}
	
	//===========================
	// dispose preferences
	
	private void disposeResource(Resource resource) {
		if (resource != null && !resource.isDisposed()) {
			resource.dispose();
		}
	}
	
	public void disposeResources() {
		disposeIDFonts();
		disposePresentationFonts();
		disposeCommentFonts();
		disposeColors();
	}
	
	private void disposeIDFonts() {
		disposeResource(fIdFont);
		disposeResource(fBoldIdFont);
	}
		
	private void disposePresentationFonts() {
		disposeResource(fPresentationFont);		
		disposeResource(fBoldPresentationFont);
		disposeResource(fBoldMDPresentationFont);
		disposeResource(fItalicPresentationFont);
		disposeResource(fBoldItalicPresentationFont);
	}
	
	private void disposeCommentFonts() {
		disposeResource(fCommentFont);
	}
	
	private void disposeColors() {
		disposeResource(fLineColor);
		disposeResource(fTableColor1);
		disposeResource(fTableColor2);
		disposeResource(fSelectColor); 
		disposeResource(fDisableColor);
		disposeResource(fMDCodeFG);
	}
	
	//===========================
	// update preferences
	
	public void updateMarginTop(int newValue){
		if (fMarginTop != newValue) {
			fMarginTop = newValue ;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MARGIN_TOP, newValue);
		}
	}
	
	public void updateMarginBottom(int newValue){
		if (fMarginBottom != newValue) {
			fMarginBottom = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MARGIN_BOTTOM, newValue);
		}
	}
	
	public void updateMarginLeft(int newValue){
		if (fMarginLeft != newValue) {
			fMarginLeft = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MARGIN_LEFT, newValue);
		}
	}
	
	public void updateMarginRight(int newValue){
		if (fMarginRight != newValue) {
			fMarginRight = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MARGIN_RIGHT, newValue);
		}
	}
	
	public void updateLineColor(RGB newRgb){
		if (!fLineColor.getRGB().equals(newRgb)){
			fLineColor = new Color(Display.getDefault(), newRgb);
			IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
			store.setValue(LINE_RGB_1, newRgb.red);
			store.setValue(LINE_RGB_2, newRgb.green);
			store.setValue(LINE_RGB_3, newRgb.blue);
		}
	}
	
	public void updateTable1Color(RGB newRgb){
		if (!fTableColor1.getRGB().equals(newRgb)){
			fTableColor1 = new Color(Display.getDefault(), newRgb);
			IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
			store.setValue(TABLE_1_RRB_1, newRgb.red);
			store.setValue(TABLE_1_RRB_2, newRgb.green);
			store.setValue(TABLE_1_RRB_3, newRgb.blue);
		}
	}
	
	public void updateTable2Color(RGB newRgb){
		if (!fTableColor2.getRGB().equals(newRgb)){
			fTableColor2 = new Color(Display.getDefault(), newRgb);
			IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
			store.setValue(TABLE_2_RRB_1, newRgb.red);
			store.setValue(TABLE_2_RRB_2, newRgb.green);
			store.setValue(TABLE_2_RRB_3, newRgb.blue);
		}
	}
	
	public void updateSelectColor(RGB newRgb){
		if (!fSelectColor.getRGB().equals(newRgb)){
			fSelectColor = new Color(Display.getDefault(), newRgb);
			IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
			store.setValue(SELECT_RRB_1, newRgb.red);
			store.setValue(SELECT_RRB_2, newRgb.green);
			store.setValue(SELECT_RRB_3, newRgb.blue);
		}
	}
	
	public void updateDisableColor(RGB newRgb) {		
		if (!fDisableColor.getRGB().equals(newRgb)){
			fDisableColor = new Color(Display.getDefault(), newRgb);
			IPreferenceStore store = ReqUIPlugin.getDefault().getPreferenceStore();
			store.setValue(DISABLE_RRB_1, newRgb.red);
			store.setValue(DISABLE_RRB_2, newRgb.green);
			store.setValue(DISABLE_RRB_3, newRgb.blue);
		}
	}
	
	public void updateIdFontSize(int newSize){
		if (fIdFontSize != newSize){
			//disposeIDFonts();
			fIdFontSize = newSize;
			fIdFont = FontManager.getMonoFont(fIdFontSize);
			fBoldIdFont = FontManager.getBoldMonoFont(fIdFontSize);
			fIdFontDimension = FontDimension.createFontSize(fIdFont);
			fIdBoldFontDimension = FontDimension.createFontSize(fBoldIdFont);		
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(ID_FONT_SIZE, fIdFontSize);
		}
	}
	
	public void updatePresentationFontSize(int newSize){
		if (fPresentationFontSize != newSize){
			//disposePresentationFonts();
			fPresentationFontSize = newSize;
			fPresentationFont = FontManager.getMonoFont(fPresentationFontSize);
			fPresentationFontDimension = FontDimension.createFontSize(fPresentationFont);
			fBoldPresentationFont = FontManager.getBoldMonoFont(fPresentationFontSize + 1);
			fBoldPresentationFontDimension = FontDimension.createFontSize(fBoldPresentationFont);
			fBoldMDPresentationFont = FontManager.getBoldMonoFont(fPresentationFontSize);						
			fItalicPresentationFont = FontManager.getItalicMonoFont(fPresentationFontSize);		
			fItalicPresentationFontDimension = FontDimension.createFontSize(fItalicPresentationFont);
			fBoldItalicPresentationFont = FontManager.getBoldItalicMonoFont(fPresentationFontSize);
			fBoldMdPresentationStyle = new TextStyle(fBoldMDPresentationFont, null, null);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(PRESENTATION_FONT_SIZE, fPresentationFontSize);
		}
	}
	
	public void updateCommentFontSize(int newSize){
		if (fCommentFontSize != newSize){
			fCommentFontSize = newSize;
			fCommentFont = FontManager.getMonoFont(fCommentFontSize);
			fCommentFontDimension = FontDimension.createFontSize(fCommentFont);
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(COMMENT_FONT_SIZE, fCommentFontSize);

		}
	}
	
	public void updateShowLines(boolean newValue){
		if (fShowLines != newValue){
			fShowLines = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(SHOW_LINE, fShowLines);
		}
	}
	
	public void updateBackgroundMode(int newValue){
		if (fBackgroundMode != newValue){
			fBackgroundMode = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(BACKGROUND_MODE, fBackgroundMode);
		}
	}
	
	public void updateMDFontStylesMode(boolean newValue){
		if (fMDFontStyles != newValue){
			fMDFontStyles = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MD_FONT_STYLES, fMDFontStyles);
		}
	}
	
	public void updateMDParagraphEmptyLine(boolean newValue){
		if (fMDParagraphEmptyLine != newValue){
			fMDParagraphEmptyLine = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MD_PARAGRAPH_EMPTY_LINE, fMDParagraphEmptyLine);
		}
	}
	
	public void updateMDListEmptyLine(boolean newValue){
		if (fMDListEmptyLine != newValue){
			fMDListEmptyLine = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MD_LIST_EMPTY_LINE, fMDListEmptyLine);
		}
	}
	
	public void updateMDParagraphIndentEnable(boolean newValue){
		if (fMDParagraphIndentEnable != newValue){
			fMDParagraphIndentEnable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MD_PARAGRAPH_INDENT_ENABLE, fMDParagraphIndentEnable);
		}
	}
		
	public void updateMDParagraphIntent(int newValue){
		if (fMDParagraphIndent != newValue){
			fMDParagraphIndent = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(MD_PARAGRAPH_INDENT, fMDParagraphIndent);
		}
	}
	
	public void updateHeadersMonoFont(boolean newValue){
		if (fHeadersMonoFont != newValue){
			fHeadersMonoFont = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(HEADERS_MONO_FONT, fHeadersMonoFont);
		}
	}
	
	public void updateDndEnable(boolean newValue){
		if (fDndEnable != newValue){
			fDndEnable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(DND_ENABLE, fDndEnable);
		}
	}
	
	public void updateWrapIdEnabled(boolean newValue){
		if (fWrapIDEnable != newValue){
			fWrapIDEnable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(WRAP_ID_ENABLE, fWrapIDEnable);
		}
	}
	
	public void updateNewStrForTextbox(boolean newValue){
		if (fNewStrForTextbox != newValue){
			fNewStrForTextbox = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(NEW_STR_FOR_TEXT_BOX, fNewStrForTextbox);
		}
	}
	
	public void updateLineBetweenEntries(boolean newValue) {
		if (fLineBetweenEntries != newValue) {
			fLineBetweenEntries = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(LINE_BETWEEN_ENTRIES, fLineBetweenEntries);
		}		
	}
	
	public void updateWrapField(boolean newValue) {
		if (fWrapField != newValue) {
			fWrapField = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(WRAP_FIELDS, fWrapField);
		}
	}
	
	public void updateNotShowEmptyFields(boolean newValue) {
		if (fNotShowEmptyFields != newValue) {
			fNotShowEmptyFields = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(NOT_SHOW_EMPTY_FIELDS, fNotShowEmptyFields);
		}
	}
	
	public void updateUndoRedoMode(boolean newValue) {
		if (fUndoRedoMode != newValue) {
			fUndoRedoMode = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(UNDO_REDO_MODE, fUndoRedoMode);
		}
	}
	
	public void updateOpenLinkFolderSection(boolean newValue){
		if (fOpenLinkFolderSection != newValue){
			fOpenLinkFolderSection = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(OPEN_LINK_FOLDER_IN_SECTION, fOpenLinkFolderSection);
		}
	}
	
	public void updateCsvColumnWidthByContent(boolean newValue){
		if (fCsvColumnWidthByContent != newValue){
			fCsvColumnWidthByContent = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(CSV_TABLE_WIDTH_BY_CONTENT, fCsvColumnWidthByContent);
		}
	}
	
	public void updateEditDescInTable(boolean newValue) {
		if (fEditDescInTable != newValue) {
			fEditDescInTable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(EDIT_DESC_IN_TABLE, fEditDescInTable);
		}
	}
	
	public void updateEditFileInTable(boolean newValue) {
		if (fEditFileInTable != newValue) {
			fEditFileInTable = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(EDIT_FILE_IN_TABLE, fEditFileInTable);
		}
	}
	
	public void updateFixedModeForDisableObjs(boolean newValue) {
		if (fFixedModeForDisableObjs != newValue) {
			fFixedModeForDisableObjs = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(FIXED_MODE_FOR_DISABLE_OBJS, fFixedModeForDisableObjs);
		}		
	}
	
	public void updateRenameDisableFile(boolean newValue) {
		if (fRenameDisableFile != newValue) {
			fRenameDisableFile = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(RENAME_DISABLE_FILE, fRenameDisableFile);
		}
	}
	
	public void updateRenameDisableFolder(boolean newValue) {
		if (fRenameDisableFolder != newValue) {
			fRenameDisableFolder = newValue;
			ReqUIPlugin.getDefault().getPreferenceStore().setValue(RENAME_DISABLE_FOLDER, fRenameDisableFolder);
		}
	}
	
	//============================
	// getters
	
	public static int marginTop(){
		return instance().fMarginTop;
	}
	
	public static int marginBottom(){
		return instance().fMarginBottom;
	}
	
	public static int marginLeft(){
		return instance().fMarginLeft;
	}
	
	public static int marginRight(){
		return instance().fMarginRight;
	}

	public static int versionMarginTop(){
		return instance().fMarginTop + VERSION_TOP_INDENT;
	}
	
	public static int versionMarginBottom(){
		return instance().fMarginBottom + VERSION_BOTTOM_INDENT;
	}
	
	public static int versionMarginLeft(){
		return instance().fMarginLeft + VERSION_LEFT_INDENT;
	}
	
	public static int versionMarginRight(){
		return instance().fMarginRight + VERSION_RIGHT_INDENT;
	}
	
	public static Color lineColor(){
		return instance().fLineColor;
	}
	
	public static boolean isShowLine(){
		return instance().fShowLines;
	}
	
	public static Color tableColor1(){
		return instance().fTableColor1;
	}
	
	public static Color tableColor2(){
		return instance().fTableColor2;
	}
	
	public static Color tableSelectionColor(){
		return instance().fSelectColor;
	}
	
	public static Color tableDisableColor() {
		return instance().fDisableColor;
	}
	
	public static int backGroundMode(){
		return instance().fBackgroundMode;
	}
	
	public static boolean isHeaderMonoFont(){
		return instance().fHeadersMonoFont;
	}
	
	public static boolean isDndEnabled(){
		return instance().fDndEnable;
	}
	
	public static boolean isWrapIdEnable(){
		return instance().fWrapIDEnable;
	}

	public static boolean isNewStrForTextbox(){
		return instance().fNewStrForTextbox;
	}
	
	public static boolean isLineBetweenEntries() {
		return instance().fLineBetweenEntries;
	}
	
	public static boolean isWrapFields() {
		return instance().fWrapField;
	}
	
	public static boolean isNotShowEmptyFields() {
		return instance().fNotShowEmptyFields;
	}
	
	public static boolean isUndoRedoMode() {
		return instance().fUndoRedoMode;
	}

	public static boolean isOpenLinkFolderSection(){
		return instance().fOpenLinkFolderSection;
	}
	
	public static boolean isCsvColumnWidthByContent(){
		return instance().fCsvColumnWidthByContent;
	}
	
	public static boolean isEditDescInTable() {
		return instance.fEditDescInTable;
	}
	
	public static boolean isEditFileInTable() {
		return instance.fEditFileInTable;
	}
	public static boolean isFixedModeForDisableObjs() {
		return instance.fFixedModeForDisableObjs;
	}
	
	public static boolean isRenameDisableFile() {
		return instance.fRenameDisableFile;
	}
	
	public static boolean isRenameDisableFolder() {
		return instance.fRenameDisableFolder;
	}
	
	public static MarkdownSettings markdownSettings(boolean mdComment) {
		TableSettings settings = instance();
		return MarkdownSettings.instance(
				settings.fMDFontStyles, 
				settings.fMDParagraphEmptyLine, 
				settings.fMDListEmptyLine, 
				settings.fMDParagraphIndentEnable,
				settings.fMDParagraphIndent,
				mdComment				
				);
	}
	
	public static boolean isMDFontStyles(){
		return instance().fMDFontStyles;
	}
	
	public static boolean isMDParagraphEmptyLine(){
		return instance().fMDParagraphEmptyLine;
	}
	
	public static boolean isMDListEmptyLine(){
		return instance().fMDListEmptyLine;
	}
	
	public static boolean isMDParagraphIndentEnable() {
		return instance().fMDParagraphIndentEnable;
	}
	
	public static Color mdCodeFGColor() {
		return instance.fMDCodeFG;
	}
	
	public static Color mdCommentFGColor() {
		return instance.fMDCommentFG;
	}
	
	public static int mdParagraphIntent(){
		return instance().fMDParagraphIndent;
	}

	public static int idFontSize(){
		return instance().fIdFontSize;
	}
	
	public static int presentationFontSize(){
		return instance().fPresentationFontSize;
	}
	
	public static int commentFontSize(){
		return instance().fCommentFontSize;
	}
	
	public static Font idFont(){
		return instance().fIdFont;
	}
	
	public static Font boldIdFont(){
		return instance().fBoldIdFont;
	}
	
	public static Font presentationFont(){
		return instance().fPresentationFont;		
	}
	
	public static Font boldPresentationFont(){
		return instance().fBoldPresentationFont;
	}
	
	public static Font boldMDPresentationFont(){
		return instance().fBoldMDPresentationFont;
	}
		
	public static Font italicPresentationFont(){
		return instance().fItalicPresentationFont;
	}
	
	public static Font boldItalicPresentationFont(){
		return instance().fBoldItalicPresentationFont;
	}
		
	public static Font commentFont(){
		return instance().fCommentFont;
	}
	
	public static FontDimension presentationFontDimension(){
		return instance().fPresentationFontDimension;
	}
	
	public static FontDimension italicPresentationFontDimension(){
		return instance().fItalicPresentationFontDimension;
	}
	
	public static FontDimension commentFontDimension(){
		return instance().fCommentFontDimension;
	}
	
	public static FontDimension idFontDimension(){
		return instance().fIdFontDimension;
	}
	
	public static FontDimension boldIdFontDimenstion(){
		return instance().fIdBoldFontDimension;
	}
	
	public static FontDimension boldPresentationFontDimension(){
		return instance().fBoldPresentationFontDimension;
	}

	public static TextStyle boldMdPresentationStyle() {
		return instance().fBoldMdPresentationStyle;
	}
}
