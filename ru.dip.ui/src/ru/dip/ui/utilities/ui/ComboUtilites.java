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
package ru.dip.ui.utilities.ui;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * Класс для автодополнения combo
 */

public class ComboUtilites {
	
    private static final String LCL = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
    private static final String UCL = LCL.toUpperCase();
    private static final String NUMS = "0123456789"; //$NON-NLS-1$
	
	private ComboUtilites(){
		
	}
	
	public static void enableContentProposal(Control control)
    {       
		SimpleContentProposalProvider proposalProvider = null;
        ContentProposalAdapter proposalAdapter = null;
        if (control instanceof Combo)
        {
            Combo combo = (Combo) control;
            proposalProvider = new SimpleContentProposalProvider(combo.getItems());
            proposalAdapter = new ContentProposalAdapter(
                combo,
                new ComboContentAdapter(),
                proposalProvider,
                getActivationKeystroke(),
                getAutoactivationChars());
        }
        proposalProvider.setFiltering(true);
        proposalAdapter.setPropagateKeys(true);
        proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }
	 
    private static char[] getAutoactivationChars() {          
        String delete = new String(new char[] { 8 });
        String allChars = LCL + UCL + NUMS + delete;
        return allChars.toCharArray();
    }
 
    private static KeyStroke getActivationKeystroke() {
        KeyStroke instance = KeyStroke.getInstance(
                SWT.CTRL, Integer.valueOf(' ').intValue());
        return instance;
    }
	


}
