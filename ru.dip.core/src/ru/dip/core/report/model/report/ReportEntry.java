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

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.utilities.xml.Attribut;
import ru.dip.core.utilities.xml.FullTag;
import ru.dip.core.utilities.xml.Tag;

public class ReportEntry {

	public static final String ATR_TITLE = "title";
	public static final String ATR_BOUNDS = "bounds";
	public static final String RULE_ELEMENT = "rule";
	
	@SuppressWarnings("unused")
	private Report fReport;
	private Tag fEntryTag;
	private String fTitle;
	private String fBounds;
	private List<ReportRule> fRules = new ArrayList<>();
	
	public ReportEntry(Tag tag, Report report) {
		fEntryTag = tag;
		fReport = report;
		createModel();
	}
	
	private void createModel(){
		fRules.clear();
		Attribut atrTitle = fEntryTag.getAttribut(ATR_TITLE);
		if (atrTitle != null){
			fTitle = atrTitle.getValue();
		}
		Attribut atrBounds = fEntryTag.getAttribut(ATR_BOUNDS);
		if (atrBounds != null){
			fBounds = atrBounds.getValue();
		}
		if (!(fEntryTag instanceof FullTag)){
			return;
		}
		for (Tag tag: ((FullTag)fEntryTag).getChildren()){
			if (RULE_ELEMENT.equals(tag.getName())){
				ReportRule rule = new ReportRule(tag, this);
				fRules.add(rule);
			}
		}
	}
	
	public String getName(){
		return fTitle;
	}

	public String getBounds() {		
		return fBounds;
	}

	public List<ReportRule> getRules(){
		return fRules;
	}
}
