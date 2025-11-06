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

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.scanner.RuleScanner;
import ru.dip.core.utilities.ReportUtils;
import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.ktable.FilterValidator;
import ru.dip.ui.utilities.image.ImageProvider;
import ru.dip.ui.wizard.report.LoadReportWizard;
import ru.dip.ui.wizard.report.SaveToReportWizard;

public class FilterToolBar {
	
	public static interface IFilterToolBarListener {
		
		void apply();
		
		void reset();	
	}
	
	private Composite fParent;
	
	private Composite fToolbarComposite;
	private ToolBar fFilterToolBar;
	private Text fFilter;
	private ToolItem fResetFilter;
	private ToolItem fApplyFilter;
	private ToolItem fSaveToReport;
	private ToolItem fLoadFromReport;
	private FilterHelpController fHelpController = new FilterHelpController();	
	
	private IFilterToolBarListener fListener;
	private DipTableEditor fEditor;
	
	public FilterToolBar(DipTableEditor editor, Composite composite) {
		fEditor = editor;
		fParent = composite;
		createFilterToolbar(fParent);
	}

	private void createFilterToolbar(Composite composite) {
		fToolbarComposite = new Composite(composite, SWT.NONE);
		fToolbarComposite.setLayout(LayoutManager.notIndtentLayout(2 ,false));
		fToolbarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createFilterEntry(fToolbarComposite);
		fFilterToolBar  = new ToolBar(fToolbarComposite, SWT.BORDER);
		createFilterGroup(fFilterToolBar);
	}
	
	private void createFilterEntry(Composite toolbar) {
		fFilter = new Text(toolbar, SWT.BORDER);
		fFilter.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR){
					if (fListener != null) {
						fListener.apply();
					}
				} 
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(((e.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) && (e.keyCode == SWT.SPACE)){ 
					fHelpController.forceOpenFilterHelp(fFilter, fParent.getShell());
				} 
			}
		});
		fFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFilter.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				fFilter.setBackground(ColorProvider.WHITE);
			}
		});
	}
	
	private void createFilterGroup(ToolBar toolbar) {
		createApplyFilterButton(toolbar);
		createResetFilterButton(toolbar);
		createSaveFilterToReport(toolbar);
		createLoadFilterFromReport(toolbar);
	}
	
	private void createResetFilterButton(ToolBar toolbar) {
		fResetFilter = new ToolItem(toolbar, SWT.PUSH);
		fResetFilter.setImage(ImageProvider.FILTER_REMOVE);
		fResetFilter.setToolTipText(Messages.ButtonManager_ResetFilterTooltip);
		fResetFilter.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				fFilter.setText(""); //$NON-NLS-1$
				if (fListener != null) {
					fListener.reset();
				}
			}	
		});
		
	}
	
	private void createApplyFilterButton(ToolBar toolbar) {
		fApplyFilter = new ToolItem(toolbar, SWT.PUSH);
		fApplyFilter.setImage(ImageProvider.FILTER);
		fApplyFilter.setToolTipText(Messages.ButtonManager_ApplyFilterTooltip);
		fApplyFilter.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fListener != null) {
					fListener.apply();
				}
			}			
		});
	}
	
 	private void createSaveFilterToReport(ToolBar toolbar) {
		fSaveToReport = new ToolItem(toolbar, SWT.PUSH);
		fSaveToReport.setImage(ImageProvider.SAVE_TO_REPORT);
		fSaveToReport.setEnabled(fEditor != null);
		fSaveToReport.setToolTipText(Messages.ButtonManager_SaveReportTooltip);
		fSaveToReport.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filterText = fFilter.getText();
				// валидация фильтра				
				RuleScanner scanner = new RuleScanner();
				Condition condition = scanner.scan(filterText);
				if (!FilterValidator.isValidFilter(condition, fEditor.dipProject())) {
					return;
				}
				
				WizardDialog dialog = new WizardDialog(fParent.getShell(), new SaveToReportWizard(fEditor.dipProject(), filterText));
				dialog.open();
			}
			
		});
	}

	private void createLoadFilterFromReport(ToolBar toolbar) {
		fLoadFromReport = new ToolItem(toolbar, SWT.PUSH);
		fLoadFromReport.setImage(ImageProvider.LOAD_FROM_REPORT);
		fLoadFromReport.setEnabled(fEditor != null);
		fLoadFromReport.setToolTipText(Messages.ButtonManager_LoadFromReportTooltip);
		fLoadFromReport.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				LoadReportWizard wizard = new LoadReportWizard(fEditor.dipProject());
				WizardDialog dialog = new WizardDialog(fParent.getShell(), wizard);
				if (dialog.open() == Window.OK) {
					ReportEntry entry = wizard.getModel().getReportEntry();
					String filter = ReportUtils.getFilterFromEntry(entry);
					fFilter.setText(filter);
					if (fListener != null) {
						fListener.apply();
					}
				}
			}

		});
	}
	
	public void setListener(IFilterToolBarListener listener ) {
		fListener = listener;
	}

	public String getText() {
		return fFilter.getText().trim();
	}
	
	public Text getTextControl() {
		return fFilter;
	}

	public void setBackground(Color filterApply) {
		fFilter.setBackground(filterApply);
	}

	public void update() {
		if (!fFilterToolBar.isDisposed()) {					
			fFilter.setText(""); //$NON-NLS-1$
			if (fListener != null) {
				fListener.reset();
			}
		}
	}

	public void closeIfOpenHelpDialog() {
		fHelpController.closeIfOpen();
	}

	public void openFilterHelp() {
		fHelpController.openFilterHelp(fFilter, fParent.getShell());
	}

	public boolean isFocus() {
		return fFilter.isFocusControl();
	}

}
