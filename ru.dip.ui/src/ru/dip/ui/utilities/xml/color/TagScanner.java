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
package ru.dip.ui.utilities.xml.color;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import ru.dip.ui.preferences.ReqEditorSettings;

// сканер для тега - для подсветки атрибутов имени тегов и т.п.
public class TagScanner extends RuleBasedScanner {
	private IToken fTokenTag;
	private IToken fTokenString;
	private IToken fTokenAttribute;
	@SuppressWarnings("unused")
	private IToken fTokenDeclaration;
	@SuppressWarnings("unused")
	private IToken fTokenProcInst;
	private IToken fTokenEquals;

	// создаем токены с атрибутами для (тег, строка, атрибут,декларация, equals,
	// ProcInst)
	public TagScanner() {
		fTokenTag = new Token(new TextAttribute(ReqEditorSettings.getTagColor()));
		fTokenString = new Token(new TextAttribute(ReqEditorSettings.getStringColor()));
		fTokenEquals = new Token(new TextAttribute(ReqEditorSettings.getDefaultColor()));
		fTokenAttribute = new Token(new TextAttribute(ReqEditorSettings.getAttributeColor()));
		fTokenDeclaration = new Token(new TextAttribute(ReqEditorSettings.getDeclarationColor()));
		fTokenProcInst = new Token(new TextAttribute(ReqEditorSettings.getProcessorInstructionColor()));
	
		SingleCharacterWordDetector detector = new SingleCharacterWordDetector(); // создаем char детектор
		detector.addChar('='); // детектор на "="
		WordRule wordRule = new WordRule(detector, fTokenString);
		wordRule.addWord("=", fTokenEquals);

		IRule rules[] = new IRule[5];
		rules[0] = new MultiLineRule("\"", "\"", fTokenString, '\\'); 														
		rules[1] = new MultiLineRule("'", "'", fTokenString, '\\');
		rules[2] = wordRule; 
		rules[3] = new WhitespaceRule(new WhitespaceDetector()); 
		rules[4] = new IRule() { 

			private static final int STATE_UNDEFINED = 0;
			private static final int STATE_TAGSTART = 1;
			private static final int STATE_TAGEND = 2;
			private static final int STATE_ATTRIBUTE = 3;
			private int state;

			private void switchState(int newState) {
				state = newState;
			}

			public IToken getSuccessToken() {
				switch (state) {
				case STATE_TAGSTART: // '\001'
					return fTokenTag;
				case STATE_TAGEND: // '\002'
					return fTokenTag;
				case STATE_ATTRIBUTE: // '\003'
					return fTokenAttribute;
				}
				return Token.EOF;
			}

			public IToken evaluate(ICharacterScanner scanner) {
				boolean done = false;
				switchState(0);
				int previous = -1;
				int character = -1;
				while (!done) {
					previous = character;
					character = scanner.read();
					if (character == -1) {
						done = true;
					} else {
						switch (state) {
						case STATE_UNDEFINED: // '\0'
							done = handleUndefined(scanner, character, previous);
							break;
						case STATE_TAGSTART: // '\001'
							done = handleTagStart(scanner, character, previous);
							break;
						case STATE_TAGEND: // '\002'
							done = handleTagEnd(scanner, character, previous);
							break;
						case STATE_ATTRIBUTE: // '\003'
							done = handleAttribute(scanner, character, previous);
							break;
						}
					}
				}
				return getSuccessToken();
			}

			private boolean handleTagEnd(ICharacterScanner scanner, int character, int previous) {
				return character == 62; // >
			}

			private boolean handleAttribute(ICharacterScanner scanner, int character, int previous) {
				if (character == 61 /* = */ || character == 32 /* space */ || character == 47 /* / */
						|| character == 62 /* > */) {
					scanner.unread();
					return true;
				} else {
					return false;
				}
			}

			private boolean handleTagStart(ICharacterScanner scanner, int character, int previous) {
				if (character == 32 /* sp */ || character == 34 /* " */ || character == 39 /* ' */
						|| character == 62 /* > */) {
					scanner.unread();
					return true;
				} else {
					return false;
				}
			}

			private boolean handleUndefined(ICharacterScanner scanner, int character, int previous) {
				if (character == 60 /* < */) {
					switchState(STATE_TAGSTART);
				} else if (character == 47 /* / */ || character == 62 /* > */) {
					switchState(STATE_TAGEND);
				} else {
					switchState(STATE_ATTRIBUTE);
				}
				return false;
			}
		};
		setRules(rules);
	}
	
	private class SingleCharacterWordDetector implements IWordDetector {
		
		private List<Character> _chars = new ArrayList<Character>();  //лист с символами
	    
		// добавляем символ в лист
	    public void addChar(char c) {
	        _chars.add(c);  
	    }
	     
	    // возвращает false
	    @Override
	    public boolean isWordPart(char c) {
	        return false;
	    }
	 
	    // возвращает содержит ли символ
	    @Override
	    public boolean isWordStart(char c) {
	        return _chars.contains(c);
	    }
	}

}


