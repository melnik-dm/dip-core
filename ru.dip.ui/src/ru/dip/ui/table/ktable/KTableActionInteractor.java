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
package ru.dip.ui.table.ktable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Menu;

import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.ui.Messages;
import ru.dip.ui.handlers.CopyIdHandler;
import ru.dip.ui.table.ktable.actions.ActivateReqNumerationAction;
import ru.dip.ui.table.ktable.actions.ApplyAutoNumberingAction;
import ru.dip.ui.table.ktable.actions.DeleteCommentAction;
import ru.dip.ui.table.ktable.actions.DisableAction;
import ru.dip.ui.table.ktable.actions.DocumentAction;
import ru.dip.ui.table.ktable.actions.EditCommentAction;
import ru.dip.ui.table.ktable.actions.EditDescriptionAction;
import ru.dip.ui.table.ktable.actions.MarkAction;
import ru.dip.ui.table.ktable.actions.OpenAction;
import ru.dip.ui.table.ktable.actions.OpenUnityMdAction;
import ru.dip.ui.table.ktable.actions.OrientationHorizontalAction;
import ru.dip.ui.table.ktable.actions.OrientationVerticalAction;
import ru.dip.ui.table.ktable.actions.PageBreakAction;
import ru.dip.ui.table.ktable.actions.PageBreakAction.PageBreakMode;
import ru.dip.ui.table.ktable.actions.RemoveDescriptionAction;
import ru.dip.ui.table.ktable.actions.SetAutoNumberingAction;
import ru.dip.ui.table.ktable.actions.SetLinkAction;
import ru.dip.ui.table.ktable.actions.SortedAction;
import ru.dip.ui.table.ktable.actions.create.NewFileAction;
import ru.dip.ui.table.ktable.actions.create.NewFileAfterAction;
import ru.dip.ui.table.ktable.actions.create.NewFileBeforeAction;
import ru.dip.ui.table.ktable.actions.create.NewFolderAction;
import ru.dip.ui.table.ktable.actions.create.NewFolderAfterAction;
import ru.dip.ui.table.ktable.actions.create.NewFolderBeforeAction;
import ru.dip.ui.table.ktable.actions.edit.CopyAction;
import ru.dip.ui.table.ktable.actions.edit.DeleteAction;
import ru.dip.ui.table.ktable.actions.edit.DownAction;
import ru.dip.ui.table.ktable.actions.edit.IntoFolderAction;
import ru.dip.ui.table.ktable.actions.edit.PasteAction;
import ru.dip.ui.table.ktable.actions.edit.RenameAction;
import ru.dip.ui.table.ktable.actions.edit.UpAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFileAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFileAfterAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFileBeforeAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFolderAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFolderAfterAction;
import ru.dip.ui.table.ktable.actions.imprt.ImportFolderBeforeAction;
import ru.dip.ui.table.ktable.actions.include.IncludeFolderAction;
import ru.dip.ui.table.ktable.actions.include.IncludeFolderAfterAction;
import ru.dip.ui.table.ktable.actions.include.IncludeFolderBeforeAction;
import ru.dip.ui.table.ktable.actions.md.MdExtractAction;
import ru.dip.ui.table.ktable.actions.md.MdJoinAction;
import ru.dip.ui.table.ktable.actions.md.MdSplitAction;

public class KTableActionInteractor {
	
	private KTableComposite fTable;
	
	// actions 
	private NewFileBeforeAction fNewFileBeforeAction;
	private NewFileAfterAction fNewFileAfterAction;
	private NewFolderBeforeAction fNewFolderBeforeAction;
	private NewFolderAfterAction fNewFolderAfterAction;	
	private EditDescriptionAction fEditDescriptionAction;
	private RemoveDescriptionAction fRemoveDescriptionAction;
	private CopyAction fCopyAction;
	private PasteAction fPasteAction;
	private RenameAction fRenameAction;
	private UpAction fUpAction;
	private DownAction fDownAction;
	private IntoFolderAction fIntoFolderAction;
	private DeleteAction fDeleteAction;
	private OpenAction fOpenAction;
	private NewFileAction fNewFileAction;
	private NewFolderAction fNewFolderAction;
	private OrientationHorizontalAction fHorizontalOrientationAction;
	private OrientationVerticalAction fVerticalOrientationAction;
	private PageBreakAction fPageBreakEachFolderAction;
	private PageBreakAction fPageBreakLastFolderAction;
	private PageBreakAction fPageBreakAuto;	
	private ActivateReqNumerationAction fActivateNumerationAction;
	private SetAutoNumberingAction fSetAutoNumberingAction;
	private ApplyAutoNumberingAction fApplyAutoNumberingAction;
	private EditCommentAction fEditCommentAction;
	private DeleteCommentAction fDeleteCommentAction;
	private SortedAction fSortedAction;
	private ImportFileAction fImportFileAction;
	private ImportFileBeforeAction fImportBeforeAction;
	private ImportFileAfterAction fImportAfterAction;
	private ImportFolderAction fImportFolderAction;
	private ImportFolderBeforeAction fImportFolderBeforeAction;
	private ImportFolderAfterAction fImportFolderAfterAction;
	private IncludeFolderAction fIncludeFolderAction;
	private IncludeFolderBeforeAction fIncludeBeforeAction;
	private IncludeFolderAfterAction fIncludeAfterAction;
	private DisableAction fDisableAction;
	private MdExtractAction fMdExtractAction;
	private MdSplitAction fMdSplitAction;
	private MdJoinAction fMdJoinAction;
	private OpenUnityMdAction fOpenUnityMdAction;

