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
package ru.dip.ui.table.editor.toolbar;

import java.util.TreeSet;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.export.Exporter;
import ru.dip.ui.imageview.ImagesView;
import ru.dip.ui.imageview.TablesView;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.editor.toolbar.FilterToolBar.IFilterToolBarListener;
import ru.dip.ui.table.ktable.dialog.ShemaPropertiesDialog;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.table.TableModel;
import ru.dip.ui.table.table.TableSettings;
import ru.dip.ui.utilities.image.ImageProvider;

public class ButtonManager {
	
	// model
	private DipTableEditor fEditor;
	private IBindingService fBindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
	private MouseListener fMouseListener;
	// controls
	private Composite fTableComposite;
	private Composite fParent;
	private Composite fComposite;
	private ToolBar fToolBar;
	
	// table mode
	private ToolItem fFirstColumnButton;
	private ToolItem fListViewButton;
	private ToolItem fExpandAllButton;
	private ToolItem fColapseAllButton;
	private ToolItem fNumerationButton;
	private ToolItem fFixContentButton;
	private ToolItem fCheckSpellingButton;
	private ToolItem fReviewButton;
	private ToolItem fFormProperties;
	private ToolItem fHideDisableObjs;
	private ToolItem fHiglightGlossaryButton;

	// edit group
	private ToolItem fNewFileButton;
	private ToolItem fNewFolderButton;
	private ToolItem fUpButton;
	private ToolItem fDownButton;
	private ToolItem fIntoFolderButton;
	private ToolItem fDeleteButton;
	
	private ToolItem fOpenGlossaryButton;
	private ToolItem fExportButton;	
	private ToolItem fShowIDButton;
	private ToolItem fOpenIDButton;
	private ToolItem fOpenImageView;
	private ToolItem fOpenTableView;

	private NumerationSelectionListener fNumerationListener;
	private ToolItem fUpdateButton;	
	private ToolItem fDiffModeButton;
	//private ToolItem fTestButton;
	
	private IdModeDropdownSelectionListener fListenerId;

	// filter toolbar
	private FilterToolBar fFilterToolBar;
	private boolean fShowFilterGroup = false;
	
 	public ButtonManager(Composite parent, DipTableEditor editor){
 		fTableComposite = parent;
 		fParent = new Composite(parent, SWT.NONE);
 		fParent.setLayout(LayoutManager.notIndtentLayout());
 		fParent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fEditor = editor;
		createContent(fParent);		
	}
 	
	private void createContent(Composite parent){
		fComposite = new Composite(parent, SWT.NONE);				
		fComposite.setLayout(LayoutManager.notIndtentLayout());
		fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createToolbar(fComposite);
		if (fShowFilterGroup) {
			fFilterToolBar = new FilterToolBar(fEditor, fComposite);
			//createFilterToolbar(fComposite);
			fFilterToolBar.setListener(new IFilterToolBarListener() {
				
				@Override
				public void reset() {
					fEditor.kTable().resetFilter();
				}
				
				@Override
				public void apply() {
					applyFilter();
				}
			});
		}
	}
	
	private void createToolbar(Composite composite) {		
		fToolBar = new ToolBar(composite, SWT.NONE);	
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		fToolBar.setLayout(layout);
		createOneListButton(fToolBar);
		createExpandAllButton(fToolBar);
		createCollapseAllButton(fToolBar);
		createFixContentButton(fToolBar);		
		createIdModeButton(fToolBar);
		createReviewButton(fToolBar);
		createNumerationButton(fToolBar);
		createFormProperties(fToolBar);
		createCheckSpellingButton(fToolBar);
		createHideDisableObjsButton(fToolBar);
		createFilterEnableButton(fToolBar);
		Separator separator = new Separator();
		separator.fill(fToolBar, fToolBar.getItems().length);	
		createNewFileButton(fToolBar);
		createNewFolderButton(fToolBar);
		createUpButton(fToolBar);
		createDownButton(fToolBar);
		createIntoFolderButton(fToolBar);
		createDeleteButton(fToolBar);
		separator.fill(fToolBar, fToolBar.getItems().length);		
		createShowIDButton(fToolBar);
		createOpenIDButton(fToolBar);		
		separator.fill(fToolBar, fToolBar.getItems().length);	
		createGlossaryButton(fToolBar);
		createHighlightGlossaryButton(fToolBar);
		separator.fill(fToolBar, fToolBar.getItems().length);
		createImageViewButton(fToolBar);
		createTableViewButton(fToolBar);
		createExportButton(fToolBar);
		createDiffModeButton(fToolBar);
		createUpdateButton(fToolBar);
		//createTestButton();
	}
	
