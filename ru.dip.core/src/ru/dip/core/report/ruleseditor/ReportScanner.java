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
package ru.dip.core.report.ruleseditor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;


public class ReportScanner extends RuleBasedPartitionScanner {

	public static final String CONTENT_TYPES[] = {RulesDetectRule.NAME_RULE,RulesDetectRule.FIRST_COLON,
			RulesDetectRule.EXTENSION, RulesDetectRule.SECOND_COLON, RulesDetectRule.OPEN_BRACKET,
			RulesDetectRule.FIELD, RulesDetectRule.SIGN, RulesDetectRule.BOOLEAN_SIGN, RulesDetectRule.VALUE,
			RulesDetectRule.CLOSE_BRACKET, RulesDetectRule.EMPTY_STRING, RulesDetectRule.FIRST_EMPTY_STRING,
			RulesDetectRule.WHITE_SPACE, "default_partitition" };                // последним элементом массива было "__dftl_partition_content_type"

	RulesDetectRule rule;
	IDocument fProvider;
	
	public ReportScanner(IDocument document) {
		fProvider = document;
		IPredicateRule rules[] = new IPredicateRule[1];		
		rule = new RulesDetectRule();   //одно правило но оно выбирает одно из десяти (токенов сверху)
		rules[0] = rule;
		setPredicateRules(rules);
		//setRules(rules);
	}
	
	
	@Override
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		setPrevious(offset);
		super.setPartialRange(document, offset, length, contentType, partitionOffset);

	}
	
	private void setPrevious(int offset){
		try {
			if (offset == 0){
				rule.setPreviousState(RulesDetectRule.STATE_UNDEFINED);
			} else {
				ITypedRegion region = fProvider.getPartition(offset - 1);
				String type = region.getType();
				if (RulesDetectRule.WHITE_SPACE.equals(type) || RulesDetectRule.FIRST_EMPTY_STRING.equals(type)){
					int preOffset = offset - 2;
					while (preOffset >=0){
						region = fProvider.getPartition(preOffset);
						type = region.getType();
						if (!RulesDetectRule.WHITE_SPACE.equals(type) && !RulesDetectRule.FIRST_EMPTY_STRING.equals(type)){
							rule.setPreviousState(type);
							return;
						}
						preOffset--;
					}	
				}
				rule.setPreviousState(type);
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
}