	private MarkAction[] fMarkActions = new MarkAction[IMarkable.MARKS_SIZE];
	
	private MenuManager fOrientationMenuManager;
	private MenuManager fPageBreakMenuManager;
	
	public KTableActionInteractor(KTableComposite table) {
		fTable = table;
	}

	public void addContextMenu() {
		MenuManager popupMenuManager = new MenuManager();
		createContextMenuActions();
		popupMenuManager.setRemoveAllWhenShown(true);
		IMenuListener listener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mng) {
				// для ошибочного include				
				if (fTable.selector().isOneSelected()){
					IDipDocumentElement selectedDipDocElement = fTable.selector().getSelectedOneDipDocElement();
					if (selectedDipDocElement instanceof IncludeFolder) {
						createIncludeErrorMenu(mng);
						return;
					}					 
				}
				
				createMenu(mng);
				deactivateAllActions();
				if (fTable.selector().isEmpty()){
					enableNullSelection();			
				} else if (fTable.selector().isOneSelected()) {														
					IDipDocumentElement selectedDipDocElement = fTable.selector().getSelectedOneDipDocElement();
					enableOneSelection(selectedDipDocElement);					
				} else {
					enableSeveralSelection();
				}		
			}		
	
			private IMenuManager createMenu(IMenuManager mng){
				addNewActionsToMenu(mng);
				addImportActionsToMenu(mng);
				addIncludeActionsToMenu(mng);
				
				mng.add(new Separator());
				mng.add(fOpenAction);
				mng.add(CopyIdHandler.getRelationPathItem());
				mng.add(CopyIdHandler.getFuldIdItem());
				mng.add(CopyIdHandler.getRevisionIdItem());
								
				mng.add(new Separator());
				mng.add(fEditCommentAction);
				mng.add(fDeleteCommentAction);
				
				mng.add(new Separator());
				mng.add(fEditDescriptionAction);
				mng.add(fRemoveDescriptionAction);
				
				mng.add(new Separator());
				mng.add(fUpAction);
				mng.add(fDownAction);
				mng.add(fIntoFolderAction);
				
				mng.add(new Separator());
				mng.add(fMdExtractAction);
				mng.add(fMdSplitAction);
				mng.add(fMdJoinAction);
				mng.add(fOpenUnityMdAction);
				
				mng.add(new Separator());				
				fOrientationMenuManager = new MenuManager(Messages.KTableActionInteractor_OrientationMenu);
				mng.add(fOrientationMenuManager);
				fOrientationMenuManager.add(fHorizontalOrientationAction);
				fOrientationMenuManager.add(fVerticalOrientationAction);
				
				fPageBreakMenuManager = new MenuManager(Messages.KTableActionInteractor_PageBreakMenu);		
				mng.add(fPageBreakMenuManager);
				fPageBreakMenuManager.add(fPageBreakEachFolderAction);
				fPageBreakMenuManager.add(fPageBreakLastFolderAction);
				fPageBreakMenuManager.add(fPageBreakAuto);
				
				mng.add(fActivateNumerationAction);											
				MenuManager autonumberingMenuManager = new MenuManager(Messages.KTableActionInteractor_AutoNumbering);	
				mng.add(autonumberingMenuManager);
				autonumberingMenuManager.add(fSetAutoNumberingAction);
				autonumberingMenuManager.add(fApplyAutoNumberingAction);
				mng.add(fSortedAction);
				mng.add(new Separator());
				mng.add(fDisableAction);
				
				addMarkActionsToMenu(mng);
				
				mng.add(fCopyAction);
				mng.add(fPasteAction);
				mng.add(fRenameAction);
				mng.add(fDeleteAction);		
				return mng;
			}
			
