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

import java.util.Stack;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class TagScannerRule implements IPredicateRule {
	private static final int STATE_UNDEFINED = 0;
	private static final int STATE_TEXT = 1; //
	private static final int STATE_SCAN_TAG = 2; //
	private static final int STATE_TAG = 3; //
	private static final int STATE_INCOMPLETE_TAG = 4; //
	private static final int STATE_END_TAG = 5; //
	private static final int STATE_EMPTY_TAG = 6; //
	private static final int STATE_DECL_OR_COMMENT = 7; //
	private static final int STATE_SCAN_DECLARATION = 8; //
	private static final int STATE_DECLARATION = 9; //
	private static final int STATE_SCAN_COMMENT = 10; //
	private static final int STATE_COMMENT_ABOUTTOEND = 11; //
	private static final int STATE_COMMENT = 12; //
	private static final int STATE_SCAN_PI = 13; //
	private static final int STATE_PI = 14; //
	private static final int STATE_ATTRIBUTE_STRING = 15;
	private static final int STATE_SCAN_CDATA = 16; //
	private static final int STATE_CDATA = 17; //
	private static final int STATE_CDATA_ABOUTTOEND = 18; //

	private int fState;
	private Stack<Integer> fStateStack;

	// конструктор создает стек
	public TagScannerRule() {
		fStateStack = new Stack<Integer>();
	}

	// метод возвращает Token - из integer, либо EOF
	@Override
	public IToken getSuccessToken() {
		switch (fState) {
		case STATE_TAG:
			return IPartitions.TOKEN_TAG;
		case STATE_END_TAG:
			return IPartitions.TOKEN_ENDTAG;
		case STATE_INCOMPLETE_TAG:
			return IPartitions.TOKEN_INCOMPLETETAG;
		case STATE_EMPTY_TAG:
			return IPartitions.TOKEN_EMPTYTAG;
		case STATE_TEXT:
			return IPartitions.TOKEN_TEXT;
		case STATE_COMMENT:
			return IPartitions.TOKEN_COMMENT;
		case STATE_DECLARATION:
			return IPartitions.TOKEN_DECLARATION;
		case STATE_PI:
			return IPartitions.TOKEN_PI;
		case STATE_CDATA:
			return IPartitions.TOKEN_CDATA; // yo lo agregue
		case STATE_SCAN_TAG:
		case STATE_DECL_OR_COMMENT:
		case STATE_SCAN_DECLARATION:
		case STATE_SCAN_COMMENT:
		case STATE_COMMENT_ABOUTTOEND:
		case STATE_SCAN_CDATA:
		case STATE_CDATA_ABOUTTOEND:
		case STATE_SCAN_PI:
		default:
			return Token.EOF;
		}
	}

	// метод вызывает аналогичный метод без booolean (которое не принципаиально)
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}

	// сеттер для state (integer)
	private void switchState(int newState) {
		fState = newState;
	}

	// помещает state в стек
	private void pushState() {
		fStateStack.push(Integer.valueOf(fState));
	}

	// извлекает верхний элемент из стека
	private void popState() {
		Integer i = fStateStack.pop();
		fState = i.intValue();
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		boolean done = false;
		switchState(0); // state=0
		int previous = -1;
		int character = -1;
		while (!done) {
			previous = character;
			character = scanner.read();
			if (character == -1) // если конец документа то выход из цикла
				done = true;
			else // определяем state - изначально 0 - STATE_UNDEFINED
				switch (fState) {
				case STATE_UNDEFINED:
					done = handleUndefined(scanner, character, previous);
					break;

				case STATE_TEXT:
					done = handleText(scanner, character, previous);
					break;

				case STATE_SCAN_TAG:
					done = handleScanTag(scanner, character, previous);
					break;

				case STATE_TAG:
					done = handleTag(scanner, character, previous);
					break;

				case STATE_INCOMPLETE_TAG:
					done = handleTagIncomplete(scanner, character, previous);
					break;

				case STATE_EMPTY_TAG:
					done = handleEmptyTag(scanner, character, previous);
					break;

				case STATE_END_TAG:
					done = handleEndTag(scanner, character, previous);
					break;

				case STATE_DECL_OR_COMMENT:
					done = handleDeclOrComment(scanner, character, previous);
					break;

				case STATE_SCAN_COMMENT:
					done = handleScanComment(scanner, character, previous);
					break;

				case STATE_SCAN_DECLARATION:
					done = handleScanDeclaration(scanner, character, previous);
					break;

				case STATE_COMMENT_ABOUTTOEND:
					done = handleCommentAboutToEnd(scanner, character, previous);
					break;

				case STATE_COMMENT:
					done = handleComment(scanner, character, previous);
					break;

				case STATE_SCAN_PI:
					done = handlePI(scanner, character, previous);
					break;

				case STATE_ATTRIBUTE_STRING:
					done = handleAttributeString(scanner, character, previous);
					break;
				case STATE_SCAN_CDATA:
					done = handleScanCdata(scanner, character, previous);
					break;
				case STATE_CDATA:
					done = handleAttributeCdata(scanner, character, previous);
					break;
				case STATE_CDATA_ABOUTTOEND:
					done = handleCdataAboutToEnd(scanner, character, previous);
					break;
				case STATE_DECLARATION:
				case STATE_PI:
				default:
					break;
				}
		}
		return getSuccessToken();
	}

	private boolean handleCdataAboutToEnd(ICharacterScanner scanner, int character, int previous) {
		if (previous == ']' && character == '>') {
			switchState(STATE_CDATA);
			return false;
		}

		if (previous == ']' && character != '>') {
			switchState(STATE_SCAN_CDATA);
			return false;
		} else {
			return false;
		}
	}

	private boolean handleAttributeCdata(ICharacterScanner scanner, int character, int previous) {
		scanner.unread();
		return true;
	}

	private boolean handleAttributeString(ICharacterScanner scanner, int character, int previous) {
		if (character == '"' || character == '\'')
			popState();
		else if (character == '>')
			popState();
		return false;
	}

	private boolean handleComment(ICharacterScanner scanner, int character, int previous) {
		return true;
	}

	private boolean handlePI(ICharacterScanner scanner, int character, int previous) {
		if (previous == '?' && character == '>') {
			switchState(STATE_PI);
			return true;
		} else {
			return false;
		}
	}

	private boolean handleScanDeclaration(ICharacterScanner scanner, int character, int previous) {
		if (character == '>') {
			switchState(STATE_DECLARATION);
			return true;
		} else {
			return false;
		}
	}

	private boolean handleCommentAboutToEnd(ICharacterScanner scanner, int character, int previous) {
		if (previous == '-' && character == '>') {
			switchState(STATE_COMMENT);
			return true;
		} else {
			return false;
		}
	}

	private boolean handleScanComment(ICharacterScanner scanner, int character, int previous) {
		if (previous == '-' && character == '-') {
			switchState(STATE_COMMENT_ABOUTTOEND);
			return false;
		} else {
			return false;
		}
	}

	private boolean handleScanCdata(ICharacterScanner scanner, int character, int previous) {
		if (previous == ']' && character == ']') {
			switchState(STATE_CDATA_ABOUTTOEND);
			return false;
		} else {
			return false;
		}
	}

	private boolean handleDeclOrComment(ICharacterScanner scanner, int character, int previous) {
		if (previous == '!' && character == '[') {
			return false;
		}
		if (previous == '[' && character == 'C') {
			switchState(STATE_SCAN_CDATA);
			return false;
		}
		if (previous == '!' && character == '-')
			return false;
		if (previous == '-' && character == '-') {
			switchState(STATE_SCAN_COMMENT);
			return false;
		} else {
			switchState(STATE_SCAN_DECLARATION);
			return false;
		}
	}

	private boolean handleEmptyTag(ICharacterScanner scanner, int character, int previous) {
		return character == '>';
	}

	private boolean handleEndTag(ICharacterScanner scanner, int character, int previous) {
		if (character == '<') { // если встерит < - то превратится в не законченный тег - добавил это условие надо проверить
			switchState(STATE_INCOMPLETE_TAG);
			scanner.unread();
			return true;
		}
		return character == '>';
	}

	private boolean handleTagIncomplete(ICharacterScanner scanner, int character, int previous) {
		return true;
	}

	private boolean handleTag(ICharacterScanner scanner, int character, int previous) {
		return true;
	}

	private boolean handleScanTag(ICharacterScanner scanner, int character, int previous) {
		if (character == '<') { // если встерит < - не законченный тег
			switchState(STATE_INCOMPLETE_TAG);
			scanner.unread();
			return true;
		}
		if (character == '"' || character == '\'') { // если встретит кавычки
														// или \ то
														// STATE_ATTRIBUTE_STRING
			pushState();
			switchState(STATE_ATTRIBUTE_STRING);
			return false;
		}
		if (character == '/')
			if (previous == '<') {
				switchState(STATE_END_TAG);
				return false;
			} else {
				switchState(STATE_EMPTY_TAG);
				return false;
			}
		if (character == '>') {
			switchState(STATE_TAG);
			return true;
		}
		if (previous == '<' && character == '?') {
			switchState(STATE_SCAN_PI);
			return false;
		}
		if (previous == '<' && character == '!') {
			switchState(STATE_DECL_OR_COMMENT);
			return false;
		} else {
			return false;
		}
	}

	private boolean handleText(ICharacterScanner scanner, int character, int previous) {
		if (character == '<') {
			scanner.unread();
			return true;
		} else {
			return false;
		}
	}
	
	// пока не всретит '<' - текст, как встретит сканирует как тег
	private boolean handleUndefined(ICharacterScanner scanner, int character, int previous) {
		if (character == '<') {
			switchState(STATE_SCAN_TAG);
		} else {
			switchState(STATE_TEXT);
		}
		return false;
	}
}