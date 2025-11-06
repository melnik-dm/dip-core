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
package ru.dip.core.model.interfaces;

import java.util.Collection;
import java.util.Set;

/**
 *  Нет поддержки терминов из глоссария
 */
public interface INotGlossarySupport extends IGlossarySupport {
	
	@Override
	default void findTerms(Set<String> terms) {}
	
	@Override
	default void removeIfFind(Collection<String> terms) {}

}
