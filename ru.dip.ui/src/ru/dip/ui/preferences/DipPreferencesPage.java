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

import static ru.dip.core.utilities.ui.swt.FontDimension.*;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.ui.BrowseComposite;

public class DipPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private static final String[] LANGUAGES =  {Messages.DipPreferencesPage_Russian, Messages.DipPreferencesPage_English};
	
	// indents entries
	private Text fUpText;
	private Text fDownText;
	private Text fLeftText;
	private Text fRightText;
	
	// background modes
	private Button fFolderBackModeButton;
	private Button fFileBackModeButton;
	private Button fFolderItemBackModeButton;
	private Button fFoldersFilesModeButton;
	private Button fNotBackgroundButton;
	
	// colors entries
	private ColorSelector fTableColor1;
	private ColorSelector fTableColor2;
	private ColorSelector fTableSelect;
	private ColorSelector fTableDisable;
	private ColorSelector fLineColor;
	// font size	
	private Text fIdFontSizeText;
	private Text fPresentationSizeText;
	private Text fCommentSizeText;
	private Combo fFontCombo;

	//markdown entries
	private Button fMDFonStylesButton;
	private Button fMDParagraphEmptyLine;
	private Button fMDListEmptyLine;
	private Button fMDParagraphIndent;
	private Text fMDParagraphIntentSize;
	
	// forms entries
	private Button fWrapField;
	private Button fNotShowEmptyFields;
	private Button fLineBetweenEntries;
	private Button fUndoRedoModeButton;
	private Button fNewStringTextBox;
	
	// disable entries
	private Button fFixedModeForDisableButton;
	private Button fRenameDisableFileButton;
	private Button fRenameDisableFolderButton;

	// different entries
	private Button fShowLineButton;
	//private Button fDndActiveButton;  // Перетаскивание (не реализовано)
	private Button fWrapIdButton;
	private Button fOpenFolderLinkBox;
	private Button fCSVTableWidthContent;
	private Button fGitUpdateButton;
	private Button fGitSubmoduleRecurseButton;
	private Button fEditDescInTableButton;
	private Button fEditFileInTableButton;
	private Button fCompareEditorShowDesc;
	private Button fProjectExplorerShowDesc;
	private BrowseComposite fDiaBrowse;
	private Combo fLanguageCombo;
	private Button fDisableReservation;
	private Button fDisablePreprocessing;
	
	private ValidateListener fValidateListener = new ValidateListener();
	
	public DipPreferencesPage() {
	}

	public DipPreferencesPage(String title) {
		super(title);
	}

	public DipPreferencesPage(String title, ImageDescriptor image) {
		super(title, image);
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}

	//===========================
	// create content
	
	@Override
	protected Control createContents(Composite p) {
		Composite parent = new Composite(p, SWT.BORDER);
		parent.setLayout(new GridLayout());
			
		GridData gdParent = new GridData(GridData.FILL_BOTH);
		gdParent.heightHint = 600;
		//gdParent.widthHint = 500;
		gdParent.grabExcessVerticalSpace = true;
		parent.setLayoutData(gdParent);
				
		ScrolledComposite scrol = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrol.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite composite = new Composite(scrol , SWT.BORDER);
		composite.setLayout(new GridLayout());
		createMarginsComposite(composite);		
		new Label(composite, SWT.NONE);
		createTableColorsComposite(composite);	
		new Label(composite, SWT.NONE);
		createFontSizeComposite(composite);
		createFontCompositeComposite(composite);
		new Label(composite, SWT.NONE);
		createMarkDownComposite(composite);
		new Label(composite, SWT.NONE);
		createFormPreferencesComposite(composite);
		new Label(composite, SWT.NONE);
		createDisablePreferencesComposite(composite);
		new Label(composite, SWT.NONE);		
		createOtherPreferencesComposite(composite);	
		createLanguageComposite(composite);
		setValues();
		
		scrol.setExpandVertical(true);
		scrol.setExpandHorizontal(true);
		composite.pack();
		Point size = composite.getSize();
		scrol.setMinSize(size);
		scrol.setContent(composite);
		return composite;
	}
	
	private void createMarginsComposite(Composite parent){
		Label margins = new Label(parent, SWT.NONE);
		margins.setText(Messages.DipPreferencesPage_Intents);
		margins.setFont(FontManager.boldFont);
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		Label upLabel = new Label(composite, SWT.NONE);
		upLabel.setText(Messages.DipPreferencesPage_Up);
		fUpText = new Text(composite, SWT.RIGHT | SWT.BORDER);
		fUpText.addModifyListener(fValidateListener);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fUpText.setLayoutData(gd);
		
		Label downLabel = new Label(composite, SWT.NONE);
		downLabel.setText(Messages.DipPreferencesPage_Down);
		fDownText = new Text(composite, SWT.RIGHT  | SWT.BORDER);
		fDownText.addModifyListener(fValidateListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fDownText.setLayoutData(gd);
		
		Label leftLabel = new Label(composite, SWT.NONE);
		leftLabel.setText(Messages.DipPreferencesPage_Left);
		fLeftText = new Text(composite, SWT.RIGHT  | SWT.BORDER);
		fLeftText.addModifyListener(fValidateListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fLeftText.setLayoutData(gd);
		
		Label rightLabel = new Label(composite, SWT.NONE);
		rightLabel.setText(Messages.DipPreferencesPage_Rights);
		fRightText = new Text(composite, SWT.RIGHT  | SWT.BORDER);
		fRightText.addModifyListener(fValidateListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fRightText.setLayoutData(gd);
	}
	
	private void createTableColorsComposite(Composite parent){
		Label margins = new Label(parent, SWT.NONE);
		margins.setText(Messages.DipPreferencesPage_Colors);
		margins.setFont(FontManager.boldFont);		
		Composite radioComposite = new Composite(parent, SWT.NONE);
		radioComposite.setLayout(new GridLayout(3, false));
		fFileBackModeButton = new Button(radioComposite, SWT.RADIO);
		fFileBackModeButton.setText(Messages.DipPreferencesPage_LineAlternation);
		fFolderBackModeButton = new Button(radioComposite, SWT.RADIO);
		fFolderBackModeButton.setText(Messages.DipPreferencesPage_FolderAlternation);
		fFolderItemBackModeButton = new Button(radioComposite, SWT.RADIO);
		fFolderItemBackModeButton.setText(Messages.DipPreferencesPage_LintAlternationStartWithFolder);
		fFoldersFilesModeButton = new Button(radioComposite, SWT.RADIO);
		fFoldersFilesModeButton.setText(Messages.DipPreferencesPage_Folder_Line);		
		fNotBackgroundButton = new Button(radioComposite, SWT.RADIO);
		fNotBackgroundButton.setText(Messages.DipPreferencesPage_NotUse);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));		
		ControlFactory.label(composite, Messages.DipPreferencesPage_EvenLines);
		fTableColor1 = new ColorSelector(composite);
		ControlFactory.label(composite, Messages.DipPreferencesPage_OddLines);
		fTableColor2 = new ColorSelector(composite);
		
		ControlFactory.label(composite, Messages.DipPreferencesPage_SelectLine);
		fTableSelect = new ColorSelector(composite);
		ControlFactory.label(composite, Messages.DipPreferencesPage_DisableObjs);
		fTableDisable = new ColorSelector(composite);
		ControlFactory.label(composite, Messages.DipPreferencesPage_TableLine);
		fLineColor = new ColorSelector(composite);
	}
	
	private void createFontSizeComposite(Composite parent){
		Label fontSizeLabel = new Label(parent, SWT.NONE);
		fontSizeLabel.setText(Messages.DipPreferencesPage_FontSize);
		fontSizeLabel.setFont(FontManager.boldFont);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));		
		Label idLabel = new Label(composite, SWT.NONE);
		idLabel.setText(Messages.DipPreferencesPage_ID);
		fIdFontSizeText = new Text(composite, SWT.RIGHT | SWT.BORDER);
		fIdFontSizeText.addModifyListener(fValidateListener);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fIdFontSizeText.setLayoutData(gd);
		Label presentationLabel = new Label(composite, SWT.NONE);
		presentationLabel.setText(Messages.DipPreferencesPage_Content);
		fPresentationSizeText = new Text(composite, SWT.RIGHT | SWT.BORDER);
		fPresentationSizeText.addModifyListener(fValidateListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fPresentationSizeText.setLayoutData(gd);
		Label commentLabel = new Label(composite, SWT.NONE);
		commentLabel.setText(Messages.DipPreferencesPage_Comment);
		fCommentSizeText = new Text(composite,  SWT.RIGHT | SWT.BORDER);
		fCommentSizeText.addModifyListener(fValidateListener);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCommentSizeText.setLayoutData(gd);
	}
	
	private void createFontCompositeComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label fontLabel = new Label(composite, SWT.NONE);
		fontLabel.setText(Messages.DipPreferencesPage_FontLabel);
		
		if (ResourcesUtilities.isWindows) {
			fFontCombo = new Combo(composite, SWT.BORDER);
			fFontCombo.setItems(FontManager.FONT_NAMES);
		}
	}
	
	private void createMarkDownComposite(Composite parent){
		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.DipPreferencesPage_Markdown);
		title.setFont(FontManager.boldFont);		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		fMDFonStylesButton = new Button(composite, SWT.CHECK);
		fMDFonStylesButton.setText(Messages.DipPreferencesPage_ShowStyles);
		fMDParagraphEmptyLine = new Button(composite, SWT.CHECK);
		fMDParagraphEmptyLine.setText(Messages.DipPreferencesPage_PastLineAterParagraph);
		fMDParagraphEmptyLine.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fMDParagraphEmptyLine.getSelection();
				fMDListEmptyLine.setEnabled(selection);				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

		fMDListEmptyLine = new Button(composite, SWT.CHECK);
		fMDListEmptyLine.setText(Messages.DipPreferencesPage_NotPasteLineInListItems);
		
		fMDParagraphIndent = new Button(composite, SWT.CHECK);
		fMDParagraphIndent.setText(Messages.DipPreferencesPage_AddIntetForParagraph);
		fMDParagraphIndent.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = fMDParagraphIndent.getSelection();
				fMDParagraphIntentSize.setEnabled(selection);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		Composite intentSizeComposite = new Composite(composite, SWT.NONE);
		intentSizeComposite.setLayout(new GridLayout(2, false));
		intentSizeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label intentSize = new Label(intentSizeComposite, SWT.NONE);
		intentSize.setText(Messages.DipPreferencesPage_Intent);
		fMDParagraphIntentSize = new Text(intentSizeComposite, SWT.RIGHT | SWT.BORDER);
		fMDParagraphIntentSize.addModifyListener(fValidateListener);
		GridData gdIntent = new GridData(GridData.FILL_HORIZONTAL);
		fMDParagraphIntentSize.setLayoutData(gdIntent);
	}
		
	private void createFormPreferencesComposite(Composite parent) {
		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.DipPreferencesPage_Forms);
		title.setFont(FontManager.boldFont);		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		fNewStringTextBox = new Button(composite, SWT.CHECK);
		fNewStringTextBox.setText(Messages.DipPreferencesPage_TextFieldOnNewLine);
		fLineBetweenEntries = new Button(composite, SWT.CHECK);
		fLineBetweenEntries.setText(Messages.DipPreferencesPage_EmptyLineBetweenFields);
		fWrapField = new Button(composite, SWT.CHECK);
		fWrapField.setText(Messages.DipPreferencesPage_NotTranserFieldOnSeparateLines);
		fNotShowEmptyFields = new Button(composite, SWT.CHECK);
		fNotShowEmptyFields.setText(Messages.DipPreferencesPage_NotShowEmptyFields);
		fUndoRedoModeButton = new Button(composite, SWT.CHECK);
		fUndoRedoModeButton.setText(Messages.DipPreferencesPage_UndoOnAllForm);
	}
	
	private void createDisablePreferencesComposite(Composite parent){
		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.DipPreferencesPage_DisableObjsLabel);
		title.setFont(FontManager.boldFont);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		fFixedModeForDisableButton = new Button(composite, SWT.CHECK);
		fFixedModeForDisableButton.setText(Messages.DipPreferencesPage_FixedModeForDissableObjs);
		fRenameDisableFileButton = new Button(composite, SWT.CHECK);
		fRenameDisableFileButton.setText(Messages.DipPreferencesPage_AddMarkerForDisableFileButton);
		fRenameDisableFolderButton = new Button(composite, SWT.CHECK);
		fRenameDisableFolderButton.setText(Messages.DipPreferencesPage_AddMarkerForDisableFolderButton);
	}
	
	private void createOtherPreferencesComposite(Composite parent){
		Label title = new Label(parent, SWT.NONE);
		title.setText(Messages.DipPreferencesPage_Different);
		title.setFont(FontManager.boldFont);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		fShowLineButton = new Button(composite, SWT.CHECK);
		fShowLineButton.setText(Messages.DipPreferencesPage_HorizontalLines);
		
		//fMonoFontTitleButton = new Button(composite, SWT.CHECK);
		//fMonoFontTitleButton.setText("Моно-шрифт для заголовков");	
		
		//fDndActiveButton = new Button(composite, SWT.CHECK);
		//fDndActiveButton.setText(Messages.DipPreferencesPage_EnableDragNDrop);	
		
		fWrapIdButton = new Button(composite, SWT.CHECK);
		fWrapIdButton.setText(Messages.DipPreferencesPage_TransferIdOnWords);
		
		fOpenFolderLinkBox = new Button(composite, SWT.CHECK);
		fOpenFolderLinkBox.setText(Messages.DipPreferencesPage_OpenFolderLinkInSection);
		
		fCSVTableWidthContent = new Button(composite, SWT.CHECK);
		fCSVTableWidthContent.setText(Messages.DipPreferencesPage_SetCSVColumnWithOnContent);
		
		fGitUpdateButton = new Button(composite, SWT.CHECK);
		fGitUpdateButton.setText(Messages.DipPreferencesPage_UpdateContentAfterGitOperations);	
		
		fGitSubmoduleRecurseButton = new Button(composite, SWT.CHECK);
		fGitSubmoduleRecurseButton.setText(Messages.DipPreferencesPage_UpdateSubmodulesAfterGitOperations);
		
		fEditDescInTableButton = new Button(composite, SWT.CHECK);
		fEditDescInTableButton.setText(Messages.DipPreferencesPage_EditDescInTable);	
		
		fEditFileInTableButton = new Button(composite, SWT.CHECK);
		fEditFileInTableButton.setText(Messages.DipPreferencesPage_EditFileInTable);
		
		fDisableReservation = new Button(composite, SWT.CHECK);
		fDisableReservation.setText(Messages.DipPreferencesPage_DisableReservation);
		
		if (ResourcesUtilities.isWindows) {
			fDiaBrowse = new BrowseComposite(composite, Messages.DipPreferencesPage_DiaPath);
		}	
		
		fDisablePreprocessing = new Button(composite, SWT.CHECK);
		fDisablePreprocessing.setText(Messages.DipPreferencesPage_ExportPreprocessingLabel);
		
		fCompareEditorShowDesc = new Button(composite, SWT.CHECK);
		fCompareEditorShowDesc.setText(Messages.DipPreferencesPage_ShowDescriptionInCompareEditor);
		
		fProjectExplorerShowDesc = new Button(composite, SWT.CHECK);
		fProjectExplorerShowDesc.setText(Messages.DipPreferencesPage_ShowDescriptionInProjectExplorer);
	}
	
	private void createLanguageComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		Label languageLabel = new Label(composite, SWT.NONE);
		languageLabel.setText(Messages.DipPreferencesPage_Language);
		fLanguageCombo = new Combo(composite, SWT.BORDER);
		fLanguageCombo.setItems(LANGUAGES);
	}
	
	//==========================
	// init values
	
	private void setValues(){
		// margins
		fUpText.setText(String.valueOf(TableSettings.marginTop()));		
		fDownText.setText(String.valueOf(TableSettings.marginBottom()));
		fLeftText.setText(String.valueOf(TableSettings.marginLeft()));
		fRightText.setText(String.valueOf(TableSettings.marginRight()));
		// colors
		fTableColor1.setColorValue(TableSettings.tableColor1().getRGB());
		fTableColor2.setColorValue(TableSettings.tableColor2().getRGB());
		fTableSelect.setColorValue(TableSettings.tableSelectionColor().getRGB());
		fTableDisable.setColorValue(TableSettings.tableDisableColor().getRGB());
		fLineColor.setColorValue(TableSettings.lineColor().getRGB());
		//fMonoFontTitleButton.setSelection(TableSettings.isHeaderMonoFont());
		int backGroundMode = TableSettings.backGroundMode();
		if (backGroundMode == TableSettings.FILES_BACKGROUND_MODE){
			fFileBackModeButton.setSelection(true);
			fFolderBackModeButton.setSelection(false);
			fFolderItemBackModeButton.setSelection(false);
			fFoldersFilesModeButton.setSelection(false);
			fNotBackgroundButton.setSelection(false);
		} else if (backGroundMode == TableSettings.FOLDERS_BACKGROUND_MODE){
			fFileBackModeButton.setSelection(false);
			fFolderBackModeButton.setSelection(true);
			fFolderItemBackModeButton.setSelection(false);
			fFoldersFilesModeButton.setSelection(false);
			fNotBackgroundButton.setSelection(false);
		} else if (backGroundMode == TableSettings.FOLDER_ITEMS_MODE){ 
			fFileBackModeButton.setSelection(false);
			fFolderBackModeButton.setSelection(false);
			fFolderItemBackModeButton.setSelection(true);
			fFoldersFilesModeButton.setSelection(false);
			fNotBackgroundButton.setSelection(false);			
		} else if (backGroundMode == TableSettings.FOLDERS_FILES_MODE){
			fFileBackModeButton.setSelection(false);
			fFolderBackModeButton.setSelection(false);
			fFolderItemBackModeButton.setSelection(false);
			fFoldersFilesModeButton.setSelection(true);
			fNotBackgroundButton.setSelection(false);		
		}else if (backGroundMode == TableSettings.NOT_BACKGROUND_MODE){
			fFileBackModeButton.setSelection(false);
			fFolderBackModeButton.setSelection(false);
			fFolderItemBackModeButton.setSelection(false);
			fFoldersFilesModeButton.setSelection(false);
			fNotBackgroundButton.setSelection(true);
		}		
		// font size
		fIdFontSizeText.setText(String.valueOf(TableSettings.idFontSize()));
		fPresentationSizeText.setText(String.valueOf(TableSettings.presentationFontSize()));
		fCommentSizeText.setText(String.valueOf(TableSettings.commentFontSize()));
		if (ResourcesUtilities.isWindows) {
			fFontCombo.setText(DipCorePlugin.getMonoFont());
		}
		// markdown
		fMDFonStylesButton.setSelection(TableSettings.isMDFontStyles());
		fMDParagraphIntentSize.setText(String.valueOf(TableSettings.mdParagraphIntent()));
		
		fMDParagraphEmptyLine.setSelection(TableSettings.isMDParagraphEmptyLine());
		fMDListEmptyLine.setSelection(!TableSettings.isMDListEmptyLine());		
		if (!TableSettings.isMDParagraphEmptyLine()) {
			fMDListEmptyLine.setEnabled(false);
		}		
		fMDParagraphIndent.setSelection(TableSettings.isMDParagraphIndentEnable());		
		// disable
		fFixedModeForDisableButton.setSelection(TableSettings.isFixedModeForDisableObjs());
		fRenameDisableFileButton.setSelection(TableSettings.isRenameDisableFile());
		fRenameDisableFolderButton.setSelection(TableSettings.isRenameDisableFolder());
		
		// other
		fShowLineButton.setSelection(TableSettings.isShowLine());		
		//fDndActiveButton.setSelection(TableSettings.isDndEnabled());
		fWrapIdButton.setSelection(TableSettings.isWrapIdEnable());
		fNewStringTextBox.setSelection(TableSettings.isNewStrForTextbox());
		fLineBetweenEntries.setSelection(TableSettings.isLineBetweenEntries());
		fWrapField.setSelection(TableSettings.isWrapFields());
		fNotShowEmptyFields.setSelection(TableSettings.isNotShowEmptyFields());
		fUndoRedoModeButton.setSelection(TableSettings.isUndoRedoMode());
		fOpenFolderLinkBox.setSelection(TableSettings.isOpenLinkFolderSection());
		fCSVTableWidthContent.setSelection(TableSettings.isCsvColumnWidthByContent());
		fGitUpdateButton.setSelection(DipCorePlugin.getGitUpdate());
		fGitSubmoduleRecurseButton.setSelection(DipCorePlugin.isGitSubmoduleRecurse());
		fEditDescInTableButton.setSelection(TableSettings.isEditDescInTable());		
		fEditFileInTableButton.setSelection(TableSettings.isEditFileInTable());	
		fDisableReservation.setSelection(DipCorePlugin.isDisableReservation());
		fDisablePreprocessing.setSelection(DipCorePlugin.isDisablePreprocessing());
		fCompareEditorShowDesc.setSelection(OtherPreferences.isShowDescInCompareEditor());
		fProjectExplorerShowDesc.setSelection(OtherPreferences.isShowDescInProjectExplorer());
		if (ResourcesUtilities.isWindows) {
			fDiaBrowse.setValue(DipCorePlugin.getDiaPath());
		}
		fLanguageCombo.select(DipCorePlugin.getLanguage());
	}
	
	//===========================
	// validate
	
	private class ValidateListener implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			validate();
		}
		
	}
	
	private void validate(){
		try {
			Integer.parseInt(fUpText.getText().trim());
			Integer.parseInt(fDownText.getText().trim());
			Integer.parseInt(fLeftText.getText().trim());
			Integer.parseInt(fRightText.getText().trim());
			if (!fMDParagraphIntentSize.getText().isEmpty()){
				Integer.parseInt(fMDParagraphIntentSize.getText());
			}
			int fontSize = Integer.parseInt(fIdFontSizeText.getText().trim());
			if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE){
				setValid(false);
				return;
			}
			fontSize = Integer.parseInt(fPresentationSizeText.getText().trim());
			if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE){
				setValid(false);
				return;
			}
			fontSize = Integer.parseInt(fCommentSizeText.getText().trim());
			if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE){
				setValid(false);
				return;
			}			
			setValid(true);
		} catch (NumberFormatException e){
			setValid(false);
		}
	}
	
	@Override
	protected void performDefaults() {
		// margins
		fUpText.setText(String.valueOf(TableSettings.DEFAULT_MARGIN_TOP));
		fDownText.setText(String.valueOf(TableSettings.DEFAULT_MARGIN_BOTTOM));
		fLeftText.setText(String.valueOf(TableSettings.DEFAULT_MARGIN_LEFT));
		fRightText.setText(String.valueOf(TableSettings.DEFAULT_MARGIN_RIGHT));
		// colors
		fTableColor1.setColorValue(TableSettings.DEFAULT_TABLE_1_RGB);
		fTableColor2.setColorValue(TableSettings.DEFAULT_TABLE_2_RGB);
		fTableSelect.setColorValue(TableSettings.DEFAULT_SELECT_RGB);
		fTableDisable.setColorValue(TableSettings.DEFAULT_DISABLE_RGB);
		fLineColor.setColorValue(TableSettings.DEFAULT_LINE_RGB);
		//fMonoFontTitleButton.setSelection(TableSettings.DEFAULT_HEADERS_MONO_FONT);
		fFileBackModeButton.setSelection(false);
		fFolderBackModeButton.setSelection(false);
		fFolderItemBackModeButton.setSelection(false);
		fFoldersFilesModeButton.setSelection(true);
		fNotBackgroundButton.setSelection(false);		
		// fon size
		fIdFontSizeText.setText(String.valueOf(TableSettings.DEFAULT_ID_FONT_SIZE));
		fPresentationSizeText.setText(String.valueOf(TableSettings.DEFAULT_PRESENTATION_FONT_SIZE));
		fCommentSizeText.setText(String.valueOf(TableSettings.DEFAULT_COMMENT_FONT_SIZE));
		if (ResourcesUtilities.isWindows) {
			fFontCombo.setText(DipCorePlugin.DEFAULT_MONO_FONT);
		}
		// markdown
		fMDFonStylesButton.setSelection(TableSettings.DEFAULT_MD_FONT_STYLES);
		fMDParagraphEmptyLine.setSelection(TableSettings.DEFAULT_MD_PARAGRAPH_EMPTY_LINE);
		fMDListEmptyLine.setSelection(!TableSettings.DEFAULT_MD_LIST_EMPTY_LINE);
		fMDParagraphIndent.setSelection(TableSettings.DEFAULT_MD_PARAGRAPH_INDENT_ENABLE);
		fMDParagraphIntentSize.setText(String.valueOf(TableSettings.DEFAULT_MD_PARAGRAPH_INDENT));
		fMDParagraphIntentSize.setEnabled(TableSettings.DEFAULT_MD_PARAGRAPH_INDENT_ENABLE);		
		// disable	
		fFixedModeForDisableButton.setSelection(TableSettings.DEFAULT_FIXED_MODE_FOR_DISABLE_OBJS);
		fRenameDisableFileButton.setSelection(TableSettings.DEFAULT_RENAME_DISABLE_FILE);
		fRenameDisableFolderButton.setSelection(TableSettings.DEFAULT_RENAME_DISABLE_FOLDER);		
		// other	
		fShowLineButton.setSelection(TableSettings.DEFAULT_SHOW_LINE);
		//fDndActiveButton.setSelection(TableSettings.DEFAULT_DND_ENABLE);
		fWrapIdButton.setSelection(TableSettings.DEFAULT_WRAP_ID_ENABLE);
		fNewStringTextBox.setSelection(TableSettings.DEFAULT_NEW_STR_FOR_TEXT_BOX);
		fLineBetweenEntries.setSelection(TableSettings.DEFAULT_LINE_BETWEEN_ENTRIES);
		fWrapField.setSelection(TableSettings.DEFAULT_WRAP_FIELDS);
		fNotShowEmptyFields.setSelection(TableSettings.DEFAULT_NOT_SHOW_EMPTY_FIELDS);
		fUndoRedoModeButton.setSelection(TableSettings.DEFAULT_UNDO_REDO_MODE);
		fOpenFolderLinkBox.setSelection(TableSettings.DEFAULT_OPEN_LINK_FOLDER_IN_SECTION);
		fCSVTableWidthContent.setSelection(TableSettings.DEFAULT_CSV_COLUMN_WIDTH_BY_CONTENT);
		fGitUpdateButton.setSelection(true);
		fGitSubmoduleRecurseButton.setSelection(true);
		fEditDescInTableButton.setSelection(TableSettings.DEFAULT_EDIT_DESC_IN_TABLE);
		fEditFileInTableButton.setSelection(TableSettings.DEFAULT_EDIT_FILE_IN_TABLE);
		fDisableReservation.setSelection(true);
		fDisablePreprocessing.setSelection(false);
		fCompareEditorShowDesc.setSelection(OtherPreferences.COMPARE_EDITOR_SHOW_DESC_DEFAULT);
		fProjectExplorerShowDesc.setSelection(OtherPreferences.PROJECT_EXPLORER_SHOW_DESC_DEFAULT);
		if (ResourcesUtilities.isWindows) {
			fDiaBrowse.setValue(DipCorePlugin.DEFAULT_DIA_PATH);
		}
		fLanguageCombo.select(DipCorePlugin.DEFAULT_LANGUAGE);		
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		TableSettings settings = TableSettings.instance();		
		// margins
		int marginTop = Integer.parseInt(fUpText.getText().trim());
		settings.updateMarginTop(marginTop);
		int marginDown = Integer.parseInt(fDownText.getText().trim());
		settings.updateMarginBottom(marginDown);
		int marginLeft = Integer.parseInt(fLeftText.getText().trim());
		settings.updateMarginLeft(marginLeft);
		int marginRight = Integer.parseInt(fRightText.getText().trim());		
		settings.updateMarginRight(marginRight);
		// colors
		settings.updateLineColor(fLineColor.getColorValue());
		settings.updateTable1Color(fTableColor1.getColorValue());
		settings.updateTable2Color(fTableColor2.getColorValue());
		settings.updateSelectColor(fTableSelect.getColorValue());
		settings.updateDisableColor(fTableDisable.getColorValue());
		int backMode = TableSettings.FILES_BACKGROUND_MODE;;
		if (fFolderBackModeButton.getSelection()){
			backMode = TableSettings.FOLDERS_BACKGROUND_MODE;
		} else if (fFolderItemBackModeButton.getSelection()){
			backMode = TableSettings.FOLDER_ITEMS_MODE;
		} else if (fFoldersFilesModeButton.getSelection()){ 
			backMode = TableSettings.FOLDERS_FILES_MODE;
		} else if (fNotBackgroundButton.getSelection()){		
			backMode = TableSettings.NOT_BACKGROUND_MODE;
		}
		settings.updateBackgroundMode(backMode);
		// font size
		int idSize = Integer.parseInt(fIdFontSizeText.getText().trim());
		settings.updateIdFontSize(idSize);
		int presentationSize = Integer.parseInt(fPresentationSizeText.getText().trim());
		settings.updatePresentationFontSize(presentationSize);
		int commentSize = Integer.parseInt(fCommentSizeText.getText().trim());
		settings.updateCommentFontSize(commentSize);
		if (ResourcesUtilities.isWindows) {
			DipCorePlugin.setMonoFont(fFontCombo.getText());
		}
		// mardown
		settings.updateMDFontStylesMode(fMDFonStylesButton.getSelection());
		settings.updateMDParagraphEmptyLine(fMDParagraphEmptyLine.getSelection());
		settings.updateMDListEmptyLine(!fMDListEmptyLine.getSelection());
		settings.updateMDParagraphIndentEnable(fMDParagraphIndent.getSelection());
		settings.updateMDParagraphIntent(Integer.parseInt(fMDParagraphIntentSize.getText()));	
		// disable
		settings.updateFixedModeForDisableObjs(fFixedModeForDisableButton.getSelection());
		settings.updateRenameDisableFile(fRenameDisableFileButton.getSelection());
		settings.updateRenameDisableFolder(fRenameDisableFolderButton.getSelection());
		
		// other
		settings.updateShowLines(fShowLineButton.getSelection());		
		//settings.updateDndEnable(fDndActiveButton.getSelection());	
		settings.updateWrapIdEnabled(fWrapIdButton.getSelection());
		settings.updateNewStrForTextbox(fNewStringTextBox.getSelection());
		settings.updateLineBetweenEntries(fLineBetweenEntries.getSelection());
		settings.updateWrapField(fWrapField.getSelection());
		settings.updateNotShowEmptyFields(fNotShowEmptyFields.getSelection());
		settings.updateUndoRedoMode(fUndoRedoModeButton.getSelection());
		settings.updateOpenLinkFolderSection(fOpenFolderLinkBox.getSelection());
		settings.updateCsvColumnWidthByContent(fCSVTableWidthContent.getSelection());
		DipCorePlugin.setGitUpdate(fGitUpdateButton.getSelection());
		DipCorePlugin.setGitSubmoduleRecurse(fGitSubmoduleRecurseButton.getSelection());
		settings.updateEditDescInTable(fEditDescInTableButton.getSelection());
		settings.updateEditFileInTable(fEditFileInTableButton.getSelection());		
		DipCorePlugin.setDisableReservation(fDisableReservation.getSelection());
		OtherPreferences.updateCompareEditorShowDesc(fCompareEditorShowDesc.getSelection());
		OtherPreferences.updateProjectExplorerShowDesc(fProjectExplorerShowDesc.getSelection());
		
		if (ResourcesUtilities.isWindows) {
			DipCorePlugin.setDiaPath(fDiaBrowse.getValue());
		}
		DipCorePlugin.setDisablePreprocessing(fDisablePreprocessing.getSelection());
		DipCorePlugin.setLanguage(fLanguageCombo.getSelectionIndex());
		return super.performOk();
	}

}
