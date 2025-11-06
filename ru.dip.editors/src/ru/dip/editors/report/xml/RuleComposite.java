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
package ru.dip.editors.report.xml;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.dip.core.model.DipReservedUnit;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.report.checker.ReportEntryChecker;
import ru.dip.core.report.checker.ReportRuleSyntaxException;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.report.ReportEditor;
import ru.dip.ui.utilities.image.ImageProvider;

public class RuleComposite extends Composite {

	private Composite fParent;
	private ReportEditor fEditor;
	private Composite fUnitsComposite;
	private int fButtonstate = 0;
	private Label fButtonLabel; 
	private List<IDipElement> fDipElements;
	
	public RuleComposite(Composite parent, ReportEditor editor, ReportEntry entry) {
		super(parent, SWT.NONE);
		fParent = parent;
		fEditor = editor;		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginTop = - 5;
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setBackground(ColorProvider.WHITE);
		createRuleComposite(entry);
	}
	
	protected void createRuleComposite(ReportEntry entry){
		Composite titleComposite = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginTop = - 5;
		layout.marginLeft = -5;
		titleComposite.setLayout(layout);
		titleComposite.setBackground(ColorProvider.WHITE);
		fButtonLabel = new Label(titleComposite, SWT.LEFT);
		fButtonLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		Label title = new Label(titleComposite, SWT.NONE);
		title.setForeground(ColorProvider.BLACK);
		title.setFont(FontManager.boldFont);
		title.setText(entry.getName());
		Label countLabel = new Label(titleComposite, SWT.NONE);
		countLabel.setForeground(ColorProvider.BLACK);
		IDipParent parent = fEditor.getDipProject();		
		if (parent == null){
			return;
		}

		try {
			fDipElements = ReportEntryChecker.findEntry(entry, parent);
			countLabel.setText("    ( " + String.valueOf(fDipElements.size()) + " )");
			countLabel.setFont(FontManager.boldFont);
			if (!fDipElements.isEmpty()) {
				fUnitsComposite = createUnitsComposite(this, fDipElements);
				fButtonLabel.setImage(ImageProvider.COLLAPSE);
				fButtonLabel.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseUp(MouseEvent e) {
						if (fButtonstate == 0) {
							collapse();
						} else {
							expand();
						}
					}
				});
			}
		} catch (ReportRuleSyntaxException e1) {
			ControlFactory.label(this, e1.getMessage(), ColorProvider.RED);
			return;			
		}		

	}
	
	private void expand(){
		fButtonstate = 0;
		fButtonLabel.setImage(ImageProvider.COLLAPSE);
		fUnitsComposite = createUnitsComposite(RuleComposite.this, fDipElements);
		fParent.layout();
	}
	
	private void collapse(){
		fButtonstate = 1;
		fButtonLabel.setImage(ImageProvider.EXPAND);
		if (fUnitsComposite != null){
			fUnitsComposite.dispose();
		}
		fParent.layout();
	}
	
	public void expandWithCheck(){
		if (fButtonstate == 1){
			expand();
		}
	}
	
	public void collapseWithCheck(){
		if (fButtonstate == 0){
			collapse();
		}
	}
	
	private Composite createUnitsComposite(Composite parent, List<IDipElement> units){		
		Composite unitsComposite = new Composite(parent, SWT.BORDER);
		unitsComposite.setLayout(new GridLayout());
		unitsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unitsComposite.setBackground(ColorProvider.WHITE);
		if (parent != null){
			for (IDipElement element: units){
				if (element instanceof DipUnit) {
					DipUnit unit = (DipUnit) element;
					
					Hyperlink link = new Hyperlink(unitsComposite, SWT.NONE);
					link.setText(DipUtilities.relativeProjectID(unit));
					link.setForeground(ColorProvider.BLACK);
					link.setBackground(ColorProvider.WHITE);
					link.addHyperlinkListener(new HyperlinkAdapter() {
												
						@Override
						public void linkActivated(HyperlinkEvent e) {
							WorkbenchUtitlities.openFile(unit.resource());
						}
					});
				} else if (element instanceof DipReservedUnit) {
					Label label = new Label(unitsComposite, SWT.NONE);
					label.setForeground(ColorProvider.BLACK);
					label.setBackground(ColorProvider.WHITE);
					String id = element.id();
					label.setText(id);
				}
			}
		}
		return unitsComposite;
	}

}
