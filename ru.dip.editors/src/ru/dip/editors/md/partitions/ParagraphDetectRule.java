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

public class ParagraphDetectRule implements IPredicateRule {

	private IToken fToken = Token.UNDEFINED;
	
	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		IToken token =   evaluate(scanner);
		return token;
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken token =  doEvaluate(scanner);
		return token;
	}
	
	private IToken doEvaluate(ICharacterScanner scanner){
		int c = scanner.read();
		while (isSpaceChar(c)/* || c == '\n'*/) {
			c = scanner.read();
		}
		
		if (c == ICharacterScanner.EOF) {
			return Token.UNDEFINED;
		}	
		if (isSpaceChar(c) || c == '\n') {
			return PartitionStyles.TOKEN_EMPTY_LINE;
		}
		// проверяем на список
		if (c == '*' || c == '-') {	
			
			// если продолжающийся список
			int next = scanner.read();
			if (next == '.' || next == ')') {
				return checkAfterNumberMarker(scanner);
			} else {
				scanner.unread();
			}			
		
			return checkAfterGraphicMarker(scanner);
		} else if (Character.isDigit(c)) {
			while ((c = scanner.read()) != ICharacterScanner.EOF){
				if (Character.isDigit(c)) {
					continue;
				}
				if (c == '.' || c ==')') {
					return checkAfterNumberMarker(scanner);
				}
				break;
			}
			scanner.unread();	
			if (checkEnd(scanner)) {
				fToken = PartitionStyles.TOKEN_PARAGRAPH;
				return fToken;				
			}
		} else if (c == '<') {
			c = scanner.read();
			if (c == '!' && scanner.read() == '-' && scanner.read() =='-') {
				return checkCommentEnd(scanner);
			} else {
				scanner.unread();
				return checkTokenEnd(PartitionStyles.TOKEN_PARAGRAPH, scanner);				
			}
		} else if (c == '`') {
			if (scanner.read() == '`' && scanner.read() == '`') {
				return checkCodeEnd(scanner);
			} else {
				scanner.unread();
				return checkTokenEnd(PartitionStyles.TOKEN_PARAGRAPH, scanner);				
			}		
		} else if (c == '~') { 
			if (scanner.read() == '~' && scanner.read() == '~') {
				return checkTildaCodeEnd(scanner);
			} else {
				scanner.unread();
				return checkTokenEnd(PartitionStyles.TOKEN_PARAGRAPH, scanner);				
			}
		} else if (!isSpaceChar(c)) {
			if (checkEnd(scanner)) {
				fToken = PartitionStyles.TOKEN_PARAGRAPH;
				return fToken;				
			}
		}	
		return Token.UNDEFINED;
	}
	
	private IToken checkAfterNumberMarker(ICharacterScanner scanner) {
		int c = scanner.read();
		if (c == ICharacterScanner.EOF || c == '\n') {
			fToken = PartitionStyles.TOKEN_NUMBER_LIST_ITEM;
			return fToken;
		}
		if (isSpaceChar(c)) {
			return checkTokenEnd(PartitionStyles.TOKEN_NUMBER_LIST_ITEM, scanner);
		} else {	
			return checkTokenEnd(PartitionStyles.TOKEN_PARAGRAPH, scanner);
		}
	}
	
	private IToken checkAfterGraphicMarker(ICharacterScanner scanner) {
		int c = scanner.read();
		if (c == ICharacterScanner.EOF || c == '\n') {
			fToken = PartitionStyles.TOKEN_GRAPHIC_LIST_ITEM;
			return fToken;
		}
		if (isSpaceChar(c)) {
			return checkTokenEnd(PartitionStyles.TOKEN_GRAPHIC_LIST_ITEM, scanner);
		} else {	
			return checkTokenEnd(PartitionStyles.TOKEN_PARAGRAPH, scanner);
		}
	}
	
	private IToken checkCommentEnd(ICharacterScanner scanner) {
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			if (c == '-' && scanner.read() == '-'){
				c = scanner.read();
				if (c == '>') {
					return PartitionStyles.TOKEN_COMMENT;
				} else {
					scanner.unread();
					scanner.unread();
				}			
			}
		}
		return PartitionStyles.TOKEN_COMMENT;		
	}
	
	private IToken checkCodeEnd(ICharacterScanner scanner) {
		int c;
		boolean firstline = true;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {		
			if (c == '\n') {
				firstline = false;
				while (isSpaceChar(c = scanner.read()) || c == '\n') {}				
				if (c == ICharacterScanner.EOF) {
					return PartitionStyles.TOKEN_CODE;
				}
				if (c == '`') {
					if (scanner.read() == '`') {
						if  (scanner.read() == '`') {
							return PartitionStyles.TOKEN_CODE;
						} else {
							scanner.unread();
							scanner.unread();
						}
					} else {
						scanner.unread();
					}
				}
				continue;
			}
			if (firstline && c == '`') {
				if (scanner.read() == '`') {
					if  (scanner.read() == '`') {
						return PartitionStyles.TOKEN_CODE;
					} else {
						scanner.unread();
						scanner.unread();
					}
				} else {
					scanner.unread();
				}
			}
		}
		return PartitionStyles.TOKEN_CODE;		
	}
			
	private IToken checkTildaCodeEnd(ICharacterScanner scanner) {
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			
			if (c == '\n') {
				while (isSpaceChar(c = scanner.read())) {}
				if (c == ICharacterScanner.EOF) {
					return PartitionStyles.TOKEN_CODE;
				}
				if (c == '~') {
					if  (scanner.read() == '~' && scanner.read() == '~') {
						return PartitionStyles.TOKEN_CODE;
					}					
					scanner.unread();
					scanner.unread();					
				}
				continue;
			}
		}

		return PartitionStyles.TOKEN_CODE;		
	}
	
	private IToken checkTokenEnd(IToken token, ICharacterScanner scanner) {
		if (checkEnd(scanner)) {
			fToken = token;
			return fToken;				
		}
		fToken = Token.UNDEFINED;
		return fToken;
	}
	
	private boolean checkEnd(ICharacterScanner scanner) {		
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			//int count;
			if (c == '\n') {
				c = scanner.read();
				
				// пропускаем пробелы
				int spaceCount = 0; 				
				while (isSpaceChar(c) && c != '\n') {
					c =scanner.read();
					spaceCount++;
				}				
				if (c == '<') {
					spaceCount = 0;
					if (scanner.read() == '!' && scanner.read() == '-' && scanner.read() == '-') {
						scanner.unread();
						scanner.unread();
						scanner.unread();
						scanner.unread();
						return true;
					} else {
						scanner.unread();
						continue;
					}				
				} else if (c == '`') {
					spaceCount = 0;
					if (scanner.read() == '`' && scanner.read() == '`') {
						scanner.unread();
						scanner.unread();
						scanner.unread();
						return true;
					} else {
						scanner.unread();
						continue;
					}										
				} else if (c == '~') {
					spaceCount = 0;
					if (scanner.read() == '~' && scanner.read() == '~') {
						scanner.unread();
						scanner.unread();
						scanner.unread();
						return true;
					} else {
						scanner.unread();
						continue;
					}										
				} else if (c == '*' || c == '-') {
					c = scanner.read();
					if (c == ICharacterScanner.EOF || isSpaceChar(c)) {
						scanner.unread();
						scanner.unread();
						for (int i = 0; i < spaceCount; i++) {
							scanner.unread();
						}						
						return true;
					} else {
						spaceCount = 0;
					}
									
				} else if (Character.isDigit(c)) {	
					int digitCount = 1;
					while ((c = scanner.read()) != ICharacterScanner.EOF){
						if (Character.isDigit(c)) {
							digitCount++;
							continue;
						}
						if (c == '.' || c == ')') {
							c = scanner.read();
							if (c == ICharacterScanner.EOF || isSpaceChar(c)) {
								for (int i = 0; i < digitCount + 2; i++) {
									scanner.unread();
								}
								for (int i = 0; i < spaceCount; i++) {
									scanner.unread();
								}	
								return true;
							} else {
								break;
							}
						} else {
							scanner.unread();
							break;
						}
					}
					spaceCount = 0;
					continue;
				} else if (c == '\n' || c == ICharacterScanner.EOF) {
					scanner.unread();
					return true;
				} else if (!isSpaceChar(c)) {
					spaceCount = 0;
					continue;
				}	
				boolean result = true;
				while ((c = scanner.read()) != ICharacterScanner.EOF) {
					if (c == '\n') {
						return true;
					} if (isSpaceChar(c)) {
					} else {
						result = false;
						break;
					}
				}
				if (result) {
					return true;
				}
			}	
		}				
		return true;
	}
	
	@Override
	public IToken getSuccessToken() {
		return fToken;
	}
	
	public boolean isSpaceChar(int c) {
		return Character.isSpaceChar(c) /*|| '\n' == c*/;		
	}

}
