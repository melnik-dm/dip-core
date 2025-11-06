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
package ru.dip.editors.md.partitions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

public class MdPartitionScanner extends RuleBasedPartitionScanner { 
		
	public MdPartitionScanner() {				
		IPredicateRule[] rules = new IPredicateRule[2];
		rules[1] = new EmptyLineRule();
		rules[0] = new ParagraphDetectRule();
		setPredicateRules(rules);
	}
	
	@Override
	public void setRange(IDocument document, int offset, int length) {
		super.setRange(document, 0, document.getLength());
	}
	
	@Override
	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		super.setPartialRange(document, offset, length, contentType, partitionOffset);
	}
	
	@Override
	public int getTokenLength() {
		int length =  super.getTokenLength();
		return length;
	}
	
	@Override
	public int getTokenOffset() {
		int offset =  super.getTokenOffset();
		return offset;
	}
	
	@Override
	public IToken nextToken() {
		IToken token =  super.nextToken();
		return token;
	}
	
	@Override
	public int read() {
		int result =  super.read();
		return result;
	}
	
	@Override
	public void unread() {
		super.unread();
	}
	
}
