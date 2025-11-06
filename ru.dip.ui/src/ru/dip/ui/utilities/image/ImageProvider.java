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
package ru.dip.ui.utilities.image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ru.dip.ui.ReqUIPlugin;

public class ImageProvider {
	
	//=======================
	// Navigator (Project Explorer)	
	public static final Image SHOW_RESERVED_OBJS = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("show_reserved.png"));
	public static final Image TABLE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("table.gif"));	
	public static final Image REPORT_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("report_folder.gif"));	
	public static final Image SCHEMA_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("schema_folder.gif"));
	public static final Image SCHEMA_FILE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("schema_file.gif"));	
	public static final Image GLOSS_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("glossfolder.png"));	
	public static final Image GLOSS_FIELD = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("info.png"));
	public static final Image SERVICE_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("close_obj.png"));
	public static final Image TOC = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("list.png"));
	public static final Image LINK_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("folder_link.gif"));
	public static final Image VAR_CONTAINER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("variables_container.gif"));
	public static final Image VARIABLE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("variable.gif"));
	
	//==========================
	// Overlay
	
	public static final Image ERROR_OVR = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("error_overlay.png"));
	
	public static final ImageDescriptor ERRPR_OVR_DESCRIPTOR = ImageDescriptor.createFromImage(ERROR_OVR);

	public static final ImageDescriptor LOCK_OVR_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"lock.gif");
	public static final ImageDescriptor LINK_OVR_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"link_ovr.png");
	
	
	//==========================
	// Document(Section) Label Provider
	
	public static final Image EMPTY_IMAGE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("empty1.png"));
	public static final Image GO_INTO = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("go_into.gif"));
	public static final Image IMAGE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("image48.png"));	
	public static final Image COMMENT = new Image(Display.getDefault(),	
			ImageProvider.class.getResourceAsStream("comment.png"));	
	public static final ImageDescriptor COMMENT_DESCRIPTOR = ImageDescriptor.createFromImage(COMMENT);	

	public static final Image UML = IMAGE;
	
	public static final Image BOOKMARK_1 = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("bookmark_1.png"));
	public static final Image BOOKMARK_2 = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("bookmark_2.png"));
	public static final Image BOOKMARK_3 = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("bookmark_3.png"));
	public static final Image BOOKMARK_4 = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("bookmark_4.png"));
	public static final Image BOOKMARK_5 = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("bookmark_5.png"));
	
	public static final Image[] BOOKMARKS = {BOOKMARK_1, BOOKMARK_2, BOOKMARK_3, BOOKMARK_4, BOOKMARK_5};
	
	public static final ImageDescriptor[] BOOKMARK_DESCS = {
			ImageDescriptor.createFromImage(BOOKMARK_1),
			ImageDescriptor.createFromImage(BOOKMARK_2),
			ImageDescriptor.createFromImage(BOOKMARK_3),
			ImageDescriptor.createFromImage(BOOKMARK_4),
			ImageDescriptor.createFromImage(BOOKMARK_5)
	};
	
	//===========================================
	//  Document(Section) button icons
	
	public static final Image FILE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("file.gif"));
	public static final Image FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("folder.gif"));
	public static final ImageDescriptor FOLDER_DESCRIPTOR = ImageDescriptor.createFromImage(FOLDER);
	public static final Image NEW_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("newfolder.gif"));
	public static final Image UP = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("up.gif"));
	public static final ImageDescriptor UP_DESCRIPTOR = ImageDescriptor.createFromImage(UP);
	public static final Image DOWN = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("down.gif"));
	public static final ImageDescriptor DOWN_DESCRIPTOR = ImageDescriptor.createFromImage(DOWN);
	public static final Image INTO_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("into_folder.gif"));	
	public static final Image DELETE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("delete.png"));
	public static final ImageDescriptor DELETE_DESCRIPTOR = ImageDescriptor.createFromImage(DELETE);
	public static final Image FIRST_COLUMN = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("first_column.gif"));
	public static final Image LIST = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("list.gif"));
	public static final Image EMPTY = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("empty.png"));
	public static final Image EXPAND_ALL = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("expand_all.gif"));
	public static final Image COLLAPSE_ALL = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("collapse_all.gif"));
	public static final Image NUMERATION = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("numeration.gif"));
	public static final Image FIX_CONTENT = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("fix_content.gif"));
	public static final Image COMMENT_ICON = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("comment.gif"));
	public static final ImageDescriptor ADD_COMMENT_DESC = ImageDescriptor.createFromImage(COMMENT_ICON);
	public static final Image EXPORT_ICON = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("export.gif"));
	public static final Image SHOW_BY_ID_ICON = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("show_by_id.gif"));
	public static final Image HIGHLIGHT_GLOSS = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("higlight_gloss.gif"));
	public static final Image OPEN_BY_ID_ICON = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("open_by_id.png"));
	public static final Image IMAGE_VIEW = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("image.gif"));
	public static final Image TABLE_VIEW = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("dip_table.gif"));
	public static final Image HELP = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("help.png"));
	public static final Image FORM_PREFERENCES = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("preference_importer.gif"));
	public static final Image SPELL_CHECK = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("spellcheck.gif"));
	public static final ImageDescriptor SPELL_CHECK_DESCRIPTOR = ImageDescriptor.createFromImage(SPELL_CHECK);
	public static final Image HIDE_DISABLE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("hide.png"));
	public static final Image UPDATE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("update.png"));
	public static final Image FILTER_REMOVE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("remove_filter.png"));
	public static final Image LOAD_FROM_REPORT = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("load_from_report.png")); 
	public static final Image SAVE_TO_REPORT = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("save_to_report.png")); 
	public static final Image DIFF = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("diff.png"));
	
	//===================================
	// FormEditor -  context menu
	public static final ImageDescriptor SAVE = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/table/save.png").get();	
	public static final ImageDescriptor MATCH_HEIGHT = ImageDescriptor.createFromFile(ImageProvider.class,
			"matchheight.gif");
	
	
	//===================================
	// Trace dialog
	
	public static final ImageDescriptor ADD_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"add.gif");
	public static final Image ADD = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("add.gif"));
	public static final Image ADD_SEVERAL = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("add_several.png"));
	public static final Image COPY = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("copy.gif"));
	public static final Image FIND = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("find.gif"));
	public static final Image REPO = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("repo.gif"));
	public static final Image FILTER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("filter.png"));
	public static final ImageDescriptor FILTER_DESC = ImageDescriptor.createFromImage(FILTER);
	
	public static final Image CLEAR_FILTER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("clear.png"));
	
	//===============================
	// new wizards
	
	public static final ImageDescriptor NEW_FILE_WIZ = ResourceLocator.
			imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/wizards/newfile_wiz.png").get();		
	public static final ImageDescriptor NEW_FOLDER_WIZ = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/wizards/newfolder_wiz.png").get();		
	public static final ImageDescriptor NEW_PROJECT_WIZ = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/wizards/newprj_wiz.png").get();		

	//==============================
	// apply_numbering dialog
	
	public static final Image FORWARD = new Image(Display.getDefault(), 
			ImageProvider.class.getResourceAsStream("forward_nav.png"));
	
	//==============================
	// report editor
	
	public static final Image EXPAND = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("expand.gif"));
	public static final Image COLLAPSE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("collapse.gif"));
	
	//========================
	// dialogs
	
	public static final Image WARNING = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("warning.png"));
	
	//========================
	// edit view
	
	public static final ImageDescriptor EDIT_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"edit.png");
	public static final Image EDIT = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("edit.png"));
	
	//========================
	// render view
	
	public static final ImageDescriptor RENDER = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/view/render_view.png").get();		

	//========================
	// incorrect links view
	
	public static final Image ERROR = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("error.png"));
	
	//=======================
	// markdown editor
	
	public static final ImageDescriptor BOLD_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/md/style_bold.gif").get();	
	public static final ImageDescriptor DISABLE_BOLD_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/md/disable_style_bold.png").get();	
	public static final ImageDescriptor ITALIC_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/md/style_italic.gif").get();
	public static final ImageDescriptor DISABLE_ITALIC_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/md/disable_style_italic.png").get();
	public static final ImageDescriptor SYNTAX_HIGHLIGHT_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/md/highlight.png").get();
	public static final ImageDescriptor LINK_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"link.png");
	public static final ImageDescriptor COMMENT_EDIT_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"comment_edit.png");
	public static final ImageDescriptor CODE_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"code.gif");
	public static final ImageDescriptor NUMBER_LIST_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"numeration_list.gif");
	public static final ImageDescriptor MARKER_LIST_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"marker_list.gif");
	public static final ImageDescriptor PARAGRAPH_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"paragraph.png");
	public static final ImageDescriptor HIDE_LINKS_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"hide_links.png");
	public static final ImageDescriptor AUTO_TEXT_WRAPPING = ImageDescriptor.createFromFile(ImageProvider.class,
			"wordwrap.png");
	public static final ImageDescriptor AUTO_CORRECT_DESCRIPTOR = ImageDescriptor.createFromFile(ImageProvider.class,
			"autocorrect.gif");
	public static final ImageDescriptor FORMAT_BLOCK_CODE = ImageDescriptor.createFromFile(ImageProvider.class,
			"bin_obj.gif");
	
	//=========================
	// dip image
	
	public static final ImageDescriptor ALLIGNMENT_DESCRIPTOR = ResourceLocator.
			imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/view/allignment.gif").get();
	public static final ImageDescriptor HORIZONTAl_DESCRIPTOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/view/horizontal_orientation.gif").get();
	
	//=========================
	// dip toc
	
	public static final ImageDescriptor LINK_WITH_EDITOR = ResourceLocator
			.imageDescriptorFromBundle(ReqUIPlugin.PLUGIN_ID, "icons/toolbar/link_to_editor.png").get();

	//=========================
	// dip glossary
	
	public static final Image FIND_UNSED = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("find_unsed.gif"));
	public static final Image ABBREVIATION = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("abbreviation.png"));
	
	//==========================
	// variables
	
	public static final Image DUPLICATE = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("duplicate.gif"));
	
	//==========================
	// tdbp 
	
	public static final Image PROJECT_FOLDER = new Image(Display.getDefault(),
			ImageProvider.class.getResourceAsStream("prj_folder.png"));

}