			private void addNewActionsToMenu(IMenuManager mng) {
				MenuManager newSubMenuManager = new MenuManager(Messages.KTableActionInteractor_NewMenu);	
				mng.add(newSubMenuManager);
				newSubMenuManager.add(fNewFileAction);
				newSubMenuManager.add(fNewFileBeforeAction);
				newSubMenuManager.add(fNewFileAfterAction);	
				newSubMenuManager.add(new Separator());
				newSubMenuManager.add(fNewFolderAction);
				newSubMenuManager.add(fNewFolderBeforeAction);
				newSubMenuManager.add(fNewFolderAfterAction);
			}
			
			private void addImportActionsToMenu(IMenuManager mng) {
				MenuManager importSubMenuManager = new MenuManager(Messages.KTableActionInteractor_ImportMenu);
				mng.add(importSubMenuManager);
				importSubMenuManager.add(fImportFileAction);
				importSubMenuManager.add(fImportBeforeAction);
				importSubMenuManager.add(fImportAfterAction);
				importSubMenuManager.add(fImportFolderAction);
				importSubMenuManager.add(fImportFolderBeforeAction);
				importSubMenuManager.add(fImportFolderAfterAction);
			}
			
			private void addMarkActionsToMenu(IMenuManager mng) {
				MenuManager markSubMenuManager = new MenuManager(Messages.KTableActionInteractor_MarkMenu);
				mng.add(markSubMenuManager);
				Stream.of(fMarkActions).forEach(markSubMenuManager::add); 
			}
			
			private void addIncludeActionsToMenu(IMenuManager mng) {
				MenuManager includeSubMenuManager = new MenuManager(Messages.KTableActionInteractor_IncludeMenu);
				mng.add(includeSubMenuManager);
				includeSubMenuManager.add(fIncludeFolderAction);
				includeSubMenuManager.add(fIncludeBeforeAction);
				includeSubMenuManager.add(fIncludeAfterAction);
			}
						
			private IMenuManager createIncludeErrorMenu(IMenuManager mng) {
				mng.add(new SetLinkAction(fTable));
				mng.add(fDeleteAction);
				return mng;
			}
			
