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
package ru.dip.core.report.scanner;

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.report.model.condition.BooleanSign;
import ru.dip.core.report.model.condition.CloseBracket;
import ru.dip.core.report.model.condition.Condition;
import ru.dip.core.report.model.condition.ConditionPart;
import ru.dip.core.report.model.condition.EndCondition;
import ru.dip.core.report.model.condition.ErrorCondition;
import ru.dip.core.report.model.condition.FieldName;
import ru.dip.core.report.model.condition.OpenBracket;
import ru.dip.core.report.model.condition.Sign;
import ru.dip.core.report.model.condition.Value;

public class RuleScanner {
	
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
	
	public static final int STATE_UNDEFINED = 0;
	public static final int STATE_EXTENSION = 3;
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
	
	private List<ConditionPart> fParts;
	private String fText;
	private char[] fChars;
	int fPreviousState = STATE_UNDEFINED;
	int fState = STATE_UNDEFINED;
	int fNextState = STATE_START_CONDITION;
	int fStart = 0;
	int fIndex = -1;
	int fOpenBracketCounter = 0;

	public RuleScanner() {
		
	}
	
	public Condition scan(String text){
		Condition condition = new Condition();
		fParts = new ArrayList<>();
		fText = text;
		fChars = text.toCharArray();
		fNextState = STATE_START_CONDITION;
		while (true){
			ConditionPart part = evaluate();
			if (part instanceof EndCondition){
				break;
			} else if (part instanceof ErrorCondition){
				return (Condition) part;
			} if (part != null){
				fParts.add(part);
				condition.addPart(part);
				if (part instanceof OpenBracket) {
					fOpenBracketCounter++;
				} else if (part instanceof CloseBracket) {
					fOpenBracketCounter--;
				}
			}
			fStart = fIndex + 1;
		}
		
		// check open bracket
		if (fOpenBracketCounter != 0) {
			if (fOpenBracketCounter < 0) {
				return new ErrorCondition("Лишняя скобка \")\"");
			}  else {
				return new ErrorCondition("Лишняя скобка \"(\"") ;
			}
		}
		
		if (condition != null) {
			condition.simplify();
		}
		return condition;
	}
	
	private int read(){
		fIndex++;
		if (fIndex  < fChars.length){
			return fChars[fIndex];
		} else {
			return -1;
		}		
	}
	
	private int unread(){
		fIndex--;
		if (fIndex >= 0){
			return fChars[fIndex];
		} else {
			return -1;
		}
	}
	
	public ConditionPart evaluate() {
		boolean done = false;
		int previous = -1;
		int character = -1;		
		while (!done) {
			previous = character;			
			character = read();
			if (character == -1){
				if (fNextState == STATE_VALUE){
					unread();
					done = true;
					fNextState = -1;
					break;
				}
				if (isErrorEnd(fState)) {
					return new ErrorCondition(fText);
				} else {
					return EndCondition.getInstance();
				}
			} 	
			switch (fNextState) {
			case STATE_START_CONDITION:{
				done = handleStartCondition(character, previous);
				break;
			}
			case STATE_AFTER_ERROR:{
				done = true;
				break;
			}
			case STATE_AFTER_OPEN_BRACKET:{
				done = handleAfterOpenBracket(character, previous);
				break;
			}
			case STATE_FIELD_NAME:{
				done = handleFieldName(character, previous);
				break;
			}
			case STATE_WHITESPACE:{
				done = handleWhiteSpace(character, previous);
				break;					
			}
			case STATE_AFTER_FIELD:{
				done = handleAfterField(character, previous);
				break;					
			}
			case STATE_SIGN:{
				done = handleSign(character, previous);
				break;
			}
			case STATE_AFTER_SIGN:{
				done = handleAfterSign(character, previous);
				break;
			}
			case STATE_VALUE:{
				done = handleValue(character, previous);
				break;
			}				
			case STATE_CLOSE_BRACKET:{
				done = handleCloseBracket(character, previous);
				break;
			}
			case STATE_AFTER_VALUE:{
				done = handleAfterValue(character, previous);
				break;
			}
			case STATE_BOOLEAN_SIGN:{
				done = handleBooleanSign(character, previous);
				break;
			}
			case STATE_AFTER_BOOLEAN_SIGN:{
				done = handleAfterBooleanSign(character, previous);
				break;
			}
			default:{
				break;
			}
			}	
		}
		return getSuccessConditionPart();
	}
	
	private boolean isErrorEnd(int state) {
		if (state == STATE_WHITESPACE) {
			return isErrorEnd(fPreviousState);
		}	
		return state == STATE_FIELD_NAME
				|| state == STATE_ERROR
				|| state == STATE_AFTER_FIELD
				|| state == STATE_OPEN_BRACKET
				|| state == STATE_BOOLEAN_SIGN 
				|| state == STATE_SIGN;	
	}
	
