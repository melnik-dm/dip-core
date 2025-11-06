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
* Класс tmlInlineParser из библиотеки commonmark (https://opensource.org/license/BSD-2-Clause)
* https://mvnrepository.com/artifact/org.commonmark/commonmark
* 
* AsciiMatcher - заменен на MdAsciiMatcher, добавлены русские буквы
*/
package ru.dip.core.utilities.md.parser;

import org.commonmark.internal.inline.InlineContentParser;
import org.commonmark.internal.inline.InlineParserState;
import org.commonmark.internal.inline.ParsedInline;
import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.node.HtmlInline;


public class MdHtmlInlineParser implements InlineContentParser {

    private static final MdAsciiMatcher asciiLetter = MdAsciiMatcher.builder().range('А', 'Я')
    		.range('а','я').range('A', 'Z').range('a', 'z').build();

    // spec: A tag name consists of an ASCII letter followed by zero or more ASCII letters, digits, or hyphens (-).
    private static final MdAsciiMatcher tagNameStart = asciiLetter;
    private static final MdAsciiMatcher tagNameContinue = tagNameStart.newBuilder().range('0', '9').c('-').build();

    // spec: An attribute name consists of an ASCII letter, _, or :, followed by zero or more ASCII letters, digits,
    // _, ., :, or -. (Note: This is the XML specification restricted to ASCII. HTML5 is laxer.)
    private static final MdAsciiMatcher attributeStart = asciiLetter.newBuilder().c('_').c(':').build();
    private static final MdAsciiMatcher attributeContinue = attributeStart.newBuilder().range('0', '9').c('.').c('-').build();
    // spec: An unquoted attribute value is a nonempty string of characters not including whitespace, ", ', =, <, >, or `.
    private static final MdAsciiMatcher attributeValueEnd = MdAsciiMatcher.builder()
            .c(' ').c('\t').c('\n').c('\u000B').c('\f').c('\r')
            .c('"').c('\'').c('=').c('<').c('>').c('`')
            .build();

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
        Scanner scanner = inlineParserState.scanner();
        Position start = scanner.position();
        // Skip over `<`
        scanner.next();

        char c = scanner.peek();
        if (tagNameStart.matches(c)) {
            if (tryOpenTag(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '/') {
            if (tryClosingTag(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '?') {
            if (tryProcessingInstruction(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '!') {
            // comment, declaration or CDATA
            scanner.next();
            c = scanner.peek();
            if (c == '-') {
                if (tryComment(scanner)) {
                    return htmlInline(start, scanner);
                }
            } else if (c == '[') {
                if (tryCdata(scanner)) {
                    return htmlInline(start, scanner);
                }
            } else if (asciiLetter.matches(c)) {
                if (tryDeclaration(scanner)) {
                    return htmlInline(start, scanner);
                }
            }
        }

        return ParsedInline.none();
    }

    private static ParsedInline htmlInline(Position start, Scanner scanner) {
        String text = scanner.getSource(start, scanner.position()).getContent();
        HtmlInline node = new HtmlInline();
        node.setLiteral(text);
        return ParsedInline.of(node, scanner.position());
    }

    private static boolean tryOpenTag(Scanner scanner) {
        // spec: An open tag consists of a < character, a tag name, zero or more attributes, optional whitespace,
        // an optional / character, and a > character.
        scanner.next();
        scanner.match(tagNameContinue);
        boolean whitespace = scanner.whitespace() >= 1;
        // spec: An attribute consists of whitespace, an attribute name, and an optional attribute value specification.
        while (whitespace && scanner.match(attributeStart) >= 1) {
            scanner.match(attributeContinue);
            // spec: An attribute value specification consists of optional whitespace, a = character,
            // optional whitespace, and an attribute value.
            whitespace = scanner.whitespace() >= 1;
            if (scanner.next('=')) {
                scanner.whitespace();
                char valueStart = scanner.peek();
                if (valueStart == '\'') {
                    scanner.next();
                    if (scanner.find('\'') < 0) {
                        return false;
                    }
                    scanner.next();
                } else if (valueStart == '"') {
                    scanner.next();
                    if (scanner.find('"') < 0) {
                        return false;
                    }
                    scanner.next();
                } else {
                    if (scanner.find(attributeValueEnd) <= 0) {
                        return false;
                    }
                }

                // Whitespace is required between attributes
                whitespace = scanner.whitespace() >= 1;
            }
        }

        scanner.next('/');
        return scanner.next('>');
    }

    private static boolean tryClosingTag(Scanner scanner) {
        // spec: A closing tag consists of the string </, a tag name, optional whitespace, and the character >.
        scanner.next();
        if (scanner.match(tagNameStart) >= 1) {
            scanner.match(tagNameContinue);
            scanner.whitespace();
            return scanner.next('>');
        }
        return false;
    }

    private static boolean tryProcessingInstruction(Scanner scanner) {
        // spec: A processing instruction consists of the string <?, a string of characters not including the string ?>,
        // and the string ?>.
        scanner.next();
        while (scanner.find('?') > 0) {
            scanner.next();
            if (scanner.next('>')) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryComment(Scanner scanner) {
        // spec: An HTML comment consists of <!-- + text + -->, where text does not start with > or ->, does not end
        // with -, and does not contain --. (See the HTML5 spec.)

        // Skip first `-`
        scanner.next();
        if (!scanner.next('-')) {
            return false;
        }

        if (scanner.next('>') || scanner.next("->")) {
            return false;
        }

        while (scanner.find('-') >= 0) {
            if (scanner.next("--")) {
                return scanner.next('>');
            } else {
                scanner.next();
            }
        }

        return false;
    }

    private static boolean tryCdata(Scanner scanner) {
        // spec: A CDATA section consists of the string <![CDATA[, a string of characters not including the string ]]>,
        // and the string ]]>.

        // Skip `[`
        scanner.next();

        if (scanner.next("CDATA[")) {
            while (scanner.find(']') >= 0) {
                if (scanner.next("]]>")) {
                    return true;
                } else {
                    scanner.next();
                }
            }
        }

        return false;
    }

    private static boolean tryDeclaration(Scanner scanner) {
        // spec: A declaration consists of the string <!, an ASCII letter, zero or more characters not including
        // the character >, and the character >.
        scanner.match(asciiLetter);
        if (scanner.whitespace() <= 0) {
            return false;
        }
        if (scanner.find('>') >= 0) {
            scanner.next();
            return true;
        }
        return false;
    }
}
