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

public class Report {
	
	public static String ATR_DESCRIPTION = "desc";
	public static String ENTRY_ELEMENT_NAME = "entry";
	
	private String fDescription;
	private Tag fReportTag;
	private List<ReportEntry> fEntries = new ArrayList<>();
	
	public Report(){
	}
	
	public void createModel(Tag reportTag){
		fEntries.clear();
		fReportTag = reportTag;
		Attribut atrDesc = fReportTag.getAttribut(ATR_DESCRIPTION);
		if (atrDesc != null){
			fDescription = atrDesc.getValue();
		}
		if (!(fReportTag instanceof FullTag)){
			return;
		}
		for (Tag tag: ((FullTag)fReportTag).getChildren()){
			if (ENTRY_ELEMENT_NAME.equals(tag.getName())){
				ReportEntry entry = new ReportEntry(tag, this);
				fEntries.add(entry);
			}
		}
	}
	
	public List<ReportEntry> getEntries(){
		return fEntries;
	}

	public String getDescription(){
		return fDescription;
	}
}
