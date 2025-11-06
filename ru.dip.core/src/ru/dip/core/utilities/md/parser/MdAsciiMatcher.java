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

/**
 * На основе класса AsciiMatcher из библиотеки commonmark (https://opensource.org/license/BSD-2-Clause)
 * https://mvnrepository.com/artifact/org.commonmark/commonmark
 * 
 * Переопределен метод
 * public Builder c(char c), чтобы можно было добавлять любые символы (было ограничение c <= 127)
 * 
 */

package ru.dip.core.utilities.md.parser;

import java.util.BitSet;

import org.commonmark.internal.util.CharMatcher;

public class MdAsciiMatcher implements CharMatcher {
    
	private final BitSet set;
	
    private MdAsciiMatcher(Builder builder) {
        this.set = builder.set;
    }

    @Override
    public boolean matches(char c) {
        return set.get(c);
    }

    public Builder newBuilder() {
        return new Builder((BitSet) set.clone());
    }

    public static Builder builder() {
        return new Builder(new BitSet());
    }

    public static class Builder {
        
    	private final BitSet set;

        private Builder(BitSet set) {
            this.set = set;
        }

        public Builder c(char c) {
            set.set(c);
            return this;
        }

        public Builder range(char from, char toInclusive) {
            for (char c = from; c <= toInclusive; c++) {
                c(c);
            }
            return this;
        }

        public MdAsciiMatcher build() {
            return new MdAsciiMatcher(this);
        }
    }
}
