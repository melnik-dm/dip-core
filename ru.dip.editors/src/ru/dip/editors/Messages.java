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
package ru.dip.editors;

import org.eclipse.osgi.util.NLS;

import ru.dip.core.DipCorePlugin;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ru.dip.editors.messages"; //$NON-NLS-1$
	private static final String RU_BUNDLE_NAME = "ru.dip.editors.ru_messages"; //$NON-NLS-1$

	public static String AddCommentAction_ActionTitle;
	public static String AddCommentAction_CommentLabel;
	public static String AddCommentAction_DialogName;
	public static String AutoCorrectAction_ID;
	public static String AutoCorrectAction_Name;
	public static String AutoTextTransferAction_ID;
	public static String AutoTextTransferAction_Name;
	public static String BoldAction_ID;
	public static String BoldAction_Name;
	public static String CodeAction_ID;
	public static String CodeAction_Name;
	public static String CommentAction_ID;
	public static String CommentAction_Name;
	public static String CommentManager_CommentLabel;
	public static String CommentManager_DeleteActionName;
	public static String CommentManager_DeleteCommentConfirm;
	public static String CommentManager_DeleteCommentDialogName;
	public static String CommentManager_EditActionName;
	public static String CommentManager_EditCommentDialogName;
	public static String CsvMultiEditor_0;
	public static String CsvMultiEditor_RawTextPageName;
	public static String CsvTablePage_CreateHtmlImageErrorMessage;
	public static String CsvTablePage_TablePageName;
	public static String DipEditor_AlreadyExistsErrorTitle;
	public static String DipEditor_DnfoExtension;
	public static String DipEditor_GlossExtension;
	public static String DipEditor_NotDipProjectMessage;
	public static String DipEditor_OpenDipErrorTitle;
	public static String DipEditor_OpenDIPErrorTitle;
	public static String DipEditor_ProjectExtension;
	public static String DipEditor_SchemaFolder;
	public static String DipView_2;
	public static String DipView_DefaultActionName;
	public static String DipView_ID;
	public static String DipView_ViewSizeActionName;
	public static String DipView_ZoomMinusActionName;
	public static String DipView_ZoomPlusActionName;
	public static String DotEditor_ID;
	public static String DotEditorContributor_OpenDipViewActionName;
	public static String EditViewPart_ID;
	public static String EditViewPart_Name;
	public static String EditViewPart_QuestionSave;
	public static String EditViewPart_SaveDialogTitle;
	public static String HelpFormDialog_CheckLabel;
	public static String HelpFormDialog_DropListLabel;
	public static String HelpFormDialog_FormHelpTitle;
	public static String HelpFormDialog_PerhapsVlauesLabel;
	public static String HelpFormDialog_RadioLabel;
	public static String HelpFormDialog_TextFieldLabel;
	public static String HelpReportDialog_1;
	public static String HelpReportDialog_2;
	public static String HelpReportDialog_Title;
	public static String IncorrectLinksView_HideCorrecReferencesAction;
	public static String IncorrectLinksView_IdColumn;
	public static String IncorrectLinksView_LinkColumn;
	public static String IncorrectLinksView_TextColumn;
	public static String ItalicAction_ID;
	public static String ItalicAction_Name;
	public static String LinkAction_ID;
	public static String LinkAction_Name;
	public static String MarkerListAction_ID;
	public static String MarkerListAction_Name;
	public static String MDEditor_BoldTypeID;
	public static String MDEditor_BundleForConstructedKeys;
	public static String MDEditor_FormatActionID;
	public static String MDEditor_FormatActionName;
	public static String MDEditor_ID;
	public static String MDEditor_ItalicTypeID;
	public static String MDEditorContributor_FormatActionID;
	public static String MDEditorContributor_FormatActionName;
	public static String MDEditorContributor_HighligtEnableActionID;
	public static String MDEditorContributor_OpenRenderActionName;
	public static String MDEditorContributor_OpenRenderViewActionID;
	public static String MDEditorContributor_ShowCommentsActionTitle;
	public static String MDEditorContributor_SyntaxHighlightActionName;
	public static String NumberListAction_ID;
	public static String NumberListAction_Name;
	public static String ParagraphAction_ID;
	public static String ParagraphAction_Name;
	public static String ReportEditor_ContentPageTitle;
	public static String ReportEditor_ReportPageTitle;
	public static String ReportEditor_RulesPageTitle;
	public static String ReportPage_1;
	public static String ReportPage_ReportLabel;
	public static String ReqEditorActionBarContributor_RedoEnabledProperty;
	public static String ReqEditorActionBarContributor_UndoEnabledProperty;
	public static String DipFolderPropertiesComposite_0;
	public static String DipFolderPropertiesComposite_ApplyButton;
	public static String DipFolderPropertiesComposite_DescriptionLabel;
	public static String DipFolderPropertiesComposite_NumerationButton;
	public static String FormatBlockCodeAction_ActionName;
	public static String FormsEditor_CanNotFindSchema;
	public static String FormsEditor_ID;
	public static String FormsEditor_IsNotDipRpoject;
	public static String FormsEditor_OpenErrorTitle;
	public static String FormsEditor_RawTextPageTitle;
	public static String FormsEditor_GuiPageTitle;
	public static String FormsEditorActionBarContributor_NextTextFieldActionName;
	public static String FormsEditorActionBarContributor_PreviousTextFieldActionName;
	public static String FormsEditorActionBarContributor_UpdateTextboxActionName;
	public static String SpellCheckView_CpellCheckActionToolTip;
	public static String SpellCheckView_ErrorColumnName;
	public static String SpellCheckView_ID;
	public static String SpellCheckView_IDColumnName;
	public static String TextFieldControl_AddToGlossaryActionName;
	public static String TextFieldControl_CopyActionID;
	public static String TextFieldControl_CopyActionName;
	public static String TextFieldControl_CutActionID;
	public static String TextFieldControl_CutActionName;
	public static String TextFieldControl_DeleteActionTitle;
	public static String TextFieldControl_EditToGlossaryActionName;
	public static String TextFieldControl_FileLabel;
	public static String TextFieldControl_PasteActionID;
	public static String TextFieldControl_PasteActionName;
	public static String TextFieldControl_ProjectLabel;
	public static String TextFieldControl_RedoActionName;
	public static String TextFieldControl_SaveActionTitle;
	public static String TextFieldControl_SearchTextActionName;
	public static String TextFieldControl_TextSearchFileActionID;
	public static String TextFieldControl_TextSearchFileActionName;
	public static String TextFieldControl_TextSearchProjectActionID;
	public static String TextFieldControl_TextSearchProjectActionName;
	public static String TextFieldControl_TextSearchWorkingSetActionID;
	public static String TextFieldControl_TextSearchWorkspaceActionID;
	public static String TextFieldControl_TextSearchWorspaceActionName;
	public static String TextFieldControl_TextSearxhWorkingSetActionName;
	public static String TextFieldControl_UndoActionName;
	public static String TextFieldControl_WorkingSetLabel;
	public static String TextFieldControl_WorkspaceLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		if (DipCorePlugin.getLanguage() == 0) {
			NLS.initializeMessages(RU_BUNDLE_NAME, Messages.class);
		} else {
			NLS.initializeMessages(BUNDLE_NAME, Messages.class);
		}
	}

	private Messages() {
	}
}
