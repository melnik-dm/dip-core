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
package ru.dip.core.link;

import ru.dip.core.model.interfaces.IDipUnit;

public class Link {
	private String fTitle;
	private String fLink;
	private String fAllText;
	private IDipUnit fSource;

	public Link(String title, String link, IDipUnit unit) {
		fTitle = title;
		fLink = link;
		fAllText = "[" + title + "](" + link + ")";
		fSource = unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fLink == null) ? 0 : fLink.hashCode());
		result = prime * result + ((fSource == null) ? 0 : fSource.hashCode());
		result = prime * result + ((fTitle == null) ? 0 : fTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (fLink == null) {
			if (other.fLink != null)
				return false;
		} else if (!fLink.equals(other.fLink))
			return false;
		if (fSource == null) {
			if (other.fSource != null)
				return false;
		} else if (!fSource.equals(other.fSource))
			return false;
		if (fTitle == null) {
			if (other.fTitle != null)
				return false;
		} else if (!fTitle.equals(other.fTitle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return fAllText;
	}

	public String getLink() {
		return fLink;
	}

	public String getTitle() {
		return fTitle;
	}

	public String getText() {
		return fAllText;
	}

	public IDipUnit getSource() {
		return fSource;
	}

	public String getSourceId() {
		return fSource.id();
	}

	public boolean isLocatedProject(String projectName) {
		return fSource.dipProject().name().equals(projectName);
	}
}
