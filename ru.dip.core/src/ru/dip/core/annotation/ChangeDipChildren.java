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
package ru.dip.core.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Непосредственно в этом методе изменяются Children (добавляются, удаляются, меняются местами и т.д.)
 * Соответственно изменяется .dnfo
 * 
 * Если метод сам не вносит изменения, а вызывает другой метод @ChangeDipChildren - то он не помечается данной аннотацией
 * 
 * Нужно учитывать:
 * - если это AbstractDiffElement - то нужно вносить изменения и в его DiffChildren
 */
@Retention(SOURCE)
@Target({ METHOD, CONSTRUCTOR })
public @interface ChangeDipChildren {

}
