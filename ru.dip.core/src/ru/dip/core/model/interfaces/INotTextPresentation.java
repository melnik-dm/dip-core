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

/**
 * Объекты не содержат текстовой информации
 * Они не могут содержать термины из глоссария, переменные, в них не работает поиск
 */
public interface INotTextPresentation extends IEmptyResultFindable, INotGlossarySupport, INotVariablesSupport  {

}
