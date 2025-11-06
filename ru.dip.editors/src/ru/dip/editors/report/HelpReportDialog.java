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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.model.DipProject;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.ReportRule;
import ru.dip.core.report.model.report.ReportRulePresentation;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.Messages;

public class HelpReportDialog extends Dialog {

	private List<ReportEntry> fEntries;
	private DipProject fProject;

	public HelpReportDialog(Shell shell, List<ReportEntry> entries, DipProject project) {
		super(shell);
		fEntries = entries;
		fProject = project;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.HelpReportDialog_Title);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < 500){
			p.x = 500;
		}
		if (p.y < 300){
			p.y = 300;
		}
		return p;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(ColorProvider.WHITE);
		for (ReportEntry entry: fEntries){
			createEntryComposite(entry, composite);
		}
		return composite;
	}
	
	private void createEntryComposite(ReportEntry entry, Composite parent){
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));		
		composite.setBackground(ColorProvider.WHITE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(entry.getName());
		nameLabel.setFont(FontManager.boldFont);		
		Label boundsLabel = new Label(composite, SWT.NONE);
		String bounds = entry.getBounds();
		if (bounds != null && !bounds.isEmpty()){
			boundsLabel.setText(Messages.HelpReportDialog_1 + bounds + Messages.HelpReportDialog_2);
		}		
		for (ReportRule rule: entry.getRules()){
			StyledText styledText = new StyledText(composite, SWT.READ_ONLY);			
			ReportRulePresentation rulePresentation = rule.getRulePresentation(fProject);
			styledText.setText(rulePresentation.toString());
			Point extensionPoint = rulePresentation.getExtensionPoint();
			if (extensionPoint != null){
				StyleRange range = new StyleRange();
				range.fontStyle = SWT.BOLD;
				range.start = extensionPoint.x;
				range.length = extensionPoint.y - extensionPoint.x;
				styledText.setStyleRange(range);
			}
			
			for (Point p: rulePresentation.getBoundPoints()){
				StyleRange range = new StyleRange();
				range.foreground = ColorProvider.SELECT;
				range.start = p.x;
				range.length = p.y - p.x;
				styledText.setStyleRange(range);
			}
			
			for (Point p: rulePresentation.getFieldNamePoints()){
				StyleRange range = new StyleRange();
				range.foreground = ColorProvider.MAGENTA;
				range.start = p.x;
				range.length = p.y - p.x;
				styledText.setStyleRange(range);
			}
			for (Point p: rulePresentation.getValuePoints()){
				StyleRange range = new StyleRange();
				range.foreground = ColorProvider.BLUE;
				range.start = p.x;
				range.length = p.y - p.x;
				styledText.setStyleRange(range);
			}
			for (Point p: rulePresentation.getSignPoints()){
				StyleRange range = new StyleRange();
				range.fontStyle = SWT.BOLD;
				range.start = p.x;
				range.length = p.y - p.x;
				styledText.setStyleRange(range);
			}			
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			styledText.setLayoutData(gd);
		}
	}

}
