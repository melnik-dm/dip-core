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
package ru.dip.core.form.partitioner;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

public class PartitionScanner extends RuleBasedPartitionScanner {

	public static final String CONTENT_TYPES[] = {IPartitions.TAG,IPartitions.EMPTYTAG,
			IPartitions.INCOMPLETETAG, IPartitions.ENDTAG, IPartitions.TEXT,
			IPartitions.COMMENT, IPartitions.DECLARATION, IPartitions.PI,
			IPartitions.CDATA, "default_partitition" };                // последним элементом массива было "__dftl_partition_content_type"

	public PartitionScanner() {
		IPredicateRule rules[] = new IPredicateRule[1];		
		rules[0] = new TagScannerRule();   //одно правило но оно выбирает одно из десяти (токенов сверху)
		setPredicateRules(rules);
	}
}