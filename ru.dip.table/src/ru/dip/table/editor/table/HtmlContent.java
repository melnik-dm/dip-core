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
package ru.dip.table.editor.table;

public class HtmlContent {
	
	public static final String SAVE = "SAVE";
	public static final String INIT = "INIT";
	public static final String DIRTY = "DIRTY";

	public static final String BEFORE_SCRIPT_PATH = "<!DOCTYPE html>\n"
			+ "<html lang=\"en\">"
			+ "  <head>"
			+ "    <meta charset=\"utf-8\">"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
			+ "    <script name=\"test\" src=\"";
	
	public static final String AFTER_SCRIPT_PATH = "\" referrerpolicy=\"origin\"></script>";

	public static String scriptPath(String path) {
		StringBuilder builder = new StringBuilder();
		builder.append(BEFORE_SCRIPT_PATH);
		builder.append(path);
		builder.append(AFTER_SCRIPT_PATH);
		return builder.toString();
	}
	
	public static final String scriptINitStart = " <script>\n tinymce.init({\n"
			+ " selector: '#mytextarea',"
			+ " language: 'ru',";
	//		+ " autoresize: ON,";
	//		+ "	height: '800%',";
	
	public static final String PLUGINS = "plugins: ["
			+ "'autoresize advlist autolink lists link image charmap emoticons preview anchor',"
			+ "'searchreplace visualblocks code fullscreen',"
			+ "'insertdatetime media table paste code help wordcount',"
			+ "'save'],\n";
	
	public static final String MENU = "  menubar: 'edit insert view format table tools help',"
			+ " menu: {\n"
			+ "    edit: { title: 'Edit', items: 'undo redo | cut copy paste | selectall | searchreplace' },"
			+ "    view: { title: 'View', items: 'code | visualaid visualchars visualblocks | spellchecker | preview fullscreen' },"
			+ "    insert: { title: 'Insert', items: 'image link media template codesample inserttable | charmap emoticons hr | pagebreak nonbreaking anchor toc | insertdatetime' },\n"
			+ "    format: { title: 'Format', items: 'bold italic underline strikethrough superscript subscript codeformat | formats blockformats fontformats fontsizes align lineheight | forecolor backcolor | removeformat' },\n"
			+ "    tools: { title: 'Tools', items: 'spellchecker spellcheckerlanguage | code wordcount' },\n"
			+ "    table: { title: 'Table', items: 'inserttable | cell row column | tableprops deletetable' },\n"
			+ "    help: { title: 'Help', items: 'help' }\n"
			+ " },";
	
	public static final String LISTENERS = "  setup: function(editor) {"	
					// callBack Dirty
			+ "		editor.on('Dirty', function(e) {"
			+ "     	callBack(\"DIRTY\");"
			+ "    	});"
					// callBack INIT
			+ "	   	editor.on('init', function(e) {"
			+ "			callBack(\"INIT\");"
			+ "    	});"
					// key Listener - undo-redo
			+ "    	editor.on('keyup keypress keydown', e => {"
			+ "			if (e.type === 'keydown' && !e.shiftKey && e.ctrlKey "
			+ "				&& (e.key === 'я' || e.key === 'Я')) {"
			+ "				tinymce.activeEditor.execCommand('Undo');"
			+ "			}"
			
			+ "			if (e.type === 'keydown' && !e.shiftKey && e.ctrlKey  "
			+ "				&& (e.key === 'н' || e.key === 'Н')) {"
			+ "				tinymce.activeEditor.execCommand('Redo');"
			+ "			}"
			
			+ "			if (e.shiftKey && e.ctrlKey && (e.key === 'я' || e.key === 'Я')) {"
			+ "				tinymce.activeEditor.execCommand('Redo');"
			+ "			}"  
		
			+ "     });"  
			+ "  },";
		
	
	public static final String SAVE_CALL_BACK = "  save_onsavecallback: function () { console.log('Save');  callBack(\"SAVED\", tinymce.activeEditor.getContent());}\n});\n"; 
	
	public static final String INIT_SCRIPT = 
			scriptINitStart
			+ PLUGINS
			+ MENU
			+ LISTENERS
			+ SAVE_CALL_BACK;
	
	public static final String SAVE_FUNCTION = "function dipsave(){ tinymce.activeEditor.execCommand('mceSave'); "
			+ "callBack(\"SAVE\", tinymce.activeEditor.getContent());"
			+ " };\n";
	public static final String SET_CONTENT_FUNCTION = "function setcontent(content) { tinymce.activeEditor.setContent(content);};\n";
	public static final String UNDO_FUNCTION = "function undo11() { tinymce.activeEditor.execCommand('Undo');};\n";
	
	public static final String BODY = "</script>"		
			+ "</head>"			
			+ "<body>"
			+ "   <form method=\"post\">"
			+ "      <textarea id=\"mytextarea\">"
			+ "      </textarea>"
			+ "	  </form>"
			+ "	</body>"
			+ "</html>";
	
	public static final  String HTML_CODE = 
			SAVE_FUNCTION
			+ SET_CONTENT_FUNCTION 
			+ BODY;

}
