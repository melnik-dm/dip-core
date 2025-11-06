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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class RulesDetectRule implements IPredicateRule {

	public static final String NAME_RULE = "_rule_name";
	public static final String FIRST_COLON = "_first_colon";
	public static final String EXTENSION = "_extension";
	public static final String SECOND_COLON = "_second_colon";
	public static final String OPEN_BRACKET = "_open_bracket";
	public static final String FIELD = "_field";
	public static final String SIGN = "_sign";
	public static final String BOOLEAN_SIGN = "_boolean_sign";
	public static final String VALUE = "_value";
	public static final String CLOSE_BRACKET = "_close_bracket";
	public static final String EMPTY_STRING = "_empty_string";
	public static final String FIRST_EMPTY_STRING = "_first_empty_string";
	public static final String ERROR = "_error";
	public static final String WHITE_SPACE = "_whitespace";
	
	
	public static IToken TOKEN_NAME_RULE = new Token(NAME_RULE);
	public static IToken TOKEN_FIRST_COLON = new Token(FIRST_COLON);
	public static IToken TOKEN_EXTENSION = new Token(EXTENSION);
	public static IToken TOKEN_SECOND_COLON = new Token(SECOND_COLON);
	public static IToken TOKEN_OPEN_BRACKET = new Token(OPEN_BRACKET);
	public static IToken TOKEN_FIELD = new Token(FIELD);
	public static IToken TOKEN_SIGN = new Token(SIGN);
	public static IToken TOKEN_BOOLEAN_SIGN = new Token(BOOLEAN_SIGN);
	public static IToken TOKEN_VALUE = new Token(VALUE);
	public static IToken TOKEN_CLOSE_BRACKET = new Token(CLOSE_BRACKET);
	public static IToken TOKEN_EMPTY_STRING = new Token(EMPTY_STRING);	
	public static IToken TOKEN_ERROR = new Token(ERROR);
	public static IToken TOKEN_EMPTY_FIRST_STRING = new Token(FIRST_EMPTY_STRING);
	public static IToken TOKEN_WHITESPACE = new Token(WHITE_SPACE);

	
	public static final int STATE_UNDEFINED = 0;
	public static final int STATE_NAME_RULE = 1;
	public static final int STATE_FIRST_COLON = 2;
	public static final int STATE_EXTENSION = 3;
	public static final int STATE_SECOND_COLON = 4;

	public static final int STATE_OPEN_BRACKET = 6;
	public static final int STATE_AFTER_OPEN_BRACKET = 7;
	
	public static final int STATE_FIELD_NAME = 8;
	public static final int STATE_SIGN = 9;
	public static final int STATE_BOOLEAN_SIGN = 10;
	public static final int STATE_VALUE = 11;
	public static final int STATE_AFTER_VALUE = 12;
	public static final int STATE_CLOSE_BRACKET = 13;

	public static final int STATE_EMPTY = 14;
	public static final int STATE_EMPTY_FIRST = 16;
	public static final int STATE_ERROR = 15;
	public static final int STATE_AFTER_ERROR = 20;
	
	public static final int STATE_START_CONDITION = 17;
	public static final int STATE_WHITESPACE = 18;
	public static final int STATE_START_EXTENSION = 19;
	
	public static final int STATE_AFTER_FIELD = 21;
	public static final int STATE_AFTER_SIGN = 22;
	public static final int STATE_AFTER_BOOLEAN_SIGN = 23;
	

	//IToken token = new Token();
	int fPreviousState = STATE_UNDEFINED;
	int fState = STATE_UNDEFINED;
	int fNextState = STATE_UNDEFINED;
	boolean fEmpty = true;
	
	@Override
	public IToken getSuccessToken() {
		switch (fState) {
		case STATE_NAME_RULE:{
			return TOKEN_NAME_RULE;
		}
		case STATE_EMPTY:{
			return TOKEN_EMPTY_STRING;
		}
		case STATE_EMPTY_FIRST:{
			return TOKEN_EMPTY_FIRST_STRING;
		}
		case STATE_ERROR:{
			return TOKEN_ERROR;
		}
		case STATE_FIRST_COLON:{
			return TOKEN_FIRST_COLON ;
		}
		case STATE_EXTENSION :{
			return TOKEN_EXTENSION;
		}
		case STATE_SECOND_COLON:{
			return TOKEN_SECOND_COLON;
		}
		case STATE_FIELD_NAME:{
			return TOKEN_FIELD;
		}
		case STATE_OPEN_BRACKET:{
			return TOKEN_OPEN_BRACKET;
		}
		case STATE_SIGN:{
			return TOKEN_SIGN;
		}
		case STATE_BOOLEAN_SIGN:{
			return TOKEN_BOOLEAN_SIGN;
		}		
		case STATE_VALUE:{
			return TOKEN_VALUE;
		}
		case STATE_CLOSE_BRACKET:{
			return TOKEN_CLOSE_BRACKET;
		}
		case STATE_WHITESPACE:{
			return TOKEN_WHITESPACE;
		}
		default:
			return Token.EOF;
		}
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		fState = STATE_UNDEFINED;
		fPreviousState = STATE_UNDEFINED;
		return evaluate(scanner);
	}
	
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		
		boolean done = false;
		int previous = -1;
		int character = -1;		
		while (!done) {
			previous = character;			
			character = scanner.read();
			if (character == -1){
				return Token.EOF;
			} 	
			switch (fNextState) {
			case STATE_UNDEFINED:{
				done = handleUndefined(scanner, character, previous);
				break;
			}
			case STATE_AFTER_ERROR:{
				done = handleAfterError(scanner, character, previous);
				break;
			}
			case STATE_EMPTY:{
				done = handleEmpty(scanner, character, previous);
				break;
			}
			case STATE_EMPTY_FIRST:{
				done = handleFirstEmpty(scanner, character, previous);
				break;
			}
			case STATE_NAME_RULE:{
				done = handleName(scanner, character, previous);
				break;
			}
			case STATE_FIRST_COLON:{
				done = handleFirstColumn(scanner, character, previous);
				break;
			}
			case STATE_START_EXTENSION:{
				done = handleStartExtension(scanner, character, previous);
				break;
			}
			case STATE_EXTENSION:{
				done = handleExtension(scanner, character, previous);
				break;				
			}
			case STATE_SECOND_COLON:{
				done = handleSecondColumn(scanner, character, previous);
				break;
			}
			case STATE_START_CONDITION:{
				done = handleStartCondition(scanner, character, previous);
				break;
			}
			case STATE_AFTER_OPEN_BRACKET:{
				done = handleAfterOpenBracket(scanner, character, previous);
				break;
			}
			case STATE_FIELD_NAME:{
				done = handleFieldName(scanner, character, previous);
				break;
			}
			case STATE_WHITESPACE:{
				done = handleWhiteSpace(scanner, character, previous);
				break;					
			}
			case STATE_AFTER_FIELD:{
				done = handleAfterField(scanner, character, previous);
				break;					
			}
			case STATE_SIGN:{
				done = handleSign(scanner, character, previous);
				break;
			}
			case STATE_AFTER_SIGN:{
				done = handleAfterSign(scanner, character, previous);
				break;
			}
			case STATE_VALUE:{
				done = handleValue(scanner, character, previous);
				break;
			}
			case STATE_CLOSE_BRACKET:{
				done = handleCloseBracket(scanner, character, previous);
				break;
			}
			case STATE_AFTER_VALUE:{
				done = handleAfterValue(scanner, character, previous);
				break;
			}
			case STATE_BOOLEAN_SIGN:{
				done = handleBooleanSign(scanner, character, previous);
				break;
			}
			case STATE_AFTER_BOOLEAN_SIGN:{
				done = handleAfterBooleanSign(scanner, character, previous);
				break;
			}
			default:{
				break;
			}
			}	
		}		
		return getSuccessToken();
	}

	private boolean handleUndefined(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
				fPreviousState = fState;
				fState = STATE_EMPTY;
				fNextState = STATE_UNDEFINED;
				return true;
		} else if(character == ':'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else {
			fPreviousState = STATE_UNDEFINED;
			fState = STATE_NAME_RULE;
			fNextState = STATE_NAME_RULE;
			scanner.unread();
			return false;
		}
	}
	
	private boolean handleAfterError
	(ICharacterScanner scanner, int character, int previous){
		if (character =='\n'){
			fPreviousState = STATE_ERROR;
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;
			return true;
		} else {
			return true;
		}
	}
	
	// первый \n
	private boolean handleFirstEmpty(ICharacterScanner scanner, int character, int previous){
		fState = STATE_EMPTY_FIRST;
		fNextState = STATE_EMPTY;
		return true;
	}
	
	// после \n
	private boolean handleEmpty(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = STATE_EMPTY;
			fState = STATE_EMPTY;
			fNextState = STATE_UNDEFINED;
			return true;
		} else if (Character.isWhitespace(character)){
			fState = STATE_WHITESPACE;
			fNextState = STATE_EMPTY;
			return true;
		} else if (character == ':'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else if (fPreviousState == STATE_NAME_RULE){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;	
			return true;
		} else if (fPreviousState == STATE_ERROR){
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else if (fPreviousState == STATE_FIRST_COLON){
			fState = STATE_EXTENSION;
			fNextState = STATE_EXTENSION;
			return false;
		} else if (fPreviousState == STATE_SECOND_COLON){
			scanner.unread();
			fNextState = STATE_START_CONDITION;
			return false;
		} else if (fPreviousState == STATE_OPEN_BRACKET){
			scanner.unread();
			fNextState = STATE_AFTER_OPEN_BRACKET;
			return false;
		} else if (fPreviousState == STATE_FIELD_NAME){
			scanner.unread();
			fNextState = STATE_AFTER_FIELD;
			return false;
		} else if (fPreviousState == STATE_SIGN){
			scanner.unread();
			fNextState = STATE_AFTER_SIGN;
			return false;
		} else if (fPreviousState == STATE_VALUE){
			scanner.unread();
			fNextState = STATE_AFTER_VALUE;
			return false;
		} else if (fPreviousState == STATE_CLOSE_BRACKET){
			scanner.unread();
			fNextState = STATE_AFTER_VALUE;
			return false;
		} else if (fPreviousState == STATE_BOOLEAN_SIGN){
			scanner.unread();
			afterBooleanSpace = true;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return false;
		} else {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;	
			return true;
		}	
	}
	
	private boolean handleName(ICharacterScanner scanner, int character, int previous){
		if (character == ':'){
			fPreviousState = STATE_NAME_RULE;
			fState = STATE_NAME_RULE;
			fNextState = STATE_FIRST_COLON;
			scanner.unread();
			return true;
		}  else if (character == '\n'){
			scanner.unread();		
			fPreviousState = STATE_NAME_RULE;
			fNextState = STATE_EMPTY_FIRST;
			fState = STATE_NAME_RULE;
			return true;
		}
		return false;
	}
	
	private boolean handleFirstColumn(ICharacterScanner scanner, int character, int previous){
		fPreviousState = STATE_FIRST_COLON;
		fState = STATE_FIRST_COLON;
		fNextState = STATE_START_EXTENSION;
		return true;
	}
	
	private boolean handleStartExtension(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;
			return true;
		} else if (Character.isWhitespace(character)){
			fState = STATE_WHITESPACE;
			return true;
		} else if (character == ':'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else {
			fState = STATE_EXTENSION;
			fNextState = STATE_EXTENSION;
			scanner.unread();
			return false;			
		}
	}
		
	private boolean handleExtension(ICharacterScanner scanner, int character, int previous){
		if (character == ':'){
			fPreviousState = STATE_EXTENSION;
			fNextState = STATE_SECOND_COLON;
			scanner.unread();
			return true;
		}  else if (character == '\n'){
			scanner.unread();		
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
		return false;	
	}
	
	private boolean handleSecondColumn(ICharacterScanner scanner, int character, int previous){
		fPreviousState = STATE_SECOND_COLON;
		fState = STATE_SECOND_COLON;
		fNextState = STATE_START_CONDITION;
		return true;
	}
	
	private boolean handleStartCondition(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;						
			return true;
		} else if (Character.isWhitespace(character)){
			fState = STATE_WHITESPACE;
			return true;
			
		} else if (character == '('){
			fPreviousState = STATE_OPEN_BRACKET;
			fState = STATE_OPEN_BRACKET;
			fNextState = STATE_AFTER_OPEN_BRACKET;
			return true;
			
		} else if (character == ')'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else {
			fState = STATE_FIELD_NAME;
			fNextState = STATE_FIELD_NAME;
			return false;
		}						
	}

	private boolean handleAfterOpenBracket(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = STATE_OPEN_BRACKET;
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;						
			return true;
		} else if (Character.isWhitespace(character)){
			fState = STATE_WHITESPACE;
			return true;
			
		} else if (character == '('){
			fPreviousState = STATE_OPEN_BRACKET;
			fState = STATE_OPEN_BRACKET;
			fNextState = STATE_AFTER_OPEN_BRACKET;
			return true;
			
		} else if (character == ')'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else {
			fPreviousState = STATE_OPEN_BRACKET;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_FIELD_NAME;
			return false;
		}	
	}
	
	private boolean handleFieldName(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			scanner.unread();		
			fPreviousState = STATE_FIELD_NAME;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_EMPTY_FIRST;					
			return true;
		} else if (Character.isWhitespace(character)){
			scanner.unread();		
			fPreviousState = STATE_FIELD_NAME;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_WHITESPACE;
			return true;	
		} else if (character == '('){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
			
		} else if (character == ')'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} 
		return false;
	}
	
	private boolean handleWhiteSpace(ICharacterScanner scanner, int character, int previous){
		fState = STATE_WHITESPACE;
		if (fPreviousState == STATE_FIELD_NAME){
			fNextState = STATE_AFTER_FIELD;
		} else if (fPreviousState == STATE_SIGN){
			fNextState = STATE_AFTER_SIGN;
		} else if (fPreviousState == STATE_VALUE){
			fNextState = STATE_AFTER_VALUE;
		}
		return true;
	}
	
	private boolean handleAfterField(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = STATE_FIELD_NAME;
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;					
			return true;
		} else if (Character.isWhitespace(character)){
			fState = STATE_WHITESPACE;
			return true;	
		} else if (isSignStartCharacter(character)){
			fState = STATE_SIGN;
			fNextState = STATE_SIGN;
			return false;
		} else {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}
	
	/**
	 * Вызывается на втором знаке (после: = ! < > )
	 */
	private boolean handleSign(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			if (previous == '<' || previous == '>'){
				scanner.unread();
				fPreviousState = STATE_SIGN;
				fState = STATE_SIGN;
				fNextState = STATE_EMPTY_FIRST;
				return true;
			} else {
				scanner.unread();
				fPreviousState = STATE_ERROR;
				fState = STATE_ERROR;
				fNextState = STATE_EMPTY_FIRST;
				return true;
			}
			
		} else if (Character.isWhitespace(character)){
			if (previous == '<' || previous == '>'){
				scanner.unread();
				fPreviousState = STATE_SIGN;
				fState = STATE_SIGN;
				fNextState = STATE_WHITESPACE;
				return true;
			} else {
				fPreviousState = STATE_ERROR;
				fState = STATE_ERROR;
				fNextState = STATE_AFTER_ERROR;
				return true;
			}						
		} else if (character == '='){
			fPreviousState = STATE_SIGN;
			fState = STATE_SIGN;
			fNextState = STATE_AFTER_SIGN;	
			return true;
		} else {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}

	/**
	 * либо после пробела / либо после '='
	 */
	private boolean handleAfterSign(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = STATE_SIGN;
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;
			return true;
		} else if (Character.isWhitespace(character)){
			fPreviousState = STATE_SIGN;
			fState = STATE_WHITESPACE;
			return true;
		} else if (character == '(' || character == ')' || character == '|' || character == '&'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else if (character == '\"'){ 
			fHaveQuoters = true;
			fState = STATE_VALUE;
			fNextState = STATE_VALUE;
			return true;
		} else {
			fState = STATE_VALUE;
			fNextState = STATE_VALUE;
			fHaveQuoters =  false;
			return false;
		}
	}
	
	private boolean fHaveQuoters = false;
	
	/**
	 * вызывается на втором символе
	 */
	private boolean handleValue(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			scanner.unread();
			fPreviousState = STATE_VALUE;
			fState = STATE_VALUE;
			fNextState = STATE_EMPTY_FIRST;
			return true;
		} else if (fHaveQuoters){
			if (character == '\"'){
				fPreviousState = STATE_VALUE;
				fState = STATE_VALUE;
				fNextState = STATE_AFTER_VALUE;
				fHaveQuoters = false;
				return true;
			} else {
				return false;
			}			
		} else if (Character.isWhitespace(character)){
			scanner.unread();
			fPreviousState = STATE_VALUE;
			fState = STATE_VALUE;
			fNextState = STATE_WHITESPACE;
			return true;
		} else if (character == '('){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else if (character == ')'){
			scanner.unread();
			fPreviousState = STATE_VALUE;
			fState = STATE_VALUE;
			fNextState = STATE_CLOSE_BRACKET;    
			return true;
		} else {
			return false;
		}
	}

	private boolean handleCloseBracket(ICharacterScanner scanner, int character, int previous){
		fPreviousState = STATE_CLOSE_BRACKET;
		fState = STATE_CLOSE_BRACKET;
		fNextState = STATE_AFTER_VALUE; 
		return true;	
	}
	
	/**
	 *  либо после пробела либо после ')'
	 */	
	private boolean handleAfterValue(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = fState;  // value либо close_bracket
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;
			return true;
		} else if (Character.isWhitespace(character)){
			fPreviousState = fState; // value либо close_bracket
			fState = STATE_WHITESPACE;
			return true;
		} else if (character == '('){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		} else if (character == ')'){
			fPreviousState = fState;  // value либо close_bracket
			fState = STATE_CLOSE_BRACKET;
			fNextState = STATE_AFTER_VALUE;    
			return true;
		} else if (isBooleanSignAfterSpace(character)){
			fPreviousState = fState;  // value либо close_bracket
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_BOOLEAN_SIGN;
			return false;
		} else {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}
	
	/**
	 *  вызывается после первого символа
	 */
	private boolean handleBooleanSign(ICharacterScanner scanner, int character, int previous){
		if (previous == '&' && character == '&'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else if (previous == '|' && character == '|'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;			
		} else if (previous == 'a' && character == 'n'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else if (previous == 'n' && character == 'd'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else if (previous == 'o' && character == 'r'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else {
			scanner.unread();
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}
	
	private boolean afterBooleanSpace = false;


	private boolean handleAfterBooleanSign(ICharacterScanner scanner, int character, int previous){
		if (character == '\n'){
			fPreviousState = STATE_BOOLEAN_SIGN; 
			fState = STATE_EMPTY_FIRST;
			fNextState = STATE_EMPTY;
			return true;
		} else if (Character.isWhitespace(character)){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_WHITESPACE;
			afterBooleanSpace = true;
			return true;
		} else if (character == '('){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_OPEN_BRACKET;
			fNextState = STATE_AFTER_OPEN_BRACKET;
			afterBooleanSpace = false;
			return true;
		} else if (character == ')'){
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			afterBooleanSpace = false;
			return true;
		} else if (afterBooleanSpace){
			//scanner.unread();
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_FIELD_NAME;
			return false;
		} else {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}
		
	private boolean isSignStartCharacter(int character){
		if (character == '='){
			return true;
		}
		if (character == '>'){
			return true;
		}
		if (character == '<'){
			return true;
		}
		if (character == '!'){
			return true;
		}
		return false;
	}


	
	private boolean isBooleanSignAfterSpace(int character){
		if (isBooleanSignStartCharacter(character)){
			return true;
		}
		if (character == 'a'){
			return true;
		}
		if (character == 'o'){
			return true;
		}
		return false;
	}
	
	private boolean isBooleanSignStartCharacter(int character){
		if (character == '&'){
			return true;
		}
		if (character == '|'){
			return true;
		}
		return false;
	}

	public void setUndefined() {
		fState = STATE_UNDEFINED;
		fPreviousState = STATE_UNDEFINED;
		fNextState = STATE_UNDEFINED;
	}
	
	public void setState(int state){
		
	}
	
	public void setPreviousState(int state){
		fPreviousState = STATE_UNDEFINED;
		fState = STATE_UNDEFINED;
		fNextState = STATE_UNDEFINED;
	}
	
	public void setPreviousState(String stateName){		
		fState = STATE_EMPTY;
		fNextState = STATE_EMPTY;
		switch (stateName){
		case NAME_RULE:{
			fPreviousState = STATE_NAME_RULE;
			break;
		}
		case FIRST_COLON:{
			fPreviousState = STATE_FIRST_COLON;
			break;
		}
		case EXTENSION:{
			fPreviousState = STATE_EXTENSION;
			break;
		}
		case SECOND_COLON:{
			fPreviousState =STATE_SECOND_COLON;
			break;
		}
		case OPEN_BRACKET:{
			fPreviousState = STATE_OPEN_BRACKET;
			break;
		}
		case FIELD:{
			fPreviousState = STATE_FIELD_NAME;
			break;
		}
		case SIGN:{
			fPreviousState = STATE_SIGN;
			break;
		}
		case BOOLEAN_SIGN:{
			fPreviousState = STATE_BOOLEAN_SIGN;	
			break;
		}
		case VALUE:{
			fPreviousState = STATE_VALUE;
			break;
		}
		case CLOSE_BRACKET:{
			fPreviousState = STATE_CLOSE_BRACKET;	
			break;
		}
		case EMPTY_STRING:{
			fPreviousState = STATE_EMPTY;
			fState = STATE_UNDEFINED;
			fNextState = STATE_UNDEFINED;
			break;
		}
		case FIRST_EMPTY_STRING:{
			fPreviousState = STATE_EMPTY_FIRST;
			break;
		}
		case ERROR:{
			fPreviousState = STATE_ERROR;
			break;
		}
		case WHITE_SPACE:{
			fPreviousState = STATE_WHITESPACE;
			break;
		} 	
		default:{
			fPreviousState = STATE_UNDEFINED;
			fState = STATE_UNDEFINED;
			fNextState = STATE_UNDEFINED;
		}
		}
	}

}