	/*private void createTestButton() {
		fTestButton = new ToolItem(fToolBar, SWT.PUSH);
		fTestButton.setText("TEST");
		fTestButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("SELECT");
				fEditor.kTable().test();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			
		});		
	}*/
		
	private void createUpdateButton(ToolBar toolBar){
		fUpdateButton = new ToolItem(toolBar, SWT.PUSH);
		fUpdateButton.setImage(ImageProvider.UPDATE);
		fUpdateButton.setToolTipText(Messages.ButtonManager_UpdateToolTip);
		fUpdateButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.fullUpdate();
			}
			
		});    
	}
	
	private void createDiffModeButton(ToolBar toolBar) {
		fDiffModeButton = new ToolItem(toolBar, SWT.CHECK);
		fDiffModeButton.setImage(ImageProvider.DIFF);
		fDiffModeButton.setToolTipText(Messages.ButtonManager_DiffModeButton);
		fDiffModeButton.setSelection(false);
		fDiffModeButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fDiffModeButton.getSelection();
				if (newSelection) {
					fEditor.kTable().applyDiffMode();;
				} else {
					fEditor.kTable().setDinamicallyDiffMode(false);
					fEditor.fullUpdate();
				}
				fCheckSpellingButton.setSelection(newSelection);
			}
			
		});
	
	}
	
	
	private void createIdModeButton(ToolBar toolBar){
		fFirstColumnButton = new ToolItem(toolBar, SWT.PUSH);
		fFirstColumnButton.setToolTipText(Messages.ButtonManager_IDCellTooltip);
		fFirstColumnButton.setImage(ImageProvider.FIRST_COLUMN);	
		fListenerId = new IdModeDropdownSelectionListener(fFirstColumnButton);
		fFirstColumnButton.addSelectionListener(fListenerId);
	}
	
	class IdModeDropdownSelectionListener extends SelectionAdapter {
		private Menu fMenu;		
		private MenuItem fHideItem;
		private MenuItem fFirstItem;
		private MenuItem fLastItem;
		private MenuItem fShowFormVersion;

		public IdModeDropdownSelectionListener(ToolItem dropdown) {
			fMenu = new Menu(dropdown.getParent().getShell());
			addHideIdItem();
			addFirstIdItem();
			addLastIdItem();
			Separator separator = new Separator();
			separator.fill(fMenu, 3);
			addShowFormVersionItem();
		}
		
		private void addHideIdItem() {
			fHideItem = new MenuItem(fMenu, SWT.RADIO);
			fHideItem.setText(Messages.ButtonManager_HideButton);
			fHideItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fHideItem.getSelection()) {
						tableModel().changeIdMode(DipTableModel.HIDE_ID);
					}
				}
				
			});
		}
		
		private void addFirstIdItem() {
			fFirstItem = new MenuItem(fMenu, SWT.RADIO);
			fFirstItem.setText(Messages.ButtonManager_FirstButton);
			fFirstItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fFirstItem.getSelection()) {
						tableModel().changeIdMode(DipTableModel.SHOW_ID);
					}
				}
				
			});
		}
		
		private void addLastIdItem() {
			fLastItem = new MenuItem(fMenu, SWT.RADIO);
			fLastItem.setText(Messages.ButtonManager_LastButton);
			fLastItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fLastItem.getSelection()) {
						tableModel().changeIdMode(DipTableModel.SHOW_LAST_ID);
					}					
				}

			});
		}
		
		private void addShowFormVersionItem() {
			fShowFormVersion = new MenuItem(fMenu, SWT.CHECK);
			fShowFormVersion.setText(Messages.ButtonManager_ShowFormVersionButton);
			fShowFormVersion.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().setShowVersionForm(fShowFormVersion.getSelection());
				}
			});
		}
		

		@Override
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			fMenu.setLocation(pt.x, pt.y + rect.height);
			setCurrentReviewMode();
			fMenu.setVisible(true);
		}

		private void setCurrentReviewMode() {
			if (tableModel().isHideID()) {
				fHideItem.setSelection(true);
				fFirstItem.setSelection(false);
				fLastItem.setSelection(false);
			} else if(tableModel().isShowFirstID()) {
				fFirstItem.setSelection(true);
				fHideItem.setSelection(false);
				fLastItem.setSelection(false);
			} else if (tableModel().isLastOrderID()){
				fLastItem.setSelection(true);
				fFirstItem.setSelection(false);
				fHideItem.setSelection(false);
			}
			fShowFormVersion.setSelection(fEditor.kTable().isShowFormVersion());
		}
	}
	
	private void createOneListButton(ToolBar toolBar){
		fListViewButton = new ToolItem(toolBar, SWT.CHECK);
		fListViewButton.setToolTipText(Messages.ButtonManager_FlatNestedTooltip);
		fListViewButton.setImage(ImageProvider.LIST);
		fListViewButton.setSelection(false);
		fListViewButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fListViewButton.getSelection();
				fEditor.kTable().changeOneListMode(newSelection);
				fListViewButton.setSelection(newSelection);
			}
			
		});
	}
	
	private void createFixContentButton(ToolBar toolBar){
		fFixContentButton = new ToolItem(toolBar, SWT.CHECK);
		fFixContentButton.setToolTipText(Messages.ButtonManager_FixedContentTooltip);
		fFixContentButton.setImage(ImageProvider.FIX_CONTENT);
		fFixContentButton.setSelection(false);
		fFixContentButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fFixContentButton.getSelection();
				fEditor.kTable().changeFixContentMode(newSelection);
				fFixContentButton.setSelection(newSelection);
			}
			
		});
	}
	
	private void createCheckSpellingButton(ToolBar toolBar) {
		fCheckSpellingButton = new ToolItem(toolBar, SWT.CHECK);
		fCheckSpellingButton.setToolTipText(Messages.ButtonManager_CheckSpellingTooltip);
		fCheckSpellingButton.setImage(ImageProvider.SPELL_CHECK);
		fCheckSpellingButton.setSelection(false);
		fCheckSpellingButton.addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fCheckSpellingButton.getSelection();
				fEditor.kTable().doCheckSpellingEnable(newSelection);
				fCheckSpellingButton.setSelection(newSelection);
			}

		});
	}
	
	private void createHideDisableObjsButton(ToolBar toolBar) {
		fHideDisableObjs = new ToolItem(toolBar, SWT.CHECK);
		fHideDisableObjs.setToolTipText(Messages.ButtonManager_HIdeDisableTooltip);
		fHideDisableObjs.setImage(ImageProvider.HIDE_DISABLE);
		fHideDisableObjs.setSelection(false);
		fHideDisableObjs.addSelectionListener(new WSelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fHideDisableObjs.getSelection();
				fEditor.kTable().doHideDisableObjsEnable(newSelection);
				fHideDisableObjs.setSelection(newSelection);
			}

		});
	}
	
	private void createReviewButton(ToolBar toolBar){
		fReviewButton = new ToolItem(toolBar, SWT.PUSH);
		fReviewButton.setToolTipText(Messages.ButtonManager_ReviewCellTooltip);
		fReviewButton.setImage(ImageProvider.COMMENT_ICON);
		ReviewDropdownSelectionListener listenerOne = new ReviewDropdownSelectionListener(fReviewButton);
		fReviewButton.addSelectionListener(listenerOne);		
	}
	
	class ReviewDropdownSelectionListener extends SelectionAdapter {
		private Menu fMenu;		
		private MenuItem fNotCommentItem;
		private MenuItem fFixedCommentItem;
		private MenuItem fFullCommentItem;
		private MenuItem fMDCommentItem;
		private MenuItem fStrictCommentsItem;

		public ReviewDropdownSelectionListener(ToolItem dropdown) {
			fMenu = new Menu(dropdown.getParent().getShell());
			addNotCommentItem();
			addFixedCommentItem();
			addFullCommentItem();			
			Separator separator = new Separator();
			separator.fill(fMenu, 3);	
			addMDCommentItem();
			addStrictCommentsItem();
		}
		
		private void addNotCommentItem() {
			fNotCommentItem = new MenuItem(fMenu, SWT.RADIO);
			fNotCommentItem.setText(Messages.ButtonManager_HideButton);
			fNotCommentItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fNotCommentItem.getSelection()) {
						tableModel().setCommentMode(DipTableModel.NOT_COMMENT);
					}
				}
				
			});
		}
		
		private void addFixedCommentItem() {
			fFixedCommentItem = new MenuItem(fMenu, SWT.RADIO);
			fFixedCommentItem.setText(Messages.ButtonManager_OnSelectButton);
			fFixedCommentItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fFixedCommentItem.getSelection()) {
						tableModel().setCommentMode(DipTableModel.FIXED_COMMENT);
					}
				}

			});
		}
		
		private void addFullCommentItem() {
			fFullCommentItem = new MenuItem(fMenu, SWT.RADIO);
			fFullCommentItem.setText(Messages.ButtonManager_AlwaysButton);
			fFullCommentItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fFullCommentItem.getSelection()) {
						tableModel().setCommentMode(DipTableModel.FULL_COMMENT);
					}					
				}
				
			});
		}

		private void addMDCommentItem() {
			fMDCommentItem = new MenuItem(fMenu, SWT.CHECK);
			fMDCommentItem.setText(Messages.ButtonManager_InlineCommentsButton);
			fMDCommentItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doShowMDComment();
				}
				
			});
		}
		
		private void addStrictCommentsItem() {
			fStrictCommentsItem = new MenuItem(fMenu, SWT.CHECK);
			fStrictCommentsItem.setText(Messages.ButtonManager_StrictCommentsButton);
			fStrictCommentsItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doShowStrictMdComments();
				}
				
			});
		}
		
		@Override
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			fMenu.setLocation(pt.x, pt.y + rect.height);
			setCurrentReviewMode();
			fMenu.setVisible(true);
		}

		private void setCurrentReviewMode() {
			if (tableModel().isNotComment()) {
				fNotCommentItem.setSelection(true);
				fFixedCommentItem.setSelection(false);
				fFullCommentItem.setSelection(false);
			} else if(tableModel().isFixedComment()) {
				fFixedCommentItem.setSelection(true);
				fNotCommentItem.setSelection(false);
				fFullCommentItem.setSelection(false);
			} else if (tableModel().isFullComment()){
				fFullCommentItem.setSelection(true);
				fFixedCommentItem.setSelection(false);
				fNotCommentItem.setSelection(false);
			}
			fMDCommentItem.setSelection(fEditor.kTable().isShowMdComment());
			fStrictCommentsItem.setSelection(fEditor.kTable().isShowStrictMdComment());
		}
	}
	
	private void createNewFileButton(ToolBar toolBar){
		fNewFileButton = new ToolItem(toolBar, SWT.PUSH);
		fNewFileButton.setToolTipText(Messages.ButtonManager_NewFileTooltip);
		fNewFileButton.setImage(ImageProvider.FILE);
		fNewFileButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doAddNewFileTrigger();
			}
			
		});
	}
	
	private void createNewFolderButton(ToolBar toolBar){
		fNewFolderButton = new ToolItem(toolBar, SWT.PUSH);
		fNewFolderButton.setToolTipText(Messages.ButtonManager_NewFolderTooltip);
		fNewFolderButton.setImage(ImageProvider.NEW_FOLDER);
		fNewFolderButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doAddNewFolderTrigger();
			}
			
		});
	}
		
	private void createUpButton(ToolBar toolBar){
		fUpButton = new ToolItem(toolBar, SWT.PUSH);
		fUpButton.setToolTipText(Messages.ButtonManager_UpTooltip);
		fUpButton.setImage(ImageProvider.UP);
		fUpButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doUpTrigger();
			}

		});
	}
	
	private void createDownButton(ToolBar toolBar){
		fDownButton = new ToolItem(toolBar, SWT.PUSH);
		fDownButton.setToolTipText(Messages.ButtonManager_DownTooltip);
		fDownButton.setImage(ImageProvider.DOWN);
	    fDownButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doDownTrigger();
				
			}
			
		});
	}
	
	private void createIntoFolderButton(ToolBar toolBar){
		fIntoFolderButton = new ToolItem(toolBar, SWT.PUSH);
		fIntoFolderButton.setToolTipText(Messages.ButtonManager_IntoFolderTooltip);
		fIntoFolderButton.setImage(ImageProvider.INTO_FOLDER);
		fIntoFolderButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doIntoFolderTrigger();
			}
			
		});
	}
	
	private void createDeleteButton(ToolBar toolBar){
		fDeleteButton = new ToolItem(toolBar, SWT.PUSH);
		fDeleteButton.setToolTipText(Messages.ButtonManager_DeleteTooltip);
		fDeleteButton.setImage(ImageProvider.DELETE);
		fDeleteButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doDeleteTrigger();
			}
			
		});
	}
	
	private void createExpandAllButton(ToolBar toolBar){
		fExpandAllButton = new ToolItem(toolBar, SWT.PUSH);
		fExpandAllButton.setToolTipText(Messages.ButtonManager_ExpandAllTooltip);
		fExpandAllButton.setImage(ImageProvider.EXPAND_ALL);
		fExpandAllButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableModel().expandAll();
			}
		});
	}
	
	private void createCollapseAllButton(ToolBar toolBar){		
		fColapseAllButton = new ToolItem(toolBar, SWT.PUSH);
		fColapseAllButton.setToolTipText(Messages.ButtonManager_CollapseAllTooltip);
		fColapseAllButton.setImage(ImageProvider.COLLAPSE_ALL);
		fColapseAllButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableModel().collapseAll();
			}
			
		});
	}
	
	private void createNumerationButton(ToolBar  toolbar){
		fNumerationButton = new ToolItem(toolbar, SWT.PUSH);
		fNumerationButton.setToolTipText(Messages.ButtonManager_NumerationTooltip);
		fNumerationButton.setImage(ImageProvider.NUMERATION);
		fNumerationListener = new NumerationSelectionListener(fNumerationButton);
		fNumerationButton.addSelectionListener(fNumerationListener);
	}
	
	class NumerationSelectionListener extends SelectionAdapter {
		private Menu fMenu;
		private MenuItem fNumerationItem;
		private MenuItem fFormNumeration;
		private MenuItem fResetItem;

		public NumerationSelectionListener(ToolItem dropdown) {
			fMenu = new Menu(dropdown.getParent().getShell());
			addNumeration();
			addResetAction();
			addFormNumeration();
		}

		private void addNumeration(){
			fNumerationItem = new MenuItem(fMenu, SWT.CHECK);
			fNumerationItem.setText(Messages.ButtonManager_NumerationButton);
			fNumerationItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doShowNumeration();
				}
				
			});			
		}
		
		private void addResetAction(){
			fResetItem = new MenuItem(fMenu, SWT.PUSH);
			fResetItem.setText(Messages.ButtonManager_ResetNumerationButton);
			fResetItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doResetNumeration();
				}
				
			});
		}
		
		private void addFormNumeration() {
			fFormNumeration = new MenuItem(fMenu, SWT.CHECK);
			fFormNumeration.setText(Messages.ButtonManager_FormNumerationButton);
			fFormNumeration.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doShowFormNumeration();
				}
				
			});			
		}
		
		@Override
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			fMenu.setLocation(pt.x, pt.y + rect.height);
			boolean showNumeration = fEditor.kTable().isShowNumeration();
			fNumerationItem.setSelection(showNumeration);
			boolean formNumeration = fEditor.kTable().isShowFormNumeration();
			fFormNumeration.setSelection(formNumeration);
			fMenu.setVisible(true);
		}
	
		public void setResetActionEnabled(boolean value) {
			fResetItem.setEnabled(value);
		}
	}
	
	private void createGlossaryButton(ToolBar  toolbar){
		fOpenGlossaryButton = new ToolItem(toolbar, SWT.PUSH);
		String bind = fBindingService.getBestActiveBindingFormattedFor("ru.dip.ui.gloss.table");  //$NON-NLS-1$
		String tooltip = Messages.ButtonManager_GlossaryTooltip;
		if (bind != null && !bind.isEmpty()) {
			tooltip = tooltip + " (" + bind + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		fOpenGlossaryButton.setToolTipText(tooltip);
		fOpenGlossaryButton.setImage(ImageProvider.GLOSS_FOLDER);
		fOpenGlossaryButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {				
				fEditor.kTable().doOpenGlossaryDialog();
			}
			
		});
	}
	
	private void createHighlightGlossaryButton(ToolBar toolbar) {
		fHiglightGlossaryButton = new ToolItem(toolbar, SWT.CHECK);
		fHiglightGlossaryButton.setToolTipText(Messages.ButtonManager_HightlightGlossaryTooltip);
		fHiglightGlossaryButton.setImage(ImageProvider.HIGHLIGHT_GLOSS);
		fHiglightGlossaryButton.setSelection(false);
		fHiglightGlossaryButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newSelection = fHiglightGlossaryButton.getSelection();
				fEditor.kTable().doHighlightGlossary(newSelection);
				fHiglightGlossaryButton.setSelection(newSelection);
			}

		});
	}
	
	private void createImageViewButton(ToolBar toolbar) {
		fOpenImageView = new ToolItem(toolbar, SWT.PUSH);
		fOpenImageView.setToolTipText(Messages.ButtonManager_ShowImagesViewTooltip);
		fOpenImageView.setImage(ImageProvider.IMAGE_VIEW);
		fOpenImageView.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtitlities.openView(ImagesView.ID);
			}
			
		});
	}
	
	private void createTableViewButton(ToolBar toolbar) {
		fOpenTableView = new ToolItem(toolbar, SWT.PUSH);
		fOpenTableView.setToolTipText(Messages.ButtonManager_ShowTablesViewTooltip);
		fOpenTableView.setImage(ImageProvider.TABLE_VIEW);
		fOpenTableView.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtitlities.openView(TablesView.ID);
			}
			
		});
	}
	
 	private void createExportButton(ToolBar  toolbar){
		if (!(fEditor.model().getContainer() instanceof DipProject)){
			return;
		}		
		fExportButton = new ToolItem(toolbar, SWT.PUSH);
		fExportButton.setToolTipText(Messages.ButtonManager_ExportTooltip);
		fExportButton.setImage(ImageProvider.EXPORT_ICON);
		fExportButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {			
				DipProject project = fEditor.getDipProject();
				Exporter exporter = new Exporter(project, fParent.getShell());
				exporter.doExport();
			}
			
		});
	}
	
	private void createShowIDButton(ToolBar  toolbar){		
		fShowIDButton = new ToolItem(toolbar, SWT.PUSH);
		//IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		String bind = fBindingService.getBestActiveBindingFormattedFor("ru.dip.ui.idshow");  //$NON-NLS-1$
		String tooltip = Messages.ButtonManager_ShowByIdTooltip;
		if (bind != null && !bind.isEmpty()) {
			tooltip = tooltip + " (" + bind + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		fShowIDButton.setToolTipText(tooltip);
		fShowIDButton.setImage(ImageProvider.SHOW_BY_ID_ICON);
		fShowIDButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doShowID();
			}
			
		});
	}
	
	private void createOpenIDButton(ToolBar  toolbar){
		fOpenIDButton = new ToolItem(toolbar, SWT.PUSH);		
		String bind = fBindingService.getBestActiveBindingFormattedFor("ru.dip.ui.idopen");  //$NON-NLS-1$
		String tooltip = Messages.ButtonManager_OpenByIdTooltip;		
		if (bind != null && !bind.isEmpty()) {
			tooltip = tooltip + " (" + bind + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}		
		fOpenIDButton.setToolTipText(tooltip);
		fOpenIDButton.setImage(ImageProvider.OPEN_BY_ID_ICON);
		fOpenIDButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fEditor.kTable().doOpenID();
			}
			
		});
	}
	
	private void createFormProperties(ToolBar toolbar) {
		fFormProperties = new ToolItem(toolbar, SWT.PUSH);
		fFormProperties.setToolTipText(Messages.ButtonManager_FormPresentationTooltip);
		fFormProperties.setImage(ImageProvider.FORM_PREFERENCES);
		FormPropertiesSelectionListener fFormPresentationListener = new FormPropertiesSelectionListener(fFormProperties);
		fFormProperties.addSelectionListener(fFormPresentationListener);
	}
	
	class FormPropertiesSelectionListener extends SelectionAdapter {
		private Menu fMenu;
		private MenuItem fEnableItem;
		private MenuItem fSettingItem;

		public FormPropertiesSelectionListener(ToolItem dropdown) {
			fMenu = new Menu(dropdown.getParent().getShell());
			addEnable();
			addSetting();
		}

		private void addEnable(){
			fEnableItem = new MenuItem(fMenu, SWT.CHECK);
			fEnableItem.setText(Messages.ButtonManager_EnableButton);
			fEnableItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					fEditor.kTable().doFormFilterPreferenciesEnable();
				}
				
			});			
		}
		
		private void addSetting(){
			fSettingItem = new MenuItem(fMenu, SWT.PUSH);
			fSettingItem.setText(Messages.ButtonManager_SettingButton);
			fSettingItem.addSelectionListener(new WSelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					ShemaPropertiesDialog dialog = new ShemaPropertiesDialog(
							fMenu.getShell(),
							fEditor.getDipProject());
					if (dialog.open() == Window.OK && fEditor.kTable().isFormShowPrefernciesEnable()) {						
						fEditor.updater().updateFormElements(true);
					}
				}

			});
		}
		
		@Override
		public void widgetSelected(SelectionEvent event) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			fMenu.setLocation(pt.x, pt.y + rect.height);		
			boolean enableFormShowPreferencies = fEditor.kTable().isFormShowPrefernciesEnable();
			fEnableItem.setSelection(enableFormShowPreferencies);
			fMenu.setVisible(true);
		}
	
	}
	
	//==========================
	// filter
	
	private void createFilterEnableButton(ToolBar toolBar){
		ToolItem showFilterButton = new ToolItem(toolBar, SWT.RADIO);
		showFilterButton.setImage(ImageProvider.FILTER);
		showFilterButton.setToolTipText(Messages.ButtonManager_FilterTooltip);
		showFilterButton.setSelection(fShowFilterGroup);
		showFilterButton.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fShowFilterGroup = !fShowFilterGroup;
				showFilterButton.setSelection(fShowFilterGroup);
				updateComposite();				
				if (fShowFilterGroup) {
					fFilterToolBar.openFilterHelp();				
				} else {
					fEditor.kTable().resetFilter();
					fFilterToolBar.closeIfOpenHelpDialog();
				}		
			}

		});    
	}
	
	private void updateComposite() {
		if (fComposite != null && !fComposite.isDisposed()) {
			fComposite.removeMouseListener(fMouseListener);
			fComposite.dispose();
		}
		createContent(fParent);
		fTableComposite.layout();
		fComposite.addMouseListener(fMouseListener);
		setTableModes();
	}
	
	public void checkFilterHelp() {
		if (fFilterToolBar != null) {
			fFilterToolBar.closeIfOpenHelpDialog();
		}
	}
	
	private void applyFilter() {
		boolean result = fEditor.kTable().applyFilter(fFilterToolBar.getText());		
		if (result) {
			fFilterToolBar.setBackground(ColorProvider.FILTER_APPLY);
		} else {
			fFilterToolBar.setBackground(TableSettings.tableDisableColor());
		}
	}
	
	//==========================
	// update
	
	public void updateFilter() {
		if (fEditor.kTable().isFilterMode()) {
			fFilterToolBar.update();
		}
	}
	
	//==========================
	// apply selection
	
 	public void applySelection(TreeSet<IDipDocumentElement> selectedElements) {
 		if (fEditor.kTable().isDiffMode() && !fEditor.kTable().isDinamicallyDiffMode()) {
 			setEditedGroupEnable(false);
 			return;
 		}
		if (selectedElements == null || selectedElements.isEmpty()) {
			setNullSlection();
		} else if (selectedElements.size() == 1) {
			applySelection(selectedElements.first());
		} else {
			applySeveralSelection(selectedElements);
		}
	}
		
	private void applySelection(IDipDocumentElement element){
		TableModel model = fEditor.model();
		if (model.isTable(element)){
			setNullSlection();
			return;
		}
		if (model.isParentHeader(element) || !model.isChild(element)){
			setNullSlection();
			setNewResEnabled(false);
			return;
		}
		if (element instanceof IncludeFolder && ((IncludeFolder) element).isErrorLink()) {
			setNullSlection();			
			setNewResEnabled(false);
			return;
		}
		if (element instanceof IncludeFolder && !element.parent().isReadOnly()) {
			boolean canUp = DipTableUtilities.canUp(element) && !fEditor.kTable().isFilterMode();
			setUpEnabled(canUp);
			boolean canDown = DipTableUtilities.canDown(element) && !fEditor.kTable().isFilterMode();
			setDownEnabled(canDown);
			setIntoFolderEnabled(true);
			setDeleteButtonEnabled(true);
			setNewResEnabled(!element.isReadOnly());
			return;
		}
		if (!element.isReadOnly()) {
			setNewResEnabled(true);
			boolean canUp = DipTableUtilities.canUp(element) && !fEditor.kTable().isFilterMode();
			setUpEnabled(canUp);
			boolean canDown = DipTableUtilities.canDown(element) && !fEditor.kTable().isFilterMode();
			setDownEnabled(canDown);
			setIntoFolderEnabled(true);
			setDeleteButtonEnabled(true);
			
			// нельзя создавать файлы в папке Appendix
			if (element instanceof DipTableContainer && Appendix.isAppendix(element.parent(), element.name())) {
				fNewFileButton.setEnabled(false);
			}			
		} else {
			setEditedGroupEnable(false);
		}
	}
	
	private void applySeveralSelection(TreeSet<IDipDocumentElement> selectedElements) {
		setEditedGroupEnable(false);
		
		for (IDipDocumentElement dipDocElement: selectedElements) {
			if (dipDocElement.isReadOnly()) {
				return;
			}
		}
		
		TableModel model = fEditor.model();		
		// delete					
		IDipDocumentElement first = selectedElements.first();			
		if (first == null || model.isTable(first) || model.isParentHeader(first)) {
			return;
		}				
		setDeleteButtonEnabled(true);
		// up
		if (DipTableUtilities.canUp(selectedElements)  && !fEditor.kTable().isFilterMode()) {
			setUpEnabled(true);
		}
		// down
		if (DipTableUtilities.canDown(selectedElements)  && !fEditor.kTable().isFilterMode()) {
			setDownEnabled(true);
		}
		// intofolder
		if (DipTableUtilities.canIntoFolder(selectedElements)) {
			setIntoFolderEnabled(true);
		}
	}
	
	/**
	 * New, Up, Down, Delete, Into Folder
	 */
	private void setEditedGroupEnable(boolean enable) {
		setNewResEnabled(enable);
		setIntoFolderEnabled(enable);
		setDeleteButtonEnabled(enable);
		setUpEnabled(enable);
		setDownEnabled(enable);
	}
	
	public void setNewResEnabled(boolean enabled){
		fNewFileButton.setEnabled(enabled);
		fNewFolderButton.setEnabled(enabled);
	}
	
	public void setNullSlection(){
		boolean readOnly = fEditor.kTable().model().isReadOnly();
		fNewFileButton.setEnabled(!readOnly);
		fNewFolderButton.setEnabled(!readOnly);
		fNumerationListener.setResetActionEnabled(!readOnly);
		
		fUpButton.setEnabled(false);
		fDownButton.setEnabled(false);
		fIntoFolderButton.setEnabled(false);
		fDeleteButton.setEnabled(false);
	}
	
	private  void setUpEnabled(boolean enable){
		fUpButton.setEnabled(enable);
	}
	
	private void setDownEnabled(boolean enable){
		fDownButton.setEnabled(enable);
	}
	
	private void setIntoFolderEnabled(boolean enable) {
		fIntoFolderButton.setEnabled(enable);		
	}
	
	private void setDeleteButtonEnabled(boolean enable){
		fDeleteButton.setEnabled(enable);
	}
	
	public void setListMode(boolean select){
		fListViewButton.setSelection(select);
		fExpandAllButton.setEnabled(!select);
		fColapseAllButton.setEnabled(!select);
	}

	public void setHighlightGlossMode(boolean select) {
		fHiglightGlossaryButton.setSelection(select);
	}
	
	public void setFixContentMode(boolean fFixedContent) {
		fFixContentButton.setSelection(fFixedContent);
	}

	public void setCheckEnable(boolean checkSpellingEnable) {
		fCheckSpellingButton.setSelection(checkSpellingEnable);
	}
	
	public void setHideDisableObjs(boolean hideDisableObjs) {
		fHideDisableObjs.setSelection(hideDisableObjs);
	}
	
	public void setDiffModeSelection(boolean selection) {
		fDiffModeButton.setSelection(selection);
	}
	
	public void setTableModes() { 
		setListMode(fEditor.kTable().isOneListMode());
		setHighlightGlossMode(fEditor.kTable().isHighlightGloss());
		setFixContentMode(fEditor.kTable().isFixedContent());
		setCheckEnable(fEditor.kTable().isCheckSpellingEnable());
		setHideDisableObjs(fEditor.kTable().isHideDisableObjs());
	}
	
	//============================
	// mouse listener
	
	public void addMouseListener(MouseListener listener) {
		fMouseListener = listener;
		fComposite.addMouseListener(listener);
	}
	
	public void removeMouseListener() {
		if (fMouseListener != null && fComposite != null && !fComposite.isDisposed()) {
			fComposite.removeMouseListener(fMouseListener);
		}
		fMouseListener = null;
	}
	
	//==============================
	// dispose
	
	
	public void dispose() {
		removeMouseListener();

		fEditor = null;
		fBindingService = null;
		fTableComposite = null;
		fParent = null;
		fComposite = null;
		fToolBar = null;
		
		if (!fFirstColumnButton.isDisposed()) {
			fFirstColumnButton.removeSelectionListener(fListenerId);
		}
		fListenerId = null;
		fFirstColumnButton = null;
		
		
		fListViewButton = null;
		fExpandAllButton = null;
		fColapseAllButton = null;
		fNumerationButton = null;
		fFixContentButton = null;
		fCheckSpellingButton = null;
		fReviewButton = null;
		fFormProperties = null;
		fHideDisableObjs = null;
		fHiglightGlossaryButton = null;

		fNewFileButton = null;
		fNewFolderButton = null;
		fUpButton = null;
		fDownButton = null;
		fIntoFolderButton = null;
		fDeleteButton = null;

		fOpenGlossaryButton = null;
		fExportButton = null;
		fShowIDButton = null;
		fOpenIDButton = null;
		fOpenImageView = null;
		fOpenTableView = null;

		fNumerationListener = null;
		fUpdateButton = null;
		fDiffModeButton = null;
	}
	
	//============================
	// getters
	
	public boolean isFocus() {
		return fComposite.isFocusControl() || fToolBar.isFocusControl();
	}
	
	public FilterToolBar getFilterToolBar() {
		return fFilterToolBar;
	}
	
	private DipTableModel tableModel() {
		return fEditor.kTable().tableModel();
	}
	
	public Composite buttonManagerComposite() {
		return fComposite;
	}
}
