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

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public interface IPartitions {
	
	public static String CDATA = "CDATA";
	public static String COMMENT = "COMMENT";
	public static String DECLARATION = "DECLARATION";
	public static String EMPTYTAG = "EMPTYTAG";
	public static String ENDTAG = "ENDTAG";
	public static String INCOMPLETETAG = "INCOMPLETETAG";
	public static String PI = "PI";
	public static String TAG = "TAG";
	public static String TEXT = "TEXT";
	public static String UNDEFINED = "UNDEFINED";
	
	public static final IToken TOKEN_TAG = new Token(IPartitions.TAG);
	public static final IToken TOKEN_ENDTAG = new Token(IPartitions.ENDTAG);
	public static final IToken TOKEN_INCOMPLETETAG = new Token(IPartitions.INCOMPLETETAG);
	public static final IToken TOKEN_EMPTYTAG = new Token(IPartitions.EMPTYTAG);
	public static final IToken TOKEN_TEXT = new Token(IPartitions.TEXT);
	public static final IToken TOKEN_COMMENT = new Token(IPartitions.COMMENT);
	public static final IToken TOKEN_UNDEFINED = new Token(IPartitions.UNDEFINED);
	public static final IToken TOKEN_DECLARATION = new Token(IPartitions.DECLARATION);
	public static final IToken TOKEN_PI = new Token(IPartitions.PI);
	public static final IToken TOKEN_CDATA = new Token(IPartitions.CDATA);

}
