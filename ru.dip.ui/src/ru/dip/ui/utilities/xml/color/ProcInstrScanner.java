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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import ru.dip.ui.preferences.ReqEditorSettings;

public class ProcInstrScanner extends RuleBasedScanner {

    public ProcInstrScanner() {
    	IToken procInstr = new Token(new TextAttribute(ReqEditorSettings.getProcessorInstructionColor()));	
    	IRule rules[] = new IRule[2];
        rules[0] = new SingleLineRule("<?", "?>", procInstr);
        rules[1] = new WhitespaceRule(new WhitespaceDetector());
        setRules(rules);
    }
}
