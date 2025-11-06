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
package ru.dip.core.model.finder;

public class FindSettings {
	
	public static class FindSettingsBuilder {
	
		private boolean fCaseSensetive = false;		// учитывать регистр
		private boolean fFindInId = false;			// искаться в названиях файлов
		private boolean fFindInDisableObjs = true;	// искать в отключенных объектах
		
		public FindSettingsBuilder caseSensetive(boolean caseSensetive) {
			fCaseSensetive = caseSensetive;
			return this;
		}
		
		public FindSettingsBuilder findInId(boolean findInId) {
			fFindInId = findInId;
			return this;
		}
		
		public FindSettingsBuilder findInDisableObjs(boolean findInDisableObjs) {
			fFindInDisableObjs = findInDisableObjs;
			return this;
		}
		
		public FindSettings build() {
			return new FindSettings(fCaseSensetive, fFindInId, fFindInDisableObjs);
		}	
	}
	
	private static FindSettings CASE_SENSETIVE_TRUE =  new FindSettings(true, false, true);
	private static FindSettings CASE_SENSETIVE_FALSE =  new FindSettings(true, false, true);

	public static FindSettings onlyCaseSensetive(boolean caseSensetive) {
		if (caseSensetive) {
			return CASE_SENSETIVE_TRUE;
		} else {
			return CASE_SENSETIVE_FALSE;
		}
	}
	
	public static FindSettingsBuilder builder() {
		return new FindSettingsBuilder();
	}
	
	private final boolean fCaseSensetive;
	private final boolean fFindInID;
	private final boolean fFindInDisableObjs;
	
	private FindSettings(boolean caseSensetive, boolean findIndId, boolean findInDisableObjs) {
		fCaseSensetive = caseSensetive;
		fFindInID = findIndId;
		fFindInDisableObjs = findInDisableObjs;
	}
	
	public boolean caseSensetive() {
		return fCaseSensetive;
	}
	
	public boolean findInId() {
		return fFindInID;
	}
	
	public boolean findInDisableObjs() {
		return fFindInDisableObjs;
	}

}
