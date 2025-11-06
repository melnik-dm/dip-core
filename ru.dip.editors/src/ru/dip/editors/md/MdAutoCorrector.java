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
package ru.dip.editors.md;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

import ru.dip.ui.preferences.MdPreferences;

/**
 * Класс для замены двойных кавычек и длинного тире
 * Используется в DocumentListener
 * 
 * Метод replace - определяет операцию замены:
 * - В редакторе Markdown и полях в UnityMdEditor, ReqEditor заменяют по разному
 * 
 */
public abstract class MdAutoCorrector {
	
	public void autoCorrect(DocumentEvent event) {
		if (MdPreferences.autoCorrect()) {
			if (checkAutoCorrect(event)) {
				return;
			}
		}
	}
	
	private boolean checkAutoCorrect(DocumentEvent event) {
		IDocument fDocument = event.getDocument();
		
		if (">".equals(event.fText) && event.fOffset > 0) { //$NON-NLS-1$
			try {
				String text = fDocument.get(event.fOffset - 1, 2);
				if (">>".equals(text)) { //$NON-NLS-1$
					replace(event.fOffset - 1, 2, "»"); //$NON-NLS-1$
					return true;
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if ("<".equals(event.fText) && event.fOffset > 0) { //$NON-NLS-1$
			try {
				String text = fDocument.get(event.fOffset - 1, 2);
				if ("<<".equals(text)) { //$NON-NLS-1$
					replace(event.fOffset - 1, 2, "«"); //$NON-NLS-1$
					return true;
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if ("-".equals(event.fText)) { //$NON-NLS-1$
			if (event.fOffset > 1) {
				try {
					String text = fDocument.get(event.fOffset - 2, 3);
					if ("---".equals(text)) { //$NON-NLS-1$
						replace(event.fOffset - 2, 3, "—"); //$NON-NLS-1$
						return true;
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}  else if (".".equals(event.fText)) { //$NON-NLS-1$
			if (event.fOffset > 1) {
				try {
					String text = fDocument.get(event.fOffset - 2, 3);
					if ("--.".equals(text)) { //$NON-NLS-1$
						replace(event.fOffset - 2, 3, "–"); //$NON-NLS-1$
						return true;
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	protected abstract void replace(int offset, int length, String text);


}
