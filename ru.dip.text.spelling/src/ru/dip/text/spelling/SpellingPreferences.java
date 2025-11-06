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
package ru.dip.text.spelling;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.spelling.IPreferenceStatusMonitor;
import org.eclipse.ui.texteditor.spelling.ISpellingPreferenceBlock;

public class SpellingPreferences implements ISpellingPreferenceBlock {

	private IPreferenceStore fStore;
	
	private Text fDictionaryText;
	private Button fDigits;		// Ignore words with digits
	private Button fMixed;		// Ignore mixed case words
	private Button fSentence;	// Ignore sentence capitalization
	private Button fUpper;		// Ignore upper case words
	private Button fURLS;		// Ignore internet addresses
	private Button fNonLetters;	// Ignore non-letters at word boundaries
	private Button fSingleLetters;	// Ignore single letters
	
	public SpellingPreferences() {
		fStore = TextSpellingPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		optionsComposite(composite);
		dictionaryComposite(composite);
		setValues();
		return composite;
	}

	private void optionsComposite(Composite parent) {
		Composite optionsComposite = new Composite(parent, SWT.NONE);
		optionsComposite.setLayout(new GridLayout());
		fDigits = new Button(optionsComposite, SWT.CHECK);
		fDigits.setText(Messages.SpellingPreferences_IgnoreWithDigits);
		fMixed = new Button(optionsComposite, SWT.CHECK);
		fMixed.setText(Messages.SpellingPreferences_IgnoreMixedCase);
		fSentence = new Button(optionsComposite, SWT.CHECK);
		fSentence.setText(Messages.SpellingPreferences_IgnoreSentenceCapitalization);
		fUpper = new Button(optionsComposite, SWT.CHECK);
		fUpper.setText(Messages.SpellingPreferences_IgnoreUpperCaseWords);
		fURLS = new Button(optionsComposite, SWT.CHECK);
		fURLS.setText(Messages.SpellingPreferences_IgnoraInternetAddressed);
		fNonLetters = new Button(optionsComposite, SWT.CHECK);
		fNonLetters.setText(Messages.SpellingPreferences_IgnoreNonLetters);
		fSingleLetters = new Button(optionsComposite, SWT.CHECK);
		fSingleLetters.setText(Messages.SpellingPreferences_IgnoreSingleLetters);		
	}
	
	private void dictionaryComposite(Composite parent) {
		Composite textComposite = new Composite(parent, SWT.NONE);
		textComposite.setLayout(new GridLayout(3, false));
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(textComposite, SWT.NONE);
		label.setText(Messages.SpellingPreferences_DictionaryLabel);
		
		fDictionaryText = new Text(textComposite, SWT.BORDER);
		fDictionaryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fDictionaryText.setText(TextSpellingPlugin.dictionary());
		
		Button browse = new Button(textComposite, SWT.NONE);
		browse.setText(Messages.SpellingPreferences_BrowseButton);
		browse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(textComposite.getShell());
				String path = dialog.open();
				if (path != null) {
					fDictionaryText.setText(path);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	private void setValues() {
		fDictionaryText.setText(TextSpellingPlugin.dictionary());
		fDigits.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_DIGITS));		
		fMixed.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_MIXED));
		fSentence.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_SENTENCE));
		fUpper.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_UPPER));
		fURLS.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_URLS));
		fNonLetters.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_NONLETTERS));
		fSingleLetters.setSelection(fStore.getBoolean(TextSpellingPlugin.PREF_IGNORE_SINGLE));
	}
	

	@Override
	public void initialize(IPreferenceStatusMonitor statusMonitor) {

	}

	@Override
	public void setEnabled(boolean enabled) {

	}

	@Override
	public boolean canPerformOk() {
		return true;
	}

	@Override
	public void performOk() {
		TextSpellingPlugin.setDictionary(fDictionaryText.getText());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_DIGITS, fDigits.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_MIXED, fMixed.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_SENTENCE, fSentence.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_UPPER, fUpper.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_URLS, fURLS.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_NONLETTERS, fNonLetters.getSelection());
		fStore.setValue(TextSpellingPlugin.PREF_IGNORE_SINGLE, fSingleLetters.getSelection());
	}
	

	@Override
	public void performDefaults() {
		fDictionaryText.setText(""); //$NON-NLS-1$
		fDigits.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_DIGITS));		
		fMixed.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_MIXED));
		fSentence.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_SENTENCE));
		fUpper.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_UPPER));
		fURLS.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_URLS));
		fNonLetters.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_NONLETTERS));
		fSingleLetters.setSelection(fStore.getDefaultBoolean(TextSpellingPlugin.PREF_IGNORE_SINGLE));
	}

	@Override
	public void performRevert() {

	}

	@Override
	public void dispose() {

	}

}