	private ConditionPart getSuccessConditionPart(){
		switch (fState){
		case STATE_OPEN_BRACKET: {
			return OpenBracket.instance();
		}
		case STATE_CLOSE_BRACKET:{
			return CloseBracket.instance();
		}
		case STATE_FIELD_NAME:{
			String value = fText.substring(fStart, fIndex + 1).trim();
			return new FieldName(value);
		}
		case STATE_SIGN:{
			String value = fText.substring(fStart, fIndex + 1).trim();
			return Sign.of(value);
		}
		case STATE_VALUE:{
			String value = fText.substring(fStart, fIndex + 1).trim();
			return new Value(value);
		}
		case STATE_BOOLEAN_SIGN: {
			String value = fText.substring(fStart, fIndex + 1).trim();
			//return new BooleanSign2(value);	
			return BooleanSign.of(value);	

		}
		case STATE_AFTER_ERROR: {
			String value = fText.substring(fStart, fIndex + 1).trim();
			return new ErrorCondition(value);
		}		
		}
		return null;
	}
	
	private boolean handleStartCondition(int character, int previous){
		if (character == '\n'){
			fNextState = STATE_START_CONDITION;						
			return false;
		} else if (Character.isWhitespace(character)){
			return false;			
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
	
	private boolean handleAfterOpenBracket(int character, int previous){
		if (character == '\n'){					
			return false;
		} else if (Character.isWhitespace(character)){
			return false;
			
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
	
	private boolean handleFieldName(int character, int previous){
		if (character == '\n'){
			unread();		
			fPreviousState = STATE_FIELD_NAME;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_WHITESPACE;					
			return true;
		} else if (Character.isWhitespace(character)){
			unread();		
			fPreviousState = STATE_FIELD_NAME;
			fState = STATE_FIELD_NAME;
			fNextState = STATE_WHITESPACE;
			return true;	
		} else if (isSignStartCharacter(character)) {
			unread();
			fNextState = STATE_AFTER_FIELD;
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
	
	private boolean handleWhiteSpace(int character, int previous){
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
	
	private boolean handleAfterField(int character, int previous){		
		if (character == '\n'){
			fState = STATE_WHITESPACE;
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
	private boolean handleSign(int character, int previous){
		if (Character.isWhitespace(character) || character == '\n'){
			if (previous == '<' || previous == '>'){
				unread();
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
	private boolean handleAfterSign(int character, int previous){
		if (Character.isWhitespace(character) || character == '\n'){
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
			return false;
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
	private boolean handleValue(int character, int previous){
		if (fHaveQuoters) {
			if (character == '\"') {
				fPreviousState = STATE_VALUE;
				fState = STATE_VALUE;
				fNextState = STATE_AFTER_VALUE;
				fHaveQuoters = false;
				return true;
			} else {
				return false;
			}
		} else {
			if (Character.isWhitespace(character) || character == '\n') {
				unread();
				fPreviousState = STATE_VALUE;
				fState = STATE_VALUE;
				fNextState = STATE_WHITESPACE;
				return true;
			} else if (character == '(') {
				fPreviousState = STATE_ERROR;
				fState = STATE_ERROR;
				fNextState = STATE_AFTER_ERROR;
				return true;
			} else if (character == ')') {
				unread();
				fPreviousState = STATE_VALUE;
				fState = STATE_VALUE;
				fNextState = STATE_CLOSE_BRACKET;
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean handleCloseBracket(int character, int previous){		
		if (fOpenBracketCounter <= 0) {
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
		
		fPreviousState = STATE_CLOSE_BRACKET;
		fState = STATE_CLOSE_BRACKET;
		fNextState = STATE_AFTER_VALUE; 
		return true;	
	}
	
	/**
	 *  либо после пробела либо после ')'
	 */	
	private boolean handleAfterValue(int character, int previous){
		if (Character.isWhitespace(character) || character == '\n'){
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
	private boolean handleBooleanSign(int character, int previous){
		int loverCasePrevious = Character.toLowerCase(previous);
		int loverCaseCharacter = Character.toLowerCase(character);
		
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
		} else if (loverCasePrevious == 'a' && loverCaseCharacter == 'n'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_BOOLEAN_SIGN;
			return false;
		} else if (loverCasePrevious == 'n' && loverCaseCharacter == 'd'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else if (loverCasePrevious == 'o' && loverCaseCharacter == 'r'){
			fPreviousState = STATE_BOOLEAN_SIGN;
			fState = STATE_BOOLEAN_SIGN;
			fNextState = STATE_AFTER_BOOLEAN_SIGN;
			return true;
		} else {
			unread();
			fPreviousState = STATE_ERROR;
			fState = STATE_ERROR;
			fNextState = STATE_AFTER_ERROR;
			return true;
		}
	}
	
	private boolean afterBooleanSpace = false;


	private boolean handleAfterBooleanSign(int character, int previous){
		if (Character.isWhitespace(character) || character == '\n'){
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
		if (character == 'a' || character == 'A'){
			return true;
		}
		if (character == 'o' || character == 'O'){
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
	
}
