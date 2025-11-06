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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class EmptyLineRule implements IPredicateRule {

	public EmptyLineRule() {
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		return doEvaluate(scanner);
	}
	
	public IToken doEvaluate(ICharacterScanner scanner) {
		if (isStartLine(scanner)) {
			int c;
			int count = 1;
			while ((c = scanner.read()) != ICharacterScanner.EOF && c != '\n') {
				if (!Character.isSpaceChar(c)) {
					for (int i = 0; i < count; i++) {
					}
					return Token.UNDEFINED;
				}				
			}
			return PartitionStyles.TOKEN_EMPTY_LINE;
		}		
		return Token.UNDEFINED;
	}
	
	private boolean isStartLine(ICharacterScanner scanner) {
		return scanner.getColumn() == 0;
	}

	@Override
	public IToken getSuccessToken() {		
		return PartitionStyles.TOKEN_EMPTY_LINE;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}
	
}
