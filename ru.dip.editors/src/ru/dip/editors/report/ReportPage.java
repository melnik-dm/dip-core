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
package ru.dip.editors.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.Messages;
import ru.dip.editors.report.xml.RuleComposite;
import ru.dip.ui.utilities.image.ImageProvider;

public class ReportPage extends FormPage {

	private ReportEditor fEditor;
	private Composite fParentComposite;
	private Composite fMainComposite;
	private Label fDescriptionLabel;
	private IManagedForm fManagedForm;
	private List<RuleComposite> fRulesComposites = new ArrayList<>();
	
	public ReportPage(ReportEditor editor, String title) {
		super(editor, "gui", title);
		fEditor = editor;
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		fManagedForm = managedForm;
		fParentComposite = fManagedForm.getForm().getBody();
		fParentComposite.setLayout(new GridLayout());	
		fParentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fParentComposite.setBackground(ColorProvider.WHITE);
		fMainComposite = new Composite(fParentComposite, SWT.NONE);
		fMainComposite.setLayout(new GridLayout());
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));	
	}
	
	public void update() {
		fRulesComposites.clear();
		fMainComposite.dispose();
		fMainComposite = new Composite(fParentComposite, SWT.NONE);
		fMainComposite.setLayout(new GridLayout());
		fMainComposite.setBackground(ColorProvider.WHITE);
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createLabelComposite();
		for (ReportEntry entry : fEditor.getEntries()) {
			RuleComposite ruleComposite = new RuleComposite(fMainComposite, fEditor, entry);
			fRulesComposites.add(ruleComposite);
		}
		fMainComposite.layout();
		fParentComposite.layout();
		fManagedForm.reflow(true);
	}
	
	private void createLabelComposite(){	
		Composite labelComposite = new Composite(fMainComposite, SWT.NONE);					
		GridLayout layout = new GridLayout(3, false);		
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		labelComposite.setLayout(layout);		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		labelComposite.setLayoutData(gd);
		labelComposite.setBackground(ColorProvider.WHITE);

	
		Label label = new Label(labelComposite, SWT.NONE);
		label.setText(Messages.ReportPage_ReportLabel);
		label.setFont(FontManager.boldFont);
		label.setForeground(ColorProvider.BLACK);
		
		fDescriptionLabel = new Label(labelComposite, SWT.NONE);
		fDescriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDescriptionLabel.setForeground(ColorProvider.BLACK);
		String description = fEditor.getDescription();
		if (description != null){
			fDescriptionLabel.setText(description);
		}
		
		Hyperlink link = new Hyperlink(labelComposite, SWT.NONE);
		link.setBackground(ColorProvider.WHITE);
		link.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
		link.setFont(FontManager.boldFont);
		link.setText(Messages.ReportPage_1);
		link.addHyperlinkListener(new HyperlinkAdapter() {
				
			@Override
			public void linkActivated(HyperlinkEvent e) {
				HelpReportDialog dialog = new HelpReportDialog(fMainComposite.getShell(), 
						fEditor.getEntries(), fEditor.getDipProject().dipProject());
				dialog.open();
			}
		});

		Composite descriptionComposite = new Composite(fMainComposite, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(1, false));
		descriptionComposite.setBackground(ColorProvider.WHITE);
		descriptionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label extenedLabel = new Label(descriptionComposite, SWT.NONE);
		extenedLabel.setImage(ImageProvider.COLLAPSE_ALL);
		extenedLabel.addMouseListener(new MouseAdapter() {
		
			int state = 0;
			
			@Override
			public void mouseUp(MouseEvent e) {
				if (state == 0){
					state = 1;
					extenedLabel.setImage(ImageProvider.EXPAND_ALL);
					for (RuleComposite ruleComposite: fRulesComposites){
						ruleComposite.collapseWithCheck();
					}
				} else {
					state = 0;
					extenedLabel.setImage(ImageProvider.COLLAPSE_ALL);
					for (RuleComposite ruleComposite: fRulesComposites){
						ruleComposite.expandWithCheck();
					}
				}
			}

		});
	}
	
}