			private void deactivateAllActions(){
				fActions.forEach(action -> action.setEnabled(false));
			}		

		};

		popupMenuManager.addMenuListener(listener);
		Menu menu = popupMenuManager.createContextMenu(fTable.table());
		fTable.table().setMenu(menu);
	}
			
	private List<DocumentAction> fActions = new ArrayList<>();
	
	private void enableNullSelection() {
		if (fTable.isDiffMode() && !fTable.isDinamicallyDiffMode()) {
			return;
		}	
		fActions.forEach(DocumentAction::enableNullSelection);
	}
	
	private void enableOneSelection(IDipDocumentElement dipDocElement) {
		if (fTable.isDiffMode() && !fTable.isDinamicallyDiffMode()) {
			enableForDiffMode(dipDocElement);
		} else {
			fActions.forEach(action -> action.enableOneSelection(dipDocElement));
		}
	}
	
	private void enableForDiffMode(IDipDocumentElement dipDocElement) {
		fOpenAction.enableOneSelection(dipDocElement);
	}
	
	private void enableSeveralSelection() {
		if (fTable.isDiffMode() && !fTable.isDinamicallyDiffMode()) {
			return;
		}		
		fActions.forEach(DocumentAction::enableSeveralSelection);
	}
	
	private void createContextMenuActions(){
		createNewActions();
		createImportActions();
		createIncludeActions();
		createEditActions();
		createMarkdownActions();
		crateDescriptionActions();
		createCommentActions();
		createNumerationActions();
		createMarkActions();

		fOpenAction = new OpenAction(fTable);
		fActions.add(fOpenAction);
		
		fHorizontalOrientationAction = new OrientationHorizontalAction(fTable);
		fActions.add(fHorizontalOrientationAction);
		
		fVerticalOrientationAction = new OrientationVerticalAction(fTable);
		fActions.add(fVerticalOrientationAction);
		
		fPageBreakEachFolderAction = new PageBreakAction(fTable, PageBreakMode.EACH_FOLDER);
		fActions.add(fPageBreakEachFolderAction);
		
		fPageBreakLastFolderAction = new PageBreakAction(fTable, PageBreakMode.LAST_FOLDER);
		fActions.add(fPageBreakLastFolderAction);
		
		fPageBreakAuto = new PageBreakAction(fTable, PageBreakMode.AUTO);
		fActions.add(fPageBreakAuto);				

		fDisableAction = new DisableAction(fTable);
		fActions.add(fDisableAction);
	}
	
	private void createNewActions() {
		fNewFileBeforeAction = new NewFileBeforeAction(fTable);		
		fActions.add(fNewFileBeforeAction);
		fNewFileAfterAction = new NewFileAfterAction(fTable);
		fActions.add(fNewFileAfterAction);
		fNewFolderBeforeAction = new NewFolderBeforeAction(fTable);
		fActions.add(fNewFolderBeforeAction);
		fNewFolderAfterAction = new NewFolderAfterAction(fTable);
		fActions.add(fNewFolderAfterAction);
		fNewFileAction = new NewFileAction(fTable);
		fActions.add(fNewFileAction);
		fNewFolderAction = new NewFolderAction(fTable);
		fActions.add(fNewFolderAction);
	}
	
	private void createImportActions() {
		fImportFileAction = new ImportFileAction(fTable);
		fActions.add(fImportFileAction);
		fImportBeforeAction = new ImportFileBeforeAction(fTable);
		fActions.add(fImportBeforeAction);
		fImportAfterAction = new ImportFileAfterAction(fTable);	
		fActions.add(fImportAfterAction);
		fImportFolderAction = new ImportFolderAction(fTable);
		fActions.add(fImportFolderAction);
		fImportFolderBeforeAction = new ImportFolderBeforeAction(fTable);
		fActions.add(fImportFolderBeforeAction);
		fImportFolderAfterAction = new ImportFolderAfterAction(fTable);
		fActions.add(fImportFolderAfterAction);
	}
	
	private void createIncludeActions() {
		fIncludeFolderAction = new IncludeFolderAction(fTable);
		fActions.add(fIncludeFolderAction);
		fIncludeBeforeAction = new IncludeFolderBeforeAction(fTable);
		fActions.add(fIncludeBeforeAction);
		fIncludeAfterAction = new IncludeFolderAfterAction(fTable);
		fActions.add(fIncludeAfterAction);
	}
	
	private void createEditActions() {
		fCopyAction = new CopyAction(fTable);
		fActions.add(fCopyAction);
		fPasteAction = new PasteAction(fTable);
		fActions.add(fPasteAction);
		fRenameAction = new RenameAction(fTable);
		fActions.add(fRenameAction);
		fUpAction = new UpAction(fTable);
		fActions.add(fUpAction);
		fDownAction = new DownAction(fTable);
		fActions.add(fDownAction);
		fIntoFolderAction = new IntoFolderAction(fTable);
		fActions.add(fIntoFolderAction);
		fDeleteAction = new DeleteAction(fTable);
		fActions.add(fDeleteAction);
		fSortedAction = new SortedAction(fTable);
		fActions.add(fSortedAction);
	}
	
	private void createMarkdownActions() {
		fMdExtractAction = new MdExtractAction(fTable);
		fActions.add(fMdExtractAction);
		fMdSplitAction = new MdSplitAction(fTable);
		fActions.add(fMdSplitAction);
		fMdJoinAction = new MdJoinAction(fTable);
		fActions.add(fMdJoinAction);
		fOpenUnityMdAction = new OpenUnityMdAction(fTable);
		fActions.add(fOpenUnityMdAction);	
	}
	
	private void crateDescriptionActions() {
		fEditDescriptionAction = new EditDescriptionAction(fTable);
		fActions.add(fEditDescriptionAction);
		fRemoveDescriptionAction = new RemoveDescriptionAction(fTable);
		fActions.add(fRemoveDescriptionAction);
	}
	
	private void createCommentActions() {
		fEditCommentAction = new EditCommentAction(fTable);
		fActions.add(fEditCommentAction);
		fDeleteCommentAction = new DeleteCommentAction(fTable);
		fActions.add(fDeleteCommentAction);
	}
	
	private void createNumerationActions() {
		fActivateNumerationAction = new ActivateReqNumerationAction(fTable);
		fActions.add(fActivateNumerationAction);
		fSetAutoNumberingAction = new SetAutoNumberingAction(fTable);	
		fActions.add(fSetAutoNumberingAction);
		fApplyAutoNumberingAction = new ApplyAutoNumberingAction(fTable);
		fActions.add(fApplyAutoNumberingAction);
	}
	
	private void createMarkActions() {
		for (int markNumber = 0; markNumber < IMarkable.MARKS_SIZE; markNumber++) {
			fMarkActions[markNumber] = new MarkAction(fTable, markNumber);
			fActions.add(fMarkActions[markNumber]);
		}
	}
	
	//=======================
	// getters
	
	public DeleteAction getDeleteAction() {
		return fDeleteAction;
	}
	
	public UpAction getUpAction() {
		return fUpAction;
	}
	
	public DownAction getDownAction() {
		return fDownAction;
	}

	public RenameAction getRenameAction() {
		return fRenameAction;
	}

	public IntoFolderAction getDoIntoFolderAction() {
		return fIntoFolderAction;
	}

	public NewFileAction getAddNewFileAction() {
		return fNewFileAction;
	}
	
	public NewFolderAction getAddNewFolder() {
		return fNewFolderAction;
	}

}
