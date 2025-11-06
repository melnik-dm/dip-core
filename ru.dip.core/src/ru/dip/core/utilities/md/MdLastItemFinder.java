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
package ru.dip.core.utilities.md;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.md.parser.MdParser;

public class MdLastItemFinder {

	/**
	 * Возвращает следующий номер пункта для последнего найденного нумерованного
	 * списка
	 */
	public static int findNextListNumber(IDipUnit unit) {
		List<IDipUnit> elements = getMdUnitsFromFolder(unit);
		int index = elements.indexOf(unit);

		for (int i = index - 1; i >= 0; i--) {
			IDipDocumentElement element = elements.get(i);
			IDipUnit prevUnit = (IDipUnit) element;

			try {
				String content = FileUtilities.readFile(prevUnit.resource());
				Node node = MdParser.instance().parse(content, prevUnit);
				int number = findNextListNumber(node);
				if (number > 0) {
					return number;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		return -1;

	}

	private static List<IDipUnit> getMdUnitsFromFolder(IDipUnit unit) {
		return unit.parent().getDipDocChildrenList().stream().filter(IDipUnit.class::isInstance)
				.filter(dde -> !dde.isDisabled()).map(IDipUnit.class::cast).filter(u -> u.getUnitType().isMarkdown())
				.collect(Collectors.toList());
	}

	public static int findNextListNumber(Node node) {
		OrderedList list = findLastList(node);
		if (list != null) {
			return findNumber(list);
		}
		return -1;
	}

	/**
	 * Возвращает последний список
	 */
	private static OrderedList findLastList(Node node) {
		Node lastNode = node.getLastChild();
		while (lastNode != null) {
			if (lastNode instanceof OrderedList) {
				return (OrderedList) lastNode;
			}
			lastNode = lastNode.getPrevious();
		}
		return null;
	}

	/**
	 * Возвращает следующий номер для списка
	 */
	private static int findNumber(OrderedList list) {
		int count = 0;
		Node node = list.getLastChild();
		while (node != null) {
			count++;
			node = node.getPrevious();
		}
		return list.getStartNumber() + count;
	}

}
