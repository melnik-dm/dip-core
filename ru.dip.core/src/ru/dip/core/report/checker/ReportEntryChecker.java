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
package ru.dip.core.report.checker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.dip.core.model.DipReservedFolder;
import ru.dip.core.model.DipReservedUnit;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.report.model.condition.BooleanSign;
import ru.dip.core.report.model.condition.CloseBracket;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.OpenBracket;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.SimpleCondition;
import ru.dip.core.report.model.condition.Value;
import ru.dip.core.report.model.report.ReportEntry;
import ru.dip.core.report.model.report.ReportRule;

public class ReportEntryChecker {
	
	public static final Comparator<IDipElement> reportResultComparator = new Comparator<IDipElement>() {

		@Override
		public int compare(IDipElement o1, IDipElement o2) {
			if (o1 instanceof IDipDocumentElement && o2 instanceof IDipDocumentElement) {
				return IDipDocumentElement.indexComparator.compare((IDipDocumentElement)o1, (IDipDocumentElement) o2);
			}
			if (o1 instanceof IDipDocumentElement) {
				return 1;
			}
			if (o2 instanceof IDipDocumentElement) {
				return -1;
			}
						
			if (o1.resource() != null && o2.resource() != null) {
				String path1 = o1.resource().getProjectRelativePath().toOSString();
				String path2 = o2.resource().getProjectRelativePath().toOSString();
				return path1.compareTo(path2);
			}
			if (o1.resource() == null && o2.resource() == null) {
				return o1.name().compareTo(o2.name());
			}
			if (o1.resource() == null) {
				return -1;
			} else {
				return 1;
			}
		}
	};
	
	public static List<IDipElement> findEntry(ReportEntry entry, IDipParent parent) throws ReportRuleSyntaxException {
		List<IDipElement> result = new ArrayList<>();
		for (ReportRule rule: entry.getRules()){
			List<IDipElement> elements = findRule(rule, parent); 
			if (elements != null && !elements.isEmpty()){
				result.addAll(elements);
			}
		}		
		// sort
		result.sort(reportResultComparator);		
		return result;
	}
	
	private static List<IDipElement> findRule(ReportRule rule, IDipParent parent) throws ReportRuleSyntaxException{
		if (rule.getExtension() != null) {		
			ruleToFilter(rule);
		}		
		String error = FilterValidator.validateFilter(rule.getCondition(), parent.dipProject());
		if (error != null) {
			throw new ReportRuleSyntaxException(error);
		}		
		return filterUnitInRecursiveParent(rule.getCondition(), parent);
	}
	
	
	/**
	 * Преобразовать ReportRule - в формат фильтра
	 */
	public static void ruleToFilter(ReportRule rule) {		
		List<ConditionPart> conditionParts = rule.getCondition().getParts(); 
		for (ConditionPart part: rule.getCondition().getParts()) {
			if (part instanceof SimpleCondition) {
				SimpleCondition condition = (SimpleCondition) part;
				FieldName fieldName = condition.fieldName();
				String newName = rule.getExtension() + "." + fieldName.getValue();
				fieldName.setValue(newName);
			}
		}
		String bounds = rule.getBounds();
		if (bounds == null) {
			bounds = rule.getReportEntry().getBounds();
		}
		if (bounds != null) {
			conditionParts.add(0, OpenBracket.instance());
			conditionParts.add(CloseBracket.instance());
			conditionParts.add(0, BooleanSign.AND);
			SimpleCondition boundsCondition = new SimpleCondition(
					new FieldName("path"), 
					Sign.EQUAL, 
					new Value(bounds));
			conditionParts.add(0, boundsCondition);
		}
		
		// если вдруг это правило будет обрабатываться второй раз
		rule.setNullExtension();
	}
	

	/**
	 * Фильтр (как в таблице Document)
	 */
	private static List<IDipElement> filterUnitInRecursiveParent(Condition condition, IParent parent){
		List<IDipElement> result = new ArrayList<>();
		for (IDipElement dipElement: parent.getChildren()){
			if (dipElement instanceof IDipParent){
				result.addAll(filterUnitInRecursiveParent(condition, (IDipParent) dipElement));
			} else if (dipElement instanceof DipReservedFolder) {
				result.addAll(filterUnitInRecursiveParent(condition, (IParent) dipElement));				
			} else if (dipElement instanceof DipUnit){
				DipUnit unit = (DipUnit) dipElement;
				boolean checkFilter = DipDocElementConditionChecker.checkDipDocElement(unit , condition);
				if (checkFilter) {
					result.add(unit);
				}
			} else if (dipElement instanceof DipReservedUnit) {
				boolean checkFilter = NoneDocElementConditionChecker.checkDipDocElement(dipElement, condition);
				if (checkFilter) {
					result.add(dipElement);
				}
			}
		}	
		return result;
	}
}
