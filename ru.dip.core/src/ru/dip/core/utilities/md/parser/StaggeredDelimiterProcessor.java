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
 * Из билблиотеки commonmark(https://opensource.org/license/BSD-2-Clause)) (не было доступа к конструктору)
 * https://mvnrepository.com/artifact/org.commonmark/commonmark
 * 
 * An implementation of DelimiterProcessor that dispatches all calls to two or more other DelimiterProcessors
 * depending on the length of the delimiter run. All child DelimiterProcessors must have different minimum
 * lengths. A given delimiter run is dispatched to the child with the largest acceptable minimum length. If no
 * child is applicable, the one with the largest minimum length is chosen.
 */
package ru.dip.core.utilities.md.parser;

import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.LinkedList;
import java.util.ListIterator;

class StaggeredDelimiterProcessor implements DelimiterProcessor {

    private final char delim;
    private int minLength = 0;
    private LinkedList<DelimiterProcessor> processors = new LinkedList<>(); // in reverse getMinLength order

    StaggeredDelimiterProcessor(char delim) {
        this.delim = delim;
    }


    @Override
    public char getOpeningCharacter() {
        return delim;
    }

    @Override
    public char getClosingCharacter() {
        return delim;
    }

    @Override
    public int getMinLength() {
        return minLength;
    }

    void add(DelimiterProcessor dp) {
        final int len = dp.getMinLength();
        ListIterator<DelimiterProcessor> it = processors.listIterator();
        boolean added = false;
        while (it.hasNext()) {
            DelimiterProcessor p = it.next();
            int pLen = p.getMinLength();
            if (len > pLen) {
                it.previous();
                it.add(dp);
                added = true;
                break;
            } else if (len == pLen) {
                throw new IllegalArgumentException("Cannot add two delimiter processors for char '" + delim + "' and minimum length " + len);
            }
        }
        if (!added) {
            processors.add(dp);
            this.minLength = len;
        }
    }

    private DelimiterProcessor findProcessor(int len) {
        for (DelimiterProcessor p : processors) {
            if (p.getMinLength() <= len) {
                return p;
            }
        }
        return processors.getFirst();
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        return findProcessor(openingRun.length()).process(openingRun, closingRun);
    }
}
