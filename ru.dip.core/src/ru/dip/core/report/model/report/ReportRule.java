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
package ru.dip.core.report.model.report;

import ru.dip.core.model.DipProject;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.scanner.RuleScanner;
import ru.dip.core.utilities.xml.Attribut;
import ru.dip.core.utilities.xml.FullTag;
import ru.dip.core.utilities.xml.Tag;

public class ReportRule {

	public static final String ATR_EXTENSION = "ext";
	public static final String ATR_BOUNDS = "bounds";
	
	private Tag fRuleTag;
	private ReportEntry fReportEntry;
	private String fExtension;
	private String fBounds;
	private Condition fCondition;
	
	public ReportRule(Tag tag, ReportEntry reportEntry) {
		fRuleTag = tag;
		fReportEntry = reportEntry;
		createModel();
	}

	private void createModel() {
		Attribut atrTitle = fRuleTag.getAttribut(ATR_EXTENSION);
		if (atrTitle != null){
			fExtension = atrTitle.getValue();
		}
		Attribut atrBounds = fRuleTag.getAttribut(ATR_BOUNDS);
		if (atrBounds != null){
			fBounds = atrBounds.getValue();
		}
		if (fRuleTag instanceof FullTag){
			String content = ((FullTag) fRuleTag).getTextContent();
			RuleScanner scanner = new RuleScanner();
			fCondition = scanner.scan(content);
		}
	}

	public ReportRulePresentation getRulePresentation(DipProject project){
		return new ReportRulePresentation(this, project);
	}

	public String getBounds(){
		return fBounds;
	}
	
	public ReportEntry getReportEntry(){
		return fReportEntry;
	}
	
	public String getExtension(){
		return fExtension;
	}
	
	public Condition getCondition(){
		return fCondition;
	}

	public void setNullExtension() {
		fExtension = null;
	}
}